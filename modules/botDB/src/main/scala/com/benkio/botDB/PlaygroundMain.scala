package com.benkio.botDB

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter

import scala.jdk.CollectionConverters.*

object PlaygroundMain extends IOApp {

  // Example Reference
  // https://github.com/youtube/api-samples/blob/master/java/src/main/java/com/google/api/services/samples/youtube/cmdline/data/Search.java

  // TODO: fill
  val apiKeys         = "IGIOT"
  val applicationName = "8CAHB"

  val log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  private def createYouTube: IO[YouTube] =
    for {
      _                      <- log.info("[PlaygroundMain] Create NetHttpTransport and jsonFactory for YouTube")
      googleNetHttpTransport <- IO(GoogleNetHttpTransport.newTrustedTransport())
      jsonFactory = GsonFactory.getDefaultInstance()
      _ <- log.info("[PlaygroundMain] Create youtube")
      youtubeService <- IO(
        YouTube
          .Builder(
            googleNetHttpTransport,
            jsonFactory,
            new HttpRequestInitializer() {
              override def initialize(request: HttpRequest): Unit = ()
            }
          )
          .setApplicationName(applicationName)
          .build()
      )
    } yield youtubeService

  private def createYouTubeVideoRequest(youtubeService: YouTube, videoIds: List[String]): IO[YouTube#Videos#List] =
    for {
      _ <- log.info(s"[PlaygroundMain] Create a YouTube Video request for $videoIds")
      request <- IO(
        youtubeService
          .videos()
          .list(List("id", "snippet", "contentDetails").asJava)
          .setKey(apiKeys)
          .setId(videoIds.asJava)
      )
    } yield request

  def run(args: List[String]): IO[ExitCode] =
    for
      _                    <- log.info("[PlaygroundMain] Start the PlaygroundMain")
      youtubeService       <- createYouTube
      youtubeVideoRequest  <- createYouTubeVideoRequest(youtubeService, List("JInL-_qi8eA"))
      _                    <- log.info(s"[PlaygroundMain] Execute Youtube video request: $youtubeVideoRequest")
      youtubeVideoResponse <- IO(youtubeVideoRequest.execute())
      _                    <- log.info(s"[PlaygroundMain] Youtube video response: $youtubeVideoResponse")
    yield ExitCode.Success
}
