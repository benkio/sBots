package com.benkio.telegrambotinfrastructure.web

import cats.effect.*
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite
import org.http4s.ember.client.*
import org.http4s.Uri

class InvidiousClientSpec extends CatsEffectSuite {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  def buildInvidiousClient(): Resource[IO, InvidiousClient[IO]] =
    EmberClientBuilder
      .default[IO]
      .withMaxResponseHeaderSize(8192)
      .build
      .map(httpClient =>
        InvidiousClient[IO](
          httpClient = httpClient,
          invidiousInstance = Uri.unsafeFromString("https://localhost")
        )
      )

  test("InvidiousClient.fetchChannelVideos should return the expected jsons if the uri is valid") { ??? }
  test("InvidiousClient.fetchChannelStreams should return the expected jsons if the uri is valid") { ??? }
  test("InvidiousClient.fetchPlailistVideos should return the expected jsons if the uri is valid") { ??? }
}
