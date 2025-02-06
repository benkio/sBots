package com.benkio.botDB.show

import cats.effect.implicits.*
import cats.effect.kernel.Async
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import io.circe.parser.decode
import io.circe.syntax.*
import log.effect.LogWriter

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.charset.StandardCharsets
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
        dbShowDatass <-
          if file.exists() && Files.size(file.toPath()) > 100
          then dbShowDataFromFile(file)
          else dbShowDataFromYoutube(source)
      yield dbShowDatass

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

    private def dbShowDataFromFile(file: File): F[List[DBShowData]] =
      for
        _ <- LogWriter.info(s"[ShowFetcher] show file exists at ${file.toPath().toAbsolutePath()}")
        fileContent = Files.readAllBytes(file.toPath()).map(_.toChar).mkString
        dbShowData <- parseJson(fileContent)
      yield dbShowData

    private def dbShowDataFromYoutube(source: ShowSource): F[List[DBShowData]] =
      val path = Path.of(source.outputFilePath)
      for {
        _ <- LogWriter.info(s"[ShowFetcher] fetch show file for $source into ${path.toAbsolutePath()}")
        dbShowDatass <- source.youtubeSources.parTraverse(youtubeSource =>
          for
            youtubeSourceContent         <- fetchJson(youtubeSource)
            youtubeSourceContentFiltered <- filterJson(youtubeSourceContent, youtubeSource, source.botName)
            result                       <- parseJson(youtubeSourceContentFiltered)
          yield result
        )
        dbShowDatas = dbShowDatass.flatten.distinct
        _ <- Async[F].delay(Files.writeString(path, dbShowDatas.asJson.noSpaces))
      } yield dbShowDatas

    private def fetchJson(source: YoutubeSource): F[String] =
      Async[F].delay(
        Process(source.toYTDLPCommand).!!
      )

    private def filterJson(sourceJson: String, youtubeSource: YoutubeSource, botName: String): F[String] =
      val jqCommand = Process(youtubeSource.toJqCommand(botName)).#<(new java.io.ByteArrayInputStream(sourceJson.getBytes(StandardCharsets.UTF_8)))
      Async[F].delay(jqCommand.!!)

    private def parseJson(input: String): F[List[DBShowData]] =
      Async[F].fromEither(decode[List[DBShowData]](input))

  }
}
