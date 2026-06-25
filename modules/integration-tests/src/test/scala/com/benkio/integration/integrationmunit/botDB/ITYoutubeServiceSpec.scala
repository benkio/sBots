package com.benkio.integration.integrationmunit.botDB

import cats.effect.IO
import cats.Semigroup
import com.benkio.botDB.config.Config
import com.benkio.botDB.show.YouTubeBotDBShowDatas
import com.benkio.botDB.show.YouTubeBotIds
import com.benkio.botDB.show.YouTubeService
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBShowData
import com.benkio.integrationtest.Logger.given
import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Paths
import scala.jdk.CollectionConverters.*

class ITYouTubeServiceSpec extends CatsEffectSuite with Constants {

  val youTubeApiKey     = Files.readAllLines(Paths.get(youTubeTokenFilenamePath)).asScala.headOption.get
  val ciEnvVar          = sys.env.get("CI")
  val runTestsCondition =
    (ciEnvVar.contains("false") || ciEnvVar.isEmpty) && youTubeApiKey != "PutYourYouTubeApiKeyHere"

  def buildYoutubeService: IO[YouTubeService[IO]] = {
    val testApplicationConfPath = s"$integrationResourcesPath$testApplicationConf"

    for {
      config         <- Config.loadConfig(Some(testApplicationConfPath))
      youTubeService <- YouTubeService(
        config = config,
        youTubeApiKey = youTubeApiKey
      )
    } yield youTubeService
  }

  test("YoutubeService.getAllBotNameIds should return the expected YoutubeBotIds") {
    // Run only locally because it needs Youtube API. You don't want the CI to run requests
    assume(runTestsCondition)

    for {
      youTubeService <- buildYoutubeService
      receivedIds    <- youTubeService.getAllBotNameIds
    } yield assertEquals(
      receivedIds,
      List(
        YouTubeBotIds(
          botId = SBotId("testbot"),
          outputFilePath = "../integration-tests/src/test/resources/testdata/test_shows.json",
          captionFolderPath = "../integration-tests/src/test/resources/testdata/showCaptions",
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
    assume(runTestsCondition)

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

  test("YouTubeService.saveCaption should return downloaded caption path") {
    assume(runTestsCondition)
    for {
      tempDir <- IO.pure(Files.createTempDirectory(Paths.get("target"), "ytdlpCaptions").toAbsolutePath())
      videoId  = "CQi-0VJJSSs"
      language = "it"
      youTubeService <- buildYoutubeService
      result         <- youTubeService.saveCaption(videoId, tempDir, language)
    } yield assert(result.nonEmpty)
  }
  test(
    "YouTubeBotDBShowDatas.semigroup should properly combine the data from the same bot to a single YouTubeBotDBShowDatas"
  ) {
    val dbShowData1 = DBShowData(
      show_id = "3kKiB",
      bot_id = "testbot",
      show_title = "videoTitle",
      show_upload_date = "2023-05-17T10:24:55.000Z",
      show_duration = 650,
      show_description = Some(
        value = "videoDescription"
      ),
      show_is_live = false,
      show_origin_automatic_caption = None
    )
    val dbShowData2 = DBShowData(
      show_id = "3kKiB",
      bot_id = "testbot",
      show_title = "videoTitle",
      show_upload_date = "2023-05-17T10:24:55.000Z",
      show_duration = 650,
      show_description = Some(
        value = "videoDescription"
      ),
      show_is_live = false,
      show_origin_automatic_caption = None
    )

    val youTubeBotDBShowDatas1 =
      YouTubeBotDBShowDatas(
        botId = SBotId("testbot"),
        outputFilePath = "outputFilePath",
        captionFolderPath = "captionFolderPath",
        captionLanguage = "it",
        dbShowDatas = List(dbShowData1)
      )

    assertEquals(
      Semigroup[YouTubeBotDBShowDatas].combine(
        youTubeBotDBShowDatas1,
        youTubeBotDBShowDatas1.copy(dbShowDatas =
          List(
            dbShowData2
          )
        )
      ),
      youTubeBotDBShowDatas1.copy(dbShowDatas =
        List(
          dbShowData1,
          dbShowData2
        )
      )
    )
  }
}
