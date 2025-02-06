package com.benkio.botDB.show

import cats.effect.implicits.*
import cats.effect.kernel.Async
import cats.effect.Resource
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import io.circe.parser.*
import io.circe.syntax.*
import log.effect.LogWriter

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
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

    private def dbShowDataFromFile(file: File): F[List[DBShowData]] =
      for
        _           <- LogWriter.info(s"[ShowFetcher] show file exists at ${file.toPath().toAbsolutePath()}")
        fileContent <- fileToString(file)
        dbShowData  <- Async[F].fromEither(decode[List[DBShowData]](fileContent))
      yield dbShowData

    private def dbShowDataFromYoutube(source: ShowSource): F[List[DBShowData]] =
      val path = Path.of(source.outputFilePath)
      for {
        _ <- LogWriter.info(s"[ShowFetcher] fetch show file for $source into ${path.toAbsolutePath()}")
        dbShowDatass <- source.youtubeSources.parTraverse(youtubeSource =>
          for
            _ <- LogWriter.info(s"[ShowFetcher] start fetching $youtubeSource for $source")
            youtubeResourceFile = fetchJson(youtubeSource)
            _ <- LogWriter.info(s"[ShowFetcher] fetch completed for $youtubeSource for $source")
            result <- youtubeResourceFile.use(inputFile =>
              LogWriter.info(
                s"[ShowFetcher] filter & parse tarted for $youtubeSource for $source, data to filter ${Files.size(inputFile.toPath())}"
              ) >>
                filterAndParseJson(inputFile, source)
            )
          yield result
        )
        dbShowDatas = dbShowDatass.flatten.distinct
        _ <- Async[F].delay(Files.writeString(path, dbShowDatas.asJson.noSpaces))
      } yield dbShowDatas

    private def fetchJson(source: YoutubeSource): Resource[F, File] =
      Resource
        .make(ResourceAccess.toTempFile(s"${UUID.randomUUID.toString}.json", Array.empty).pure)(f =>
          Async[F].delay(f.delete()).void
        )
        .evalMap(f =>
          Async[F].delay(
            Process(source.toYTDLPCommand).#>(f).!
          ) >> Async[F].pure(f)
        )

    private def filterAndParseJson(sourceJson: File, source: ShowSource): F[List[DBShowData]] =
      for
        inputFileContent <- fileToString(sourceJson)
        json <- Async[F].fromEither(parse(inputFileContent))
        dbShowDatas      <- YoutubeJSONParser.parseYoutube[F](json, source.botName)
      yield dbShowDatas

    private def fileToString(file: File): F[String] =
      Async[F].delay(Files.readAllBytes(file.toPath()).map(_.toChar).mkString)
  }
}
