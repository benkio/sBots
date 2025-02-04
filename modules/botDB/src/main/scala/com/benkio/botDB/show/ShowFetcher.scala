package com.benkio.botDB.show

import log.effect.LogWriter
import java.nio.file.Files

import java.util.UUID
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import cats.effect.Resource
import cats.effect.kernel.Async
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import io.circe.parser.decode
import java.io.File
import scala.sys.process.*
import scala.sys.process.Process

trait ShowFetcher[F[_]]:
  def generateShowJson(source: ShowSource): F[List[DBShowData]]

object ShowFetcher {
  def apply[F[_]: Async: LogWriter](): ShowFetcher[F] = ShowFetcherImpl[F]()

  private class ShowFetcherImpl[F[_]: Async: LogWriter]() extends ShowFetcher[F] {
    override def generateShowJson(source: ShowSource): F[List[DBShowData]] =
      val file = File(source.outputFilePath)
      for
        _ <- LogWriter.info(s"[ShowFetcher] check dependencies: $shellDependencies")
        _ <- checkDependencies
        _ <-
          if file.exists() && Files.size(file.toPath()) > 100
          then LogWriter.info(s"[ShowFetcher] show file exists at ${file.toPath().toAbsolutePath()}") >> Async[F].unit
          else
            LogWriter.info(
              s"[ShowFetcher] fetch show file for $source into ${file.toPath().toAbsolutePath()}"
            ) >> fetchJson(source).use(filterJson(_, source))
        dbShowDatas <- parseJson(source)
      yield dbShowDatas

    private val shellDependencies: List[String] = List("yt-dlp", "jq")
    private def checkDependencies: F[Unit] =
      shellDependencies
        .traverse_(program =>
          Async[F]
            .delay(s"which $program".!)
            .flatMap(result =>
              Async[F].raiseUnless(result == 0)(
                Throwable(s"[ShowFetcher] error checking dependencies: $program is missing")
              )
            )
        )

    // Download the json from the source in a temp file
    private def fetchJson(source: ShowSource): Resource[F, File] =
      Resource
        .make(ResourceAccess.toTempFile(s"${UUID.randomUUID.toString}.json", Array.empty).pure)(f =>
          Async[F].delay(f.delete()).void
        )
        .evalMap(f =>
          Async[F].delay(
            Process(source.toYTDLPCommand).#>(f).!
          ) >> Async[F].pure(f)
        )

    private def filterJson(sourceJson: File, source: ShowSource): F[Unit] =
      val jqCommand = Process(source.toJqCommand).#<(sourceJson).#>(File(source.outputFilePath))
      Async[F].delay(jqCommand.!!).void

    private def parseJson(source: ShowSource): F[List[DBShowData]] =
      val input = Files.readAllBytes(File(source.outputFilePath).toPath()).map(_.toChar).mkString
      Async[F].fromEither(
        decode[List[DBShowData]](
          input
        )
      )
  }
}
