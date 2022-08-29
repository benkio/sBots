package com.benkio.richardphjbensonbot

import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.ResourceAccess
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

  private class DBResourceAccess[F[_]: Async](
      transactor: Transactor[F],
      urlFetcher: UrlFetcher[F],
      log: LogWriter[F]
  ) extends ResourceAccess[F] {

    private def getUrlByName(resourceName: String): Query0[(String, String)] =
      sql"SELECT media_name, media_url FROM media WHERE media_name = $resourceName".query[(String, String)]

    private def getUrlByKind(kind: String): Query0[(String, String)] =
      sql"SELECT media_name, media_url FROM media WHERE kind = $kind".query[(String, String)]

    def getResourceByteArray(resourceName: String): Resource[F, Array[Byte]] =
      Resource.eval(for {
        name_url <- log.info(s"DB fetching $resourceName") *> getUrlByName(resourceName).unique.transact(transactor)
        content  <- urlFetcher.fetch(name_url._1, name_url._2).map(f => Files.readAllBytes(f.toPath))
      } yield content)
    def getResourcesByKind(criteria: String): Resource[F, List[File]] =
      Resource.eval(
        getUrlByKind(criteria).stream.compile.toList
          .transact(transactor)
          .flatMap(contents =>
            contents.traverse { case (fileName, url) =>
              urlFetcher.fetch(fileName, url)
            }
          )
      )

  }
}
