package com.benkio.integration.integrationmunit.botDB

import cats.effect.IO
import com.benkio.botDB.config.Config
import com.benkio.botDB.show.YouTubeBotIds
import com.benkio.botDB.show.YouTubeService
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Paths
import scala.jdk.CollectionConverters.*

class ITYoutubeServiceSpec extends CatsEffectSuite with Constants {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  def buildYoutubeService: IO[YouTubeService[IO]] = {
    val testApplicationConfPath = s"$integrationResourcesPath$testApplicationConf"

    for {
      config <- Config.loadConfig(Some(testApplicationConfPath))
      youTubeApiKey = Files.readAllLines(Paths.get(youTubeTokenFilenamePath)).asScala.headOption.get
      youTubeService <- YouTubeService(
        config = config,
        youTubeApiKey = youTubeApiKey
      )
    } yield youTubeService
  }
  val ciEnvVar = sys.env.get("CI")

  test("YoutubeService.getAllBotNameIds should return the expected YoutubeBotIds") {
    // Run only locally because it needs Youtube API. You don't want the CI to run requests
    assume(ciEnvVar.contains("false") || ciEnvVar.isEmpty)

    for {
      youTubeService <- buildYoutubeService
      receivedIds    <- youTubeService.getAllBotNameIds
    } yield assertEquals(
      receivedIds,
      List(
        YouTubeBotIds(
          botName = "testBot",
          outputFilePath = "../integration-tests/src/test/resources/testdata/testBotShow.json",
          captionLanguage = "it",
          videoIds = List(
            "95m8ztdbW0E",
            "MjxGbpqSXZQ",
            "HnYEMgvoick",
            "et1JhUovCn4",
            "5v6_z-f5A8o"
          )
        )
      )
    )
  }

  test("YoutubeService.getYouTubeVideos should return the expected video") {
    // Run only locally because it needs Youtube API. You don't want the CI to run requests
    assume(ciEnvVar.contains("false") || ciEnvVar.isEmpty)

    for {
      youTubeService <- buildYoutubeService
      videos         <- youTubeService.getYouTubeVideos(List("fcCa8ZUTpJ0", "Ql6QA1EnL4k"))
    } yield assertEquals(
      videos.map(_.toString),
      List(
        """{"contentDetails":{"duration":"PT51M16S"},"id":"fcCa8ZUTpJ0","snippet":{"description":"#RichardBenson #RockMachine","publishedAt":"2019-01-21T14:38:07.000Z","title":"Richard Benson in Rock Machine (30 ottobre 2012)"}}""",
        """{"contentDetails":{"duration":"PT53M42S"},"id":"Ql6QA1EnL4k","snippet":{"description":"#RichardBenson #RockMachine","publishedAt":"2019-01-22T17:24:44.000Z","title":"Richard Benson in Rock Machine (9 ottobre 2012)"}}"""
      )
    )
  }
}
