package com.benkio.telegrambotinfrastructure.resources.db

import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.media.Media
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource.given
import doobie.*
import doobie.util.Read
import doobie.implicits.*
import io.chrisdavenport.mules.*
import log.effect.LogWriter
import io.circe.syntax.*

import scala.concurrent.duration.*

final case class DBMediaData(
    media_name: String,
    kinds: Option[String],
    mime_type: String,
    media_sources: String,
    media_count: Int,
    created_at: String
)

object DBMediaData {

  def apply(media: Media): DBMediaData = DBMediaData(
    media_name = media.mediaName,
    kinds = media.kinds.asJson.noSpaces.some,
    mime_type = mimeTypeOrDefault(media.mediaName, None),
    media_sources = media.mediaSources.asJson.noSpaces,
    media_count = media.mediaCount,
    created_at = media.createdAt.toString
  )

  def mimeTypeOrDefault(media_name: String, mime_type: Option[String]): String =
    mime_type.getOrElse(media_name.takeRight(3) match {
      case "gif" => "image/gif"
      case "jpg" => "image/jpeg"
      case "png" => "image/png"
      case "mp3" => "audio/mpeg"
      case "mp4" => "video/mp4"
      case _     => "application/octet-stream"
    })
}

trait DBMedia[F[_]] {
  def getMedia(filename: String, cache: Boolean = true): F[DBMediaData]
  def getRandomMedia(botPrefix: String): F[DBMediaData]
  def getMediaByKind(kind: String, cache: Boolean = true): F[List[DBMediaData]]
  def getMediaByMediaCount(
      limit: Int = 20,
      mediaNamePrefix: Option[String] = None
  ): F[List[DBMediaData]]
  def incrementMediaCount(filename: String): F[Unit]
  def decrementMediaCount(filename: String): F[Unit]
  def insertMedia(dbMediaData: DBMediaData): F[Unit]
}

object DBMedia {

  def apply[F[_]: Async](
      transactor: Transactor[F],
  )(using log: LogWriter[F]): F[DBMedia[F]] = for {
    dbCache <- MemoryCache.ofSingleImmutableMap[F, String, List[DBMediaData]](defaultExpiration =
      TimeSpec.fromDuration(6.hours)
    )
  } yield new DBMediaImpl[F](
    transactor = transactor,
    dbCache = dbCache,
    log = log
  )

  private[telegrambotinfrastructure] class DBMediaImpl[F[_]: Async](
      transactor: Transactor[F],
      dbCache: MemoryCache[F, String, List[DBMediaData]],
      log: LogWriter[F]
  ) extends DBMedia[F] {

    private def getMediaInternal[A](
        cacheLookupValue: String,
        cacheResultHandler: Option[List[DBMediaData]] => F[A]
    ): F[A] = for {
      _              <- log.info(s"DB fetching media by $cacheLookupValue")
      cachedValueOpt <- dbCache.lookup(cacheLookupValue)
      media          <- cacheResultHandler(cachedValueOpt)
    } yield media

    override def incrementMediaCount(filename: String): F[Unit] = for {
      _     <- dbCache.delete(filename)
      media <- getMedia(filename)
      _     <- incrementMediaCountQuery(media).run.transact(transactor)
    } yield ()

    override def decrementMediaCount(filename: String): F[Unit] = for {
      _     <- dbCache.delete(filename)
      media <- getMedia(filename)
      _     <- decrementMediaCountQuery(media).run.transact(transactor)
    } yield ()

    override def getMedia(filename: String, cache: Boolean = true): F[DBMediaData] =
      getMediaInternal[DBMediaData](
        cacheLookupValue = filename,
        cacheResultHandler = cachedValueOpt =>
          for {
            media <- cachedValueOpt
              .filter(_ => cache)
              .flatMap(_.headOption)
              .fold[F[DBMediaData]](
                getMediaQueryByName(filename).unique.transact(transactor)
              )(Async[F].pure)
            _ <-
              if (cachedValueOpt.fold(true)(_.length != 1))
                dbCache.insert(filename, List(media))
              else Async[F].unit
          } yield media
      )

    override def getRandomMedia(botPrefix: String): F[DBMediaData] =
      getMediaQueryByRandom(botPrefix).unique
        .transact(transactor)

    override def getMediaByKind(kind: String, cache: Boolean = true): F[List[DBMediaData]] =
      getMediaInternal[List[DBMediaData]](
        cacheLookupValue = kind,
        cacheResultHandler = cachedValueOpt =>
          for {
            medias <- cachedValueOpt
              .filter(_ => cache)
              .fold(
                getMediaQueryByKind(kind).stream.compile.toList.transact(transactor)
              )(Async[F].pure)
            _ <-
              if (cachedValueOpt.isEmpty)
                dbCache.insert(kind, medias)
              else Async[F].unit
          } yield medias
      )

    override def getMediaByMediaCount(
        limit: Int = 20,
        mediaNamePrefix: Option[String] = None
    ): F[List[DBMediaData]] =
      getMediaQueryByMediaCount(mediaNamePrefix = mediaNamePrefix).stream
        .take(limit.toLong)
        .compile
        .toList
        .transact(transactor)

    override def insertMedia(dbMediaData: DBMediaData): F[Unit] =
      insertSql(dbMediaData).run.transact(transactor).void.exceptSql {
        case e if e.getMessage().contains("UNIQUE constraint failed") =>
          updateOnConflictSql(dbMediaData).run.transact(transactor).void
        case e =>
          MonadCancelThrow[F].raiseError(
            new RuntimeException(s"An error occurred in inserting $dbMediaData with exception: $e")
          )
      }
  }

