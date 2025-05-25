package com.benkio.telegrambotinfrastructure.web

import cats.effect.*
import io.circe.Json
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.circe.*
import org.http4s.client.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.http4s.Uri

class InvidiousClientSpec extends CatsEffectSuite {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  private def createMockClient(expectedChannelId: String): Client[IO] = {
    val service: HttpApp[IO] = HttpRoutes
      .of[IO] {
        case req @ GET -> Root / "api" / "v1" / "channels" / channelId / "videos" if channelId == expectedChannelId =>
          req.params.get("continuation") match {
            case Some(token) if token == JsonParserSpec.Invidious.continuationValue =>
              Ok(JsonParserSpec.Invidious.videosWithoutContinuation)
            case Some(token) =>
              BadRequest()
            case None =>
              // Default empty JSON response
              Ok(
                JsonParserSpec.Invidious.videosWithContinuation
              )
          }
        case req @ GET -> Root / "api" / "v1" / "videos" / videoId
            if JsonParserSpec.Invidious.videoData.exists((data: (String, Json)) => data._1 == videoId) =>
          Ok(
            JsonParserSpec.Invidious.videoData
              .find { case (vid, _) =>
                vid == videoId
              }
              .get
              ._2
          )
        case req @ GET -> Root / "api" / "v1" / "videos" / videoId =>
          println(
            s"[InvidiousClientSpec] Not Found $videoId - DEBUG: expectedIds ${JsonParserSpec.Invidious.videoData.map(_._1)} - Check ${JsonParserSpec.Invidious.videoData
                .exists((data: (String, Json)) => data._1 == videoId)}"
          )
          NotFound()
      }
      .orNotFound

    Client.fromHttpApp(service)
  }

  def buildInvidiousClient(expectedChannelId: String): InvidiousClient[IO] =
    InvidiousClient[IO](
      httpClient = createMockClient(expectedChannelId),
      invidiousInstance = Uri.unsafeFromString("https://localhost")
    )

  test("InvidiousClient.fetchChannelVideos should return the expected jsons if the uri is valid") {
    val channelId       = "BEVan"
    val invidiousClient = buildInvidiousClient(channelId)
    val actual =
      invidiousClient.fetchChannelVideos(Uri.unsafeFromString(s"https://localhost/api/v1/channels/$channelId/videos"))
    val expected = JsonParserSpec.Invidious.videoData.map(_._2)
    assertIO(actual, expected)
  }
  test("InvidiousClient.fetchChannelVideos should fail the channel id is not found") {
    val channelId       = "BEVan"
    val invidiousClient = buildInvidiousClient(channelId)
    val actual =
      invidiousClient.fetchChannelVideos(Uri.unsafeFromString("https://localhost/api/v1/channels/Y75b4/videos"))
    assertIO(actual.attempt.map(_.isLeft), true)
  }
  // test("InvidiousClient.fetchChannelStreams should return the expected jsons if the uri is valid") { ??? }
  // test("InvidiousClient.fetchPlailistVideos should return the expected jsons if the uri is valid") { ??? }
}
