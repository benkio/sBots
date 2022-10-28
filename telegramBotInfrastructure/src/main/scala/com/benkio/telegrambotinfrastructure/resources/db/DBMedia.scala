package com.benkio.telegrambotinfrastructure.resources.db

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import io.chrisdavenport.mules._
import log.effect.LogWriter

import scala.concurrent.duration._

trait DBMedia[F[_]] {
  def getMedia(filename: String): F[String]
  def getMediaByKind(kind: String): F[List[(String, String)]]

  def getMediaQueryByName(resourceName: String): Query0[String]
  def getMediaQueryByKind(kind: String): Query0[(String, String)]
}

object DBMedia {

  def apply[F[_]: Async](
      transactor: Transactor[F],
  )(implicit log: LogWriter[F]): F[DBMedia[F]] = for {
    dbCache <- MemoryCache.ofSingleImmutableMap[F, String, List[(String, String)]](defaultExpiration =
      TimeSpec.fromDuration(6.hours)
    )
  } yield new DBMediaImpl[F](
    transactor = transactor,
    dbCache = dbCache,
    log = log
  )

  private[telegrambotinfrastructure] class DBMediaImpl[F[_]: Async](
      transactor: Transactor[F],
      dbCache: MemoryCache[F, String, List[(String, String)]],
      log: LogWriter[F]
  ) extends DBMedia[F] {

    def getMediaQueryByName(resourceName: String): Query0[String] =
      sql"SELECT media_url FROM media WHERE media_name = $resourceName".query[String]

    def getMediaQueryByKind(kind: String): Query0[(String, String)] =
      sql"SELECT media_name, media_url FROM media WHERE kind = $kind".query[(String, String)]

    private def getMediaInternal[A](
        cacheLookupValue: String,
        cacheResultHandler: Option[List[(String, String)]] => F[A]
    ): F[A] = for {
      _              <- log.info(s"DB fetching media by $cacheLookupValue")
      cachedValueOpt <- dbCache.lookup(cacheLookupValue)
      value          <- cacheResultHandler(cachedValueOpt)
    } yield value

    def getMedia(filename: String): F[String] =
      getMediaInternal[String](
        cacheLookupValue = filename,
        cacheResultHandler = cachedValueOpt =>
          for {
            value <- cachedValueOpt
              .flatMap(_.headOption)
              .fold[F[String]](
                getMediaQueryByName(filename).unique.transact(transactor)
              ) { case (_, url) => Async[F].pure(url) }
            _ <-
              if (cachedValueOpt.fold(true)(_.length != 1))
                dbCache.insert(filename, List((filename, value)))
              else Async[F].unit
          } yield value
      )

    def getMediaByKind(kind: String): F[List[(String, String)]] =
      getMediaInternal[List[(String, String)]](
        cacheLookupValue = kind,
        cacheResultHandler = cachedValueOpt =>
          for {
            values <- cachedValueOpt.fold(
              getMediaQueryByKind(kind).stream.compile.toList.transact(transactor)
            )(Async[F].pure)
            _ <-
              if (cachedValueOpt.isEmpty)
                dbCache.insert(kind, values)
              else Async[F].unit
          } yield values
      )
  }
}
