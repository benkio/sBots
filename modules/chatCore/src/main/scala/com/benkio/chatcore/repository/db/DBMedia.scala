package com.benkio.chatcore.repository.db

import cats.effect.*
import cats.effect.syntax.monadCancel.*
import cats.implicits.*
import com.benkio.chatcore.model.media.Media
import com.benkio.chatcore.model.media.MediaSource
import com.benkio.chatcore.model.media.MediaSources
import com.benkio.chatcore.model.MimeTypeOps.given
import com.benkio.chatcore.model.SBotInfo.SBotId
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

  /** Adds a file reference returned by a successful send to the sources of `filename`, scoped to `platform`.
    * Implementations that cannot persist sources still have to override it as a no-op.
    */
  def registerFileId(filename: String, platform: com.benkio.chatcore.model.Platform, fileId: String)(using
      cats.Applicative[F]
  ): F[Unit]

  /** Replaces a failed file reference with the fresh one in a single update of `media_sources`. */
  def replaceFileId(
      filename: String,
      platform: com.benkio.chatcore.model.Platform,
      failedId: String,
      newId: String
  )(using cats.Applicative[F]): F[Unit]

  // For test purposes only
  def getAllMedia(botId: Option[SBotId] = None): F[List[DBMediaData]]
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

  private[chatcore] class DBMediaImpl[F[_]: Async](
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
            _ <- dbCache.insert(cacheKey, medias).whenA(cachedValueOpt.isEmpty)
          } yield medias
      )
    }

    override def getMediaByMediaCount(
        limit: Int = 20,
        botId: Option[SBotId] = None
    ): F[List[DBMediaData]] =
      log.debug(
        s"[DBMedia] getMediaByMediaCount for prefix $botId. SQL: ${getMediaQueryByMediaCount(limit = limit, botId = botId).sql}"
      ) >>
        getMediaQueryByMediaCount(limit = limit, botId = botId)
          .to[List]
          .transact(transactor)

    override def getAllMedia(botId: Option[SBotId] = None): F[List[DBMediaData]] =
      log.debug(
        s"[DBMedia] getAllMedia for prefix $botId. SQL: ${getAllMediaQuery(botId = botId).sql}"
      ) >>
        getAllMediaQuery(botId = botId)
          .to[List]
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

    /** Shared read-modify-write for both file-id APIs, atomic against concurrent writers: the row is read and updated
      * in one transaction, and the update only applies if `media_sources` is still what was read (compare-and-set).
      * When another writer got there first the whole step is retried on the fresh row. `operation` is only used for
      * logging.
      */
    private def updateFileIds(
        filename: String,
        platform: com.benkio.chatcore.model.Platform,
        operation: String
    )(transform: List[MediaSource] => List[MediaSource]): F[Unit] = {
      val maxAttempts = 10

      def step: ConnectionIO[FileIdUpdate] =
        getMediaQueryByName(filename).option.flatMap {
          case None          => FileIdUpdate.NotFound.pure[ConnectionIO].widen
          case Some(dbMedia) =>
            for {
              media <- Media(dbMedia).liftTo[ConnectionIO]
              sources = transform(media.mediaSources)
              result <-
                if sources == media.mediaSources then FileIdUpdate.Unchanged.pure[ConnectionIO].widen
                else
                  updateMediaSourcesIfUnchangedQuery(
                    dbMediaData = DBMediaData(media.copy(mediaSources = sources)),
                    previousSources = dbMedia.media_sources
                  ).run.map(rows =>
                    if rows == 1 then FileIdUpdate.Updated(sources, Media.droppedSources(dbMedia))
                    else FileIdUpdate.Conflict
                  )
            } yield result
        }

      // SQLite does not wait for a lock upgrade of a concurrent transaction: it fails at once with SQLITE_BUSY.
      def isBusy(e: Throwable): Boolean = e match {
        case sql: java.sql.SQLException =>
          sql.getErrorCode == 5 || Option(sql.getMessage).exists(_.contains("SQLITE_BUSY"))
        case _ => false
      }

      def attempt(left: Int): F[Unit] =
        step.transact(transactor).recover { case e if isBusy(e) => FileIdUpdate.Conflict }.flatMap {
          case FileIdUpdate.NotFound                  => log.warn(s"[DBMedia] $operation no media found for $filename")
          case FileIdUpdate.Unchanged                 => Async[F].unit
          case FileIdUpdate.Updated(sources, dropped) =>
            Async[F].whenA(dropped.nonEmpty)(
              log.warn(s"[DBMedia] $operation for $filename removed the stored entries that are not a source: $dropped")
            ) >> log.debug(s"[DBMedia] $operation $platform for $filename: $sources")
          case FileIdUpdate.Conflict if left > 1 =>
            log.debug(s"[DBMedia] $operation $platform for $filename: concurrent update, retrying") >>
              Async[F].sleep((10 * (maxAttempts - left + 1)).millis) >> attempt(left - 1)
          case FileIdUpdate.Conflict =>
            Async[F].raiseError(
              new RuntimeException(
                s"[DBMedia] $operation $platform for $filename: gave up after $maxAttempts conflicts"
              )
            )
        }

      dbCache.delete(filename) >> attempt(maxAttempts).guarantee(dbCache.delete(filename))
    }

    override def registerFileId(filename: String, platform: com.benkio.chatcore.model.Platform, fileId: String)(using
        cats.Applicative[F]
    ): F[Unit] =
      updateFileIds(filename = filename, platform = platform, operation = "registerFileId")(
        MediaSources.withFileId(_, platform, fileId)
      )

    override def replaceFileId(
        filename: String,
        platform: com.benkio.chatcore.model.Platform,
        failedId: String,
        newId: String
    )(using cats.Applicative[F]): F[Unit] =
      updateFileIds(filename = filename, platform = platform, operation = "replaceFileId")(
        MediaSources.replaceFileId(_, platform, failedId, newId)
      )
  }

  def insertSql(dbMediaData: DBMediaData): Update0 =
    sql"INSERT INTO media (media_name, bot_id, kinds, mime_type, media_sources, created_at, media_count) VALUES (${dbMediaData.media_name}, ${dbMediaData.bot_id}, ${dbMediaData.kinds.asJson.noSpaces}, ${dbMediaData.mime_type}, ${dbMediaData.media_sources.asJson.noSpaces}, ${dbMediaData.created_at}, 0);".update

  def updateOnConflictSql(dbMediaData: DBMediaData): Update0 =
    sql"UPDATE media SET kinds = ${dbMediaData.kinds.asJson.noSpaces}, media_sources = ${dbMediaData.media_sources.asJson.noSpaces} WHERE media_name = ${dbMediaData.media_name};".update

  /** Updates only the sources, and only if the column still holds `previousSources` is the raw column value that was
    * read.
    */
  def updateMediaSourcesIfUnchangedQuery(dbMediaData: DBMediaData, previousSources: String): Update0 =
    sql"UPDATE media SET media_sources = ${dbMediaData.media_sources.asJson.noSpaces} WHERE media_name = ${dbMediaData.media_name} AND media_sources = $previousSources;".update

  private enum FileIdUpdate {
    case NotFound, Unchanged, Conflict
    case Updated(sources: List[MediaSource], dropped: List[String])
  }

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

  def getMediaQueryByMediaCount(limit: Int, botId: Option[SBotId]): Query0[DBMediaData] = {
    val q: Fragment =
      fr"SELECT media_name, bot_id, kinds, mime_type, media_sources, media_count, created_at FROM media" ++
        Fragments.whereAndOpt(botId.map(s => fr"bot_id = ${s.value}")) ++
        fr" ORDER BY media_count DESC" ++
        fr" LIMIT ${limit.toString}"

    q.query[DBMediaData]
  }

  def getAllMediaQuery(botId: Option[SBotId]): Query0[DBMediaData] = {
    val q: Fragment =
      fr"SELECT media_name, bot_id, kinds, mime_type, media_sources, media_count, created_at FROM media" ++
        Fragments.whereAndOpt(botId.map(s => fr"bot_id = ${s.value}"))

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
