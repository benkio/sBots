package com.benkio.botDB

import cats.effect.*
import cats.effect.kernel.Async
import cats.implicits.*
import cats.syntax.all.*
import com.benkio.botDB.config.Config
import com.benkio.botDB.show.YouTubeRequests
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import io.circe.parser.*
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter

import java.io.File
import java.io.InputStream
import scala.io.Source
import scala.jdk.CollectionConverters.*

object CaptionMain extends IOApp {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  val youtubeTokenFilename = "youTubeApiKey.token"

  def run(args: List[String]): IO[ExitCode] = {
    val program = for {
      config <- Resource.eval(Config.loadConfig(args.headOption))
      resourceAccess = ResourceAccess.fromResources[IO](args.lastOption)
      youTubeApiKey          <- BotSetup.token(youtubeTokenFilename, resourceAccess)
      googleNetHttpTransport <- Resource.eval(Async[IO].delay(GoogleNetHttpTransport.newTrustedTransport()))
      jsonFactory = GsonFactory.getDefaultInstance()
      youTubeService <- Resource.eval(
        Async[IO].delay(
          YouTube
            .Builder(
              googleNetHttpTransport,
              jsonFactory,
              new HttpRequestInitializer() {
                override def initialize(request: HttpRequest): Unit = ()
              }
            )
            .setApplicationName(config.showConfig.applicationName)
            .build()
        )
      )
      storedDBShowDatas <- Resource.eval(getStoredDbShowDatas(config))
      target = storedDBShowDatas.head
      _      = println(s"[CaptionMain] target: $target")
      youTubeVideoCaptionRequest <- Resource.eval(
        YouTubeRequests.createYouTubeVideoCaptionRequest[IO](
          youTubeService,
          target.show_id,
          youTubeApiKey
        )
      )
      youTubeVideoCaptionResponse <- Resource.eval(Async[IO].delay(youTubeVideoCaptionRequest.execute()))
      _         = println(s"[CaptionMain] youtubeResponse: $youTubeVideoCaptionResponse")
      captionId = youTubeVideoCaptionResponse.getItems().asScala.head.getId()
      _         = println(s"[CaptionMain] captionId: $captionId")

      youTubeDownloadVideoCaptionRequest <- Resource.eval(
        YouTubeRequests.createYouTubeDownloadVideoCaptionRequest[IO](
          youTubeService,
          captionId,
          youTubeApiKey
        )
      )
      youTubeDownloadVideoCaptionResponseInputStream <- Resource.eval(
        Async[IO].delay(youTubeDownloadVideoCaptionRequest.executeMediaAsInputStream())
      )
      youTubeDownloadVideoCaptionResponse = convertInputStreamToString(youTubeDownloadVideoCaptionResponseInputStream)
      _ = println(s"[CaptionMain] youtubeResponse download: $youTubeDownloadVideoCaptionResponse")
    } yield ()

    program.use_.as(ExitCode.Success)
  }

  // Copy pasted from the show updater
  def getStoredDbShowDatas(config: Config): IO[List[DBShowData]] = {
    val showFilesResource: Resource[IO, List[File]] =
      config.showConfig.showSources
        .traverse(showSource =>
          Resource.make(Async[IO].delay(File(showSource.outputFilePath)))(f =>
            LogWriter.info(s"[ShowUpdater] Closing file $f")
          )
        )
    val deleteFiles: IO[List[DBShowData]] =
      showFilesResource.use(showFiles =>
        for {
          _ <- LogWriter.info(s"[ShowUpdater] ✓ Dry Run. Delete show files: $showFiles")
          _ <- showFiles.traverse(showFile => Async[IO].delay(showFile.delete))
        } yield List.empty
      )
    val getStoredDbShowDatas: IO[List[DBShowData]] =
      showFilesResource.use(showFiles =>
        LogWriter.info("[ShowUpdater] ❌ Dry Run. read show files: $showFiles") >>
          showFiles.flatTraverse(showFile =>
            for {
              _                   <- LogWriter.info(s"[ShowUpdater] Parse show file content: $showFile")
              showFileContentJson <- Async[IO].fromEither(parse(Source.fromFile(showFile).mkString))
              dbShowDatas         <- Async[IO].fromEither(showFileContentJson.as[List[DBShowData]])
            } yield dbShowDatas
          )
      )
    if config.showConfig.dryRun
    then deleteFiles
    else getStoredDbShowDatas
  }

  def convertInputStreamToString(inputStream: InputStream): String = {
    val source = Source.fromInputStream(inputStream)
    try {
      source.mkString
    } finally {
      source.close()
    }
  }
}
