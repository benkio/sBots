package com.benkio.telegrambotinfrastructure.repository.db

import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.media.Media
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource.given
import com.benkio.telegrambotinfrastructure.model.MimeTypeOps.given
import com.benkio.telegrambotinfrastructure.model.SBotId
import doobie.*
import doobie.implicits.*
import io.chrisdavenport.mules.*
import io.circe.syntax.*
import log.effect.LogWriter

import scala.concurrent.duration.*

final case class DBMediaData(
    media_name: String,
    bot_id: String,
    kinds: String,
    mime_type: String,
    media_sources: String,
    media_count: Int,
    created_at: String
)

object DBMediaData {

  def apply(media: Media): DBMediaData = DBMediaData(
    media_name = media.mediaName,
    bot_id = media.botId.value,
    kinds = media.kinds.asJson.noSpaces,
    mime_type = media.mimeType.show,
    media_sources = media.mediaSources.asJson.noSpaces,
    media_count = media.mediaCount,
    created_at = media.createdAt.toString
  )
}

trait DBMedia[F[_]] {
  def getMedia(filename: String, cache: Boolean = true): F[Option[DBMediaData]]
  def getRandomMedia(botId: SBotId): F[Option[DBMediaData]]
  def getMediaByKind(kind: String, botId: SBotId, cache: Boolean = true): F[List[DBMediaData]]
  def getMediaByMediaCount(
      limit: Int = 20,
      botId: Option[SBotId] = None
  ): F[List[DBMediaData]]
  def incrementMediaCount(filename: String): F[Unit]
  def decrementMediaCount(filename: String): F[Unit]
  def insertMedia(dbMediaData: DBMediaData): F[Unit]
}

object DBMedia {

  def apply[F[_]: Async](
      transactor: Transactor[F]
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
      _              <- log.debug(s"[DBMedia] DB fetching media with $cacheLookupValue")
      cachedValueOpt <- dbCache.lookup(cacheLookupValue)
      media          <- cacheResultHandler(cachedValueOpt)
      _              <- log.debug("[DBMedia] DB Fetching completed")
    } yield media

    override def incrementMediaCount(filename: String): F[Unit] = for {
      _        <- log.debug(s"DB increment media count for $filename")
      _        <- dbCache.delete(filename)
      mediaOpt <- getMedia(filename)
      _ <- mediaOpt.fold(log.error(s"[DBMedia] incrementMediaCount no results while fetching $filename"))(media =>
        log.debug(s"[DBMedia] SQL: ${incrementMediaCountQuery(media).sql}") >> incrementMediaCountQuery(media).run
          .transact(transactor)
          .void
      )
    } yield ()

    override def decrementMediaCount(filename: String): F[Unit] = for {
      _        <- log.debug(s"DB decrement media count for $filename")
      _        <- dbCache.delete(filename)
      mediaOpt <- getMedia(filename)
      _ <- mediaOpt.fold(log.error(s"[DBMedia] decrementMediaCount no results while fetching $filename"))(media =>
        log.debug(s"[DBMedia] SQL: ${decrementMediaCountQuery(media).sql}") >> decrementMediaCountQuery(media).run
          .transact(transactor)
          .void
      )
    } yield ()

    override def getMedia(filename: String, cache: Boolean = true): F[Option[DBMediaData]] =
      getMediaInternal[Option[DBMediaData]](
        cacheLookupValue = filename,
        cacheResultHandler = cachedValueOpt =>
          for {
            mediaOpt <- cachedValueOpt
              .filter(_ => cache)
              .flatMap(_.headOption)
              .fold[F[Option[DBMediaData]]](
                log.debug(s"[DBMedia] fetch media for $filename. SQL: ${getMediaQueryByName(filename).sql}") >>
                  getMediaQueryByName(filename).unique
                    .transact(transactor)
                    .map(_.some)
                    .handleErrorWith(e =>
                      log.debug(
                        s"[DBMedia] getMediaQueryByName fetch media for $filename, ${e.getMessage()}"
                      ) >> None.pure
                    )
              )(_.some.pure)
            _ <- mediaOpt
              .filter(_ => cachedValueOpt.fold(true)(_.length != 1))
              .map(media => dbCache.insert(filename, List(media)))
              .getOrElse(Async[F].unit)
          } yield mediaOpt
      )

    override def getRandomMedia(botId: SBotId): F[Option[DBMediaData]] =
      log.debug(s"[DBMedia] getRandomMedia for $botId. SQL: ${getMediaQueryByRandom(botId).sql}") >>
        getMediaQueryByRandom(botId).unique
          .transact(transactor)
          .map(_.some)
          .handleError(_ => none)

