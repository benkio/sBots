package com.benkio.telegrambotinfrastructure.resources.db

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.Media
import doobie._
import doobie.implicits._
import io.chrisdavenport.mules._
import log.effect.LogWriter

import scala.concurrent.duration._

trait DBMedia[F[_]] {
  def getMedia(filename: String, cache: Boolean = true): F[Media]
  def getMediaByKind(kind: String, cache: Boolean = true): F[List[Media]]
  def getMediaByMediaCount(
      limit: Int = 20,
      mediaNamePrefix: Option[String] = None
  ): F[List[Media]]
  def incrementMediaCount(filename: String): F[Unit]
  def decrementMediaCount(filename: String): F[Unit]

  def getMediaQueryByName(resourceName: String): Query0[Media]
  def getMediaQueryByKind(kind: String): Query0[Media]
}

object DBMedia {

  def apply[F[_]: Async](
      transactor: Transactor[F],
  )(implicit log: LogWriter[F]): F[DBMedia[F]] = for {
    dbCache <- MemoryCache.ofSingleImmutableMap[F, String, List[Media]](defaultExpiration =
      TimeSpec.fromDuration(6.hours)
    )
  } yield new DBMediaImpl[F](
    transactor = transactor,
    dbCache = dbCache,
    log = log
  )

  private[telegrambotinfrastructure] class DBMediaImpl[F[_]: Async](
      transactor: Transactor[F],
      dbCache: MemoryCache[F, String, List[Media]],
      log: LogWriter[F]
  ) extends DBMedia[F] {

    def getMediaQueryByName(resourceName: String): Query0[Media] =
      sql"SELECT media_name, kind, media_url, media_count, created_at FROM media WHERE media_name = $resourceName"
        .query[Media]

    def getMediaQueryByKind(kind: String): Query0[Media] =
      sql"SELECT media_name, kind, media_url, media_count, created_at FROM media WHERE kind = $kind".query[Media]

    def getMediaByMediaCountQuery(mediaNamePrefix: Option[String], limit: Int): Query0[Media] = {
      def query(whereClause: Fragment) =
        sql"SELECT media_name, kind, media_url, media_count, created_at FROM media $whereClause ORDER BY media_count DESC LIMIT $limit"
          .query[Media]
      mediaNamePrefix.fold(query(Fragment.empty))(prefix => query(s"WHERE media_name LIKE '$prefix%'".fr))
    }

    def incrementMediaCountQuery(media: Media): Update0 =
      sql"UPDATE media SET media_count = ${media.media_count + 1} WHERE media_name = ${media.media_name}".update

    def decrementMediaCountQuery(media: Media): Update0 =
      sql"UPDATE media SET media_count = ${media.media_count - 1} WHERE media_name = ${media.media_name}".update

    private def getMediaInternal[A](
        cacheLookupValue: String,
        cacheResultHandler: Option[List[Media]] => F[A]
    ): F[A] = for {
      _              <- log.info(s"DB fetching media by $cacheLookupValue")
      cachedValueOpt <- dbCache.lookup(cacheLookupValue)
      media          <- cacheResultHandler(cachedValueOpt)
    } yield media

    def incrementMediaCount(filename: String): F[Unit] = for {
      media <- getMedia(filename)
      _     <- incrementMediaCountQuery(media).run.transact(transactor)
    } yield ()

    def decrementMediaCount(filename: String): F[Unit] = for {
      media <- getMedia(filename)
      _     <- decrementMediaCountQuery(media).run.transact(transactor)
    } yield ()

    def getMedia(filename: String, cache: Boolean = true): F[Media] =
      getMediaInternal[Media](
        cacheLookupValue = filename,
        cacheResultHandler = cachedValueOpt =>
          for {
            media <- cachedValueOpt
              .filter(_ => cache)
              .flatMap(_.headOption)
              .fold[F[Media]](
                getMediaQueryByName(filename).unique.transact(transactor)
              )(Async[F].pure)
            _ <-
              if (cachedValueOpt.fold(true)(_.length != 1))
                dbCache.insert(filename, List(media))
              else Async[F].unit
          } yield media
      )

    def getMediaByKind(kind: String, cache: Boolean = true): F[List[Media]] =
      getMediaInternal[List[Media]](
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

    def getMediaByMediaCount(
        limit: Int = 20,
        mediaNamePrefix: Option[String] = None
    ): F[List[Media]] =
      getMediaByMediaCountQuery(limit = limit, mediaNamePrefix = mediaNamePrefix).stream.compile.toList
        .transact(transactor)
  }
}
