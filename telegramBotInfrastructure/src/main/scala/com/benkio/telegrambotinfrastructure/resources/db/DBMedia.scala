package com.benkio.telegrambotinfrastructure.resources.db

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.Media
import doobie._
import doobie.implicits._
import io.chrisdavenport.mules._
import log.effect.LogWriter

import scala.concurrent.duration._

final case class DBMediaData(
    media_name: String,
    kind: Option[String],
    media_url: String,
    media_count: Int,
    created_at: String
)

object DBMediaData {
  def apply(media: Media): DBMediaData = DBMediaData(
    media_name = media.mediaName,
    kind = media.kind,
    media_url = media.mediaUrl.renderString,
    media_count = media.mediaCount,
    created_at = media.createdAt.toString
  )
}

trait DBMedia[F[_]] {
  def getMedia(filename: String, cache: Boolean = true): F[DBMediaData]
  def getMediaByKind(kind: String, cache: Boolean = true): F[List[DBMediaData]]
  def getMediaByMediaCount(
      limit: Int = 20,
      mediaNamePrefix: Option[String] = None
  ): F[List[DBMediaData]]
  def incrementMediaCount(filename: String): F[Unit]
  def decrementMediaCount(filename: String): F[Unit]

  def getMediaQueryByName(resourceName: String): Query0[DBMediaData]
  def getMediaQueryByKind(kind: String): Query0[DBMediaData]
  def getMediaQueryByMediaCount(mediaNamePrefix: Option[String], limit: Int): Query0[DBMediaData]
}

object DBMedia {

  def apply[F[_]: Async](
      transactor: Transactor[F],
  )(implicit log: LogWriter[F]): F[DBMedia[F]] = for {
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

    override def getMediaQueryByName(resourceName: String): Query0[DBMediaData] =
      sql"SELECT media_name, kind, media_url, media_count, created_at FROM media WHERE media_name = $resourceName"
        .query[DBMediaData]

    override def getMediaQueryByKind(kind: String): Query0[DBMediaData] =
      sql"SELECT media_name, kind, media_url, media_count, created_at FROM media WHERE kind = $kind".query[DBMediaData]

    override def getMediaQueryByMediaCount(mediaNamePrefix: Option[String], limit: Int): Query0[DBMediaData] = {
      def query(whereClause: Fragment) =
        sql"SELECT media_name, kind, media_url, media_count, created_at FROM media $whereClause ORDER BY media_count DESC LIMIT $limit"
          .query[DBMediaData]
      mediaNamePrefix.fold(query(Fragment.empty))(prefix => query(s"WHERE media_name LIKE '$prefix%'".fr))
    }

    def incrementMediaCountQuery(media: DBMediaData): Update0 =
      sql"UPDATE media SET media_count = ${media.media_count + 1} WHERE media_name = ${media.media_name}".update

    def decrementMediaCountQuery(media: DBMediaData): Update0 =
      sql"UPDATE media SET media_count = ${media.media_count - 1} WHERE media_name = ${media.media_name}".update

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
      getMediaQueryByMediaCount(limit = limit, mediaNamePrefix = mediaNamePrefix).stream.compile.toList
        .transact(transactor)
  }
}
