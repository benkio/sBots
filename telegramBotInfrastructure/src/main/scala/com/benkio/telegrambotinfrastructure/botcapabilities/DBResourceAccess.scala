package com.benkio.telegrambotinfrastructure.botcapabilities

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import log.effect.LogWriter

import java.io.File
import java.nio.file.Files

object DBResourceAccess {

  def apply[F[_]: Async](
      transactor: Transactor[F],
      urlFetcher: UrlFetcher[F]
  )(implicit log: LogWriter[F]): ResourceAccess[F] =
    new DBResourceAccess[F](
      transactor,
      urlFetcher,
      log
    )

  private[telegrambotinfrastructure] class DBResourceAccess[F[_]: Async](
      transactor: Transactor[F],
      urlFetcher: UrlFetcher[F],
      log: LogWriter[F]
  ) extends ResourceAccess[F] {

    def getUrlByName(resourceName: String): Query0[(String, String)] =
      sql"SELECT media_name, media_url FROM media WHERE media_name = $resourceName".query[(String, String)]

    private def getUrlByKind(kind: String): Query0[(String, String)] =
      sql"SELECT media_name, media_url FROM media WHERE kind = $kind".query[(String, String)]

    def getResourceByteArray(resourceName: String): Resource[F, Array[Byte]] =
      for {
        name_url <- Resource.eval(
          log.info(s"DB fetching $resourceName") *> getUrlByName(resourceName).unique.transact(transactor)
        )
        file <- urlFetcher.fetchFromDropbox(name_url._1, name_url._2)
      } yield Files.readAllBytes(file.toPath)
    def getResourcesByKind(criteria: String): Resource[F, List[File]] =
      for {
        contents <- Resource.eval(getUrlByKind(criteria).stream.compile.toList.transact(transactor))
        files <- contents.traverse { case (filename, url) =>
          urlFetcher.fetchFromDropbox(filename, url)
        }
      } yield files
  }
}