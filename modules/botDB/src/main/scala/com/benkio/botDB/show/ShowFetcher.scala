package com.benkio.botDB.show

import cats.effect.implicits.*
import cats.effect.kernel.Async
import cats.effect.Resource
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.web.JsonParser
import io.circe.parser.*
import io.circe.syntax.*
import log.effect.LogWriter

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import scala.sys.process.*

trait ShowFetcher[F[_]]:
  def generateShowJson(source: ShowSource): F[List[DBShowData]]

object ShowFetcher {
  def apply[F[_]: Async: LogWriter](): ShowFetcher[F] = ShowFetcherImpl[F]()

  private class ShowFetcherImpl[F[_]: Async: LogWriter]() extends ShowFetcher[F] {
    override def generateShowJson(source: ShowSource): F[List[DBShowData]] =
      for
        _           <- LogWriter.info(s"[ShowFetcher] check dependencies: $shellDependencies")
        _           <- checkDependencies
        _           <- LogWriter.info(s"[ShowFetcher] get shows for: $source")
        fileContent <- ResourceAccess.fileToString(File(source.outputFilePath)).use(_.pure[F])
        dbShowDatass <-
          if fileContent.length > 1000
          then dbShowDataFromFile(fileContent)
          else dbShowDataFromYoutube(source)
      yield dbShowDatass

    private val shellDependencies: List[String] = List("yt-dlp")
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

    private def dbShowDataFromFile(fileContent: String): F[List[DBShowData]] =
      for
        _          <- LogWriter.info(s"[ShowFetcher] show file exists with content: ${fileContent.take(100)}...")
        dbShowData <- Async[F].fromEither(decode[List[DBShowData]](fileContent))
      yield dbShowData

    private def dbShowDataFromYoutube(source: ShowSource): F[List[DBShowData]] =
      val path = Path.of(source.outputFilePath)
      for {
        _ <- LogWriter.info(s"[ShowFetcher] fetch show file for $source into ${path.toAbsolutePath()}")
        dbShowDatass <- source.youtubeSources.parTraverse(youtubeSource =>
          (for
            _ <- LogWriter.info(s"[ShowFetcher] start fetching $youtubeSource for $source")
            youtubeResourceFile = fetchJson(youtubeSource)
            _ <- LogWriter.info(s"[ShowFetcher] fetch completed for $youtubeSource for $source")
            result <- youtubeResourceFile.use(inputFile =>
              LogWriter.info(
                s"[ShowFetcher] filter & parse started for $youtubeSource for $source, data to filter ${Files.size(inputFile.toPath())}"
              ) >>
                filterAndParseJson(inputFile, source.botName)
            )
          yield result).handleErrorWith(err =>
            LogWriter.error(s"[ShowFetcher] ERROR when computing $source with $err") >> List.empty.pure
          )
        )
        dbShowDatas = dbShowDatass.flatten.distinctBy(_.show_url)
        _ <- LogWriter.info(s"[ShowFetcher] outputFileWritten for $source. Total Shows: ${dbShowDatas.length}")
        _ <- Async[F].delay(Files.writeString(path, dbShowDatas.asJson.noSpaces))
      } yield dbShowDatas

    private def fetchJson(source: YoutubeSource): Resource[F, File] =
      ResourceAccess
        .toTempFile(s"${UUID.randomUUID.toString}.json", Array.empty)
        .evalMap(f =>
          Async[F].delay(
            Process(source.toYTDLPCommand).#>(f).!
          ) >> Async[F].pure(f)
        )
  }

  def filterAndParseJson[F[_]: Async: LogWriter](sourceJson: File, botName: String): F[List[DBShowData]] =
    ResourceAccess
      .fileToString(sourceJson)
      .use(inputFileContent =>
        for
          json <- Async[F].fromEither(parse(inputFileContent))
          dbShowDatas <- JsonParser.Ytdlp
            .parseYtdlp[F](json, botName)
            .handleErrorWith(err =>
              LogWriter.error(s"[ShowFetcher] ERROR during parsing of $botName with $err") >>
                Async[F].delay(
                  Files
                    .writeString(
                      Path.of(s"ERR-${scala.util.Random.nextInt()}-${sourceJson.getName()}"),
                      inputFileContent
                    )
                ) >>
                List.empty.pure
            )
        yield dbShowDatas
      )
}
