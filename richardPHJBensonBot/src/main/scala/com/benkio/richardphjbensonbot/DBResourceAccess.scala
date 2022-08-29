package com.benkio.richardphjbensonbot

import cats.effect._
import cats.effect.kernel.Outcome._
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

    private def fiberOutcomeHandling(outcome: Outcome[F, Throwable, File], name: String, url: String): F[File] =
      outcome match {
        case Canceled()     => Async[F].raiseError(new RuntimeException("Cancellation not supported"))
        case Errored(e)     => log.info(s"Error occurred fetching the ${name} from ${url} ") *> Async[F].raiseError(e)
        case Succeeded(fio) => fio
      }

    def getResourceByteArray(resourceName: String): Resource[F, Array[Byte]] =
      Resource.eval(for {
        name_url <- log.info(s"DB fetching $resourceName") *> getUrlByName(resourceName).unique.transact(transactor)
        fiber    <- urlFetcher.fetch(name_url._1, name_url._2)
        file     <- fiber.join.flatMap(outcome => fiberOutcomeHandling(outcome, name_url._1, name_url._2))
      } yield Files.readAllBytes(file.toPath))
    def getResourcesByKind(criteria: String): Resource[F, List[File]] =
      Resource.eval(for {
        contents <- getUrlByKind(criteria).stream.compile.toList.transact(transactor)
        filesIO <- contents.traverse { case (filename, url) =>
          urlFetcher.fetch(filename, url).map(_.join.flatMap(outcome => fiberOutcomeHandling(outcome, filename, url)))
        }
        files <- filesIO.sequence
      } yield files)
  }
}
