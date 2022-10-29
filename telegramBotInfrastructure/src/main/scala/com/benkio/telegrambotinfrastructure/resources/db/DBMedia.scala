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
  def getMedia(filename: String): F[Media]
  def getMediaByKind(kind: String): F[List[Media]]

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

    private def getMediaInternal[A](
        cacheLookupValue: String,
        cacheResultHandler: Option[List[Media]] => F[A]
    ): F[A] = for {
      _              <- log.info(s"DB fetching media by $cacheLookupValue")
      cachedValueOpt <- dbCache.lookup(cacheLookupValue)
      media          <- cacheResultHandler(cachedValueOpt)
    } yield media

    def getMedia(filename: String): F[Media] =
      getMediaInternal[Media](
        cacheLookupValue = filename,
        cacheResultHandler = cachedValueOpt =>
          for {
            media <- cachedValueOpt
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

    def getMediaByKind(kind: String): F[List[Media]] =
      getMediaInternal[List[Media]](
        cacheLookupValue = kind,
        cacheResultHandler = cachedValueOpt =>
          for {
            medias <- cachedValueOpt.fold(
              getMediaQueryByKind(kind).stream.compile.toList.transact(transactor)
            )(Async[F].pure)
            _ <-
              if (cachedValueOpt.isEmpty)
                dbCache.insert(kind, medias)
              else Async[F].unit
          } yield medias
      )
  }
}
