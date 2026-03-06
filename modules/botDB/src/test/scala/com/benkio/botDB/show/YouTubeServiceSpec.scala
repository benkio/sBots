package com.benkio.botDB.show

import cats.effect.IO
import com.benkio.botDB.config.Config
import com.benkio.botDB.config.JsonLocation
import com.benkio.botDB.config.ShowConfig
import com.benkio.botDB.config.ShowSourceConfig
import com.benkio.botDB.Logger.given
import com.benkio.chatcore.model.SBotInfo.SBotId
import munit.CatsEffectSuite

class YouTubeServiceSpec extends CatsEffectSuite {

  private val cfg: Config =
    Config(
      driver = "org.sqlite.JDBC",
      dbName = ":memory:",
      url = "jdbc:sqlite::memory:",
      migrationsLocations = List.empty,
      migrationsTable = "FlywaySchemaHistory",
      jsonLocation = List(JsonLocation(botId = "testbot", value = "/tmp")),
      showConfig = ShowConfig(
        showSources = List(
          ShowSourceConfig(
            youtubeSources = List.empty, // avoid any network calls
            botId = "testbot",
            captionLanguage = "it",
            outputFilePath = "/tmp/test_shows.json"
          )
        ),
        runShowFetching = false,
        runShowCaptionFetching = false,
        dryRun = false,
        applicationName = "sBots-test"
      )
    )

  test("YouTubeService.getAllBotNameIds should return one bot entry with empty videoIds when youtubeSources is empty") {
    val expected = List(
      YouTubeBotIds(
        botId = SBotId("testbot"),
        outputFilePath = "/tmp/test_shows.json",
        captionLanguage = "it",
        videoIds = List.empty
      )
    )

    assertIO(
      YouTubeService[IO](config = cfg, youTubeApiKey = "dummy").flatMap(_.getAllBotNameIds),
      expected
    )
  }

  test("YouTubeService.getYouTubeVideos should return empty when input ids are empty") {
    assertIO(
      YouTubeService[IO](config = cfg, youTubeApiKey = "dummy").flatMap(_.getYouTubeVideos(Nil)),
      List.empty
    )
  }
}