  def insertSql(dbMediaData: DBMediaData): Update0 =
    sql"INSERT INTO media (media_name, kinds, mime_type, media_sources, created_at, media_count) VALUES (${dbMediaData.media_name}, ${dbMediaData.kinds.asJson.noSpaces}, ${dbMediaData.mime_type}, ${dbMediaData.media_sources.asJson.noSpaces}, ${dbMediaData.created_at}, 0);".update

  def updateOnConflictSql(dbMediaData: DBMediaData): Update0 =
    sql"UPDATE media SET kinds = ${dbMediaData.kinds.asJson.noSpaces}, media_sources = ${dbMediaData.media_sources.asJson.noSpaces} WHERE media_name = ${dbMediaData.media_name};".update

  def getMediaQueryByName(resourceName: String): Query0[DBMediaData] =
    sql"SELECT media_name, kinds, media_source, media_count, created_at FROM media WHERE media_name = $resourceName"
      .query[DBMediaData]

  def getMediaQueryByKind(kind: String): Query0[DBMediaData] =
    (fr"SELECT media_name, kinds, media_source, media_count, created_at FROM media" ++
      Fragments.whereOr(
        fr"""kinds LIKE ${"""["""" + kind + """",%"""}""",
        fr"""kinds LIKE ${"""%,"""" + kind + """",%"""}""",
        fr"""kinds LIKE ${"""%,"""" + kind + """"]"""}""",
        fr"""kinds LIKE ${"""["""" + kind + """"]"""}""",
      )).query[DBMediaData]

  def getMediaQueryByMediaCount(mediaNamePrefix: Option[String]): Query0[DBMediaData] = {
    val q: Fragment =
      fr"SELECT media_name, kinds, media_source, media_count, created_at FROM media" ++
        Fragments.whereAndOpt(mediaNamePrefix.map(s => {
          val like = s + "%"
          fr"media_name LIKE $like"
        })) ++
        fr"ORDER BY media_count DESC"

    q.query[DBMediaData]
  }
  def getMediaQueryByRandom(botPrefix: String): Query0[DBMediaData] =
    val like = botPrefix + "%"
    sql"SELECT media_name, kinds, media_source, media_count, created_at FROM media WHERE media_name LIKE $like ORDER BY RANDOM() LIMIT 1"
      .query[DBMediaData]
  def incrementMediaCountQuery(media: DBMediaData): Update0 =
    sql"UPDATE media SET media_count = ${media.media_count + 1} WHERE media_name = ${media.media_name}".update

  def decrementMediaCountQuery(media: DBMediaData): Update0 =
    sql"UPDATE media SET media_count = ${media.media_count - 1} WHERE media_name = ${media.media_name}".update
}
