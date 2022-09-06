package com.benkio.telegrambotinfrastructure.botcapabilities

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import io.chrisdavenport.mules._
import log.effect.LogWriter

import java.io.File
import java.nio.file.Files
import scala.concurrent.duration._

object DBResourceAccess {

  def apply[F[_]: Async](
      transactor: Transactor[F],
      urlFetcher: UrlFetcher[F]
  )(implicit log: LogWriter[F]): F[ResourceAccess[F]] = for {
    dbCache <- MemoryCache.ofSingleImmutableMap[F, String, List[(String, String)]](defaultExpiration =
      TimeSpec.fromDuration(6.hours)
    )
  } yield new DBResourceAccess[F](
    transactor = transactor,
    urlFetcher = urlFetcher,
    dbCache = dbCache,
    log = log
  )

  private[telegrambotinfrastructure] class DBResourceAccess[F[_]: Async](
      transactor: Transactor[F],
      urlFetcher: UrlFetcher[F],
      dbCache: MemoryCache[F, String, List[(String, String)]],
      log: LogWriter[F]
  ) extends ResourceAccess[F] {

    def getUrlByName(resourceName: String): Query0[(String, String)] =
      sql"SELECT media_name, media_url FROM media WHERE media_name = $resourceName".query[(String, String)]

    private def getUrlByKind(kind: String): Query0[(String, String)] =
      sql"SELECT media_name, media_url FROM media WHERE kind = $kind".query[(String, String)]

    def getResourceByteArray(resourceName: String): Resource[F, Array[Byte]] =
      for {
        name_url <- Resource.eval(for {
          _              <- log.info(s"DB fetching by name $resourceName")
          cachedValueOpt <- dbCache.lookup(resourceName)
          value <- cachedValueOpt
            .flatMap(_.headOption)
            .fold[F[(String, String)]](
              getUrlByName(resourceName).unique.transact(transactor)
            )(name_url => Async[F].pure(name_url))
          _ <-
            if (cachedValueOpt.fold(true)(_.length != 1))
              dbCache.insert(resourceName, List(value))
            else Async[F].unit
        } yield value)
        file <- urlFetcher.fetchFromDropbox(name_url._1, name_url._2)
      } yield Files.readAllBytes(file.toPath)
    def getResourcesByKind(criteria: String): Resource[F, List[File]] =
      for {
        contents <- Resource.eval(for {
          _              <- log.info(s"DB fetching By kind $criteria")
          cachedValueOpt <- dbCache.lookup(criteria)
          values <- cachedValueOpt.fold(
            getUrlByKind(criteria).stream.compile.toList.transact(transactor)
          )(Async[F].pure)
          // insert in cache
          _ <-
            if (cachedValueOpt.isEmpty)
              dbCache.insert(criteria, values)
            else Async[F].unit
        } yield values)
        files <- contents.traverse { case (filename, url) =>
          urlFetcher.fetchFromDropbox(filename, url)
        }
      } yield files
  }
}