    override def getMediaByKind(kind: String, botId: SBotId, cache: Boolean = true): F[List[DBMediaData]] = {
      val cacheKey: String = s"${botId.value}_$kind"
      getMediaInternal[List[DBMediaData]](
        cacheLookupValue = cacheKey,
        cacheResultHandler = cachedValueOpt =>
          for {
            medias <- cachedValueOpt
              .filter(_ => cache)
              .fold(
                log.debug(
                  s"[DBMedia] getMediaByKind for ${botId.value} - $kind. SQL: ${getMediaQueryByKind(kind, botId).sql}"
                ) >>
                  getMediaQueryByKind(kind = kind, botId = botId).to[List].transact(transactor)
                  <* log.debug(s"[DBMedia] getMediaByKind for ${botId.value} - $kind completed")
              )(Async[F].pure)
            _ <-
              if cachedValueOpt.isEmpty then dbCache.insert(cacheKey, medias)
              else Async[F].unit
          } yield medias
      )
    }

    override def getMediaByMediaCount(
        limit: Int = 20,
        botId: Option[SBotId] = None
    ): F[List[DBMediaData]] =
      log.debug(
        s"[DBMedia] getMediaByMediaCount for prefix $botId. SQL: ${getMediaQueryByMediaCount(botId = botId).sql}"
      ) >>
        getMediaQueryByMediaCount(botId = botId)
          .to[List]
          .map(_.take(limit))
          .transact(transactor)

    override def insertMedia(dbMediaData: DBMediaData): F[Unit] =
      log.debug(s"[DBMedia] insertMedia for $dbMediaData. SQL: ${insertSql(dbMediaData).sql}") >>
        insertSql(dbMediaData).run.transact(transactor).void.exceptSql {
          case e if e.getMessage().contains("UNIQUE constraint failed") =>
            log.debug(
              s"[DBMedia] insertMedia unique constraint failed. Update $dbMediaData. SQL: ${updateOnConflictSql(dbMediaData).sql}"
            ) >>
              updateOnConflictSql(dbMediaData).run.transact(transactor).void
          case e =>
            log.error(s"[DBMedia] ERROR insertMedia for $dbMediaData. Error: $e") >>
              MonadCancelThrow[F].raiseError(
                new RuntimeException(s"An error occurred in inserting $dbMediaData with exception: $e")
              )
        }
  }

  def insertSql(dbMediaData: DBMediaData): Update0 =
    sql"INSERT INTO media (media_name, bot_id, kinds, mime_type, media_sources, created_at, media_count) VALUES (${dbMediaData.media_name}, ${dbMediaData.bot_id}, ${dbMediaData.kinds.asJson.noSpaces}, ${dbMediaData.mime_type}, ${dbMediaData.media_sources.asJson.noSpaces}, ${dbMediaData.created_at}, 0);".update

  def updateOnConflictSql(dbMediaData: DBMediaData): Update0 =
    sql"UPDATE media SET kinds = ${dbMediaData.kinds.asJson.noSpaces}, media_sources = ${dbMediaData.media_sources.asJson.noSpaces} WHERE media_name = ${dbMediaData.media_name};".update

  def getMediaQueryByName(resourceName: String): Query0[DBMediaData] =
    sql"SELECT media_name, bot_id, kinds, mime_type, media_sources, media_count, created_at FROM media WHERE media_name = $resourceName"
      .query[DBMediaData]

  def getMediaQueryByKind(kind: String, botId: SBotId): Query0[DBMediaData] = {
    val kindLike1 = s""""[\\"${kind}\\"%"""
    val kindLike2 = s"""%\\"${kind}\\"%"""
    val kindLike3 = s"""%\\"${kind}\\"]""""
    (fr"SELECT media_name, bot_id, kinds, mime_type, media_sources, media_count, created_at FROM media" ++
      Fragments.whereAnd(
        fr"bot_id = ${botId.value}",
        Fragments.or(
          fr"""kinds LIKE $kindLike1""",
          fr"""kinds LIKE $kindLike2""",
          fr"""kinds LIKE $kindLike3"""
        )
      )).query[DBMediaData]
  }

  def getMediaQueryByMediaCount(botId: Option[SBotId]): Query0[DBMediaData] = {
    val q: Fragment =
      fr"SELECT media_name, bot_id, kinds, mime_type, media_sources, media_count, created_at FROM media" ++
        Fragments.whereAndOpt(botId.map(s => fr"bot_id = ${s.value}")) ++
        fr"ORDER BY media_count DESC"

    q.query[DBMediaData]
  }
  def getMediaQueryByRandom(botId: SBotId): Query0[DBMediaData] =
    sql"SELECT media_name, bot_id, kinds, mime_type, media_sources, media_count, created_at FROM media WHERE bot_id = ${botId.value} ORDER BY RANDOM() LIMIT 1"
      .query[DBMediaData]
  def incrementMediaCountQuery(media: DBMediaData): Update0 =
    sql"UPDATE media SET media_count = ${media.media_count + 1} WHERE media_name = ${media.media_name}".update

  def decrementMediaCountQuery(media: DBMediaData): Update0 =
    sql"UPDATE media SET media_count = ${media.media_count - 1} WHERE media_name = ${media.media_name}".update
}
