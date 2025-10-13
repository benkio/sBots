package com.benkio.botDB.show

import cats.effect.IO
import cats.implicits.*
import com.benkio.botDB.mocks.YouTubeServiceMock
import com.benkio.botDB.show.ShowUpdater.ShowUpdaterImpl
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.model.SBotId
import com.benkio.telegrambotinfrastructure.repository.db.DBShowData
import com.google.api.services.youtube.model.*
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.*

import java.nio.file.Files
import java.nio.file.Paths
import scala.jdk.CollectionConverters.*

class ShowUpdaterSpec extends CatsEffectSuite {

  import com.benkio.botDB.TestData.*

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  // Input Data
  val botId: SBotId          = SBotId("testbot")
  val outputFilePath         = "./src/test/resources/testdata/testBotShow.json"
  val captionLanguage        = "it"
  val testCaption            = "Test Caption"
  val videoId                = "6Tw1z"
  val videoIds: List[String] = List(videoId, "vo0fM")
  val video: Video           =
    Video()
      .setId(videoId)
      .setSnippet(
        VideoSnippet()
          .setTitle("videoTitle")
          .setPublishedAt(
            com.google.api.client.util.DateTime.parseRfc3339("2023-05-17T10:24:55.000Z")
          )
          .setDescription("videoDescription")
      )
      .setContentDetails(
        VideoContentDetails().setDuration("PT10M50S")
      )
  val videos: List[Video] = List(video)
  val youTubeBotIds       = List(
    YouTubeBotIds(
      botId = botId,
      outputFilePath = outputFilePath,
      captionLanguage = captionLanguage,
      videoIds = videoIds
    )
  )
  val expectedDBShowData = DBShowData(
    show_id = videoId,
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
  val expectedYouTubeBotDBShowDatas = List(
    YouTubeBotDBShowDatas(
      botId = botId,
      outputFilePath = outputFilePath,
      captionLanguage = captionLanguage,
      dbShowDatas = List(expectedDBShowData)
    )
  )
  val storedYouTubeBotDBShowDatas = List(
    YouTubeBotDBShowDatas(
      botId = botId,
      outputFilePath = outputFilePath,
      captionLanguage = captionLanguage,
      dbShowDatas = List(
        DBShowData(
          show_id = "ADACFpS1qJo",
          bot_id = "abar",
          show_title = "Chiedilo a Barbero - Trailer - Intesa Sanpaolo On Air",
          show_upload_date = "2023-05-17T10:24:55.000Z",
          show_duration = 69,
          show_description = Some(
            value =
              """Iscriviti al canale per non perderti nessun aggiornamento su “Chiedilo a Barbero” e seguici su:
                |Spotify: https://open.spotify.com/show/7JLDPffy6du4rAy8xW3hTT
                |Apple Podcast: https://podcasts.apple.com/it/podcast/chiedilo-a-barbero-intesa-sanpaolo-on-air/id1688392438
                |Google Podcast: https://podcasts.google.com/feed/aHR0cHM6Ly9kMTcycTN0b2o3dzFtZC5jbG91ZGZyb250Lm5ldC9yc3MteG1sLWZpbGVzLzhmYjliOGYyLTU5MGItNDhmOS1hNTY2LWE5NWI3OTUwYWY2OC54bWw
                |Intesa Sanpaolo Group: https://group.intesasanpaolo.com/it/sezione-editoriale/intesa-sanpaolo-on-air""".stripMargin
          ),
          show_is_live = false,
          show_origin_automatic_caption = None
        )
      )
    )
  )

  // Mocks
  val dbLayerMock = DBLayerMock.mock(
    botId = botId
  )
  val youTubeServiceMock = YouTubeServiceMock(
    onGetAllBotNameIds = youTubeBotIds.pure[IO],
    onGetYouTubeVideos = inputVideoIds =>
      if inputVideoIds == videoIds
      then videos.pure[IO]
      else IO.raiseError(Throwable(s"[ShowUpdaterSpec] Unexpected input video ids: $inputVideoIds")),
    onFetchCaption = (inputVideoId, tempDir, inputCaptionLanguage) =>
      if videoIds.exists(_ == inputVideoId) && captionLanguage == inputCaptionLanguage && List(
          "target",
          "ytdlpCaptions"
        ).forall(tempDir.toString.contains)
      then IO.pure(Some(testCaption))
      else
        IO.raiseError(
          Throwable(
            s"[ShowUpdaterSpec] Unexpected input for fetchCaption. ($inputVideoId, $tempDir, $inputCaptionLanguage) ≠ (one of `$videoIds`, `does not contains: target & ytdlpCaptions`, $captionLanguage)"
          )
        )
  )

  val showUpdater: ShowUpdaterImpl[IO] = ShowUpdaterImpl(
    config = config,
    dbLayer = dbLayerMock,
    youTubeService = youTubeServiceMock
  )

  test("ShowUpdater.filterCandidateIds should leave the input unchanged if the stored Ids are empty") {
    assertEquals(ShowUpdater.filterCandidateIds(youTubeBotIds, List.empty), youTubeBotIds)
  }
  test("ShowUpdater.filterCandidateIds should filter out from the input the stored Ids") {
    val otherVideoId1: String = "rTU6G"
    val otherVideoId2: String = "yHLzy"
    val otherIds              = List(
      YouTubeBotIds(
        botId = SBotId("testBot2"),
        outputFilePath = "i8EWm",
        captionLanguage = captionLanguage,
        videoIds = List(otherVideoId1)
      ),
      YouTubeBotIds(
        botId = SBotId("testBot3"),
        outputFilePath = "cYVdV",
        captionLanguage = captionLanguage,
        videoIds = List(otherVideoId2)
      )
    )
    assertEquals(
      ShowUpdater.filterCandidateIds(youTubeBotIds ++ otherIds, List(otherVideoId1, otherVideoId2)),
      youTubeBotIds
    )
  }
  test("ShowUpdater.youTubeBotIdsToVideos should convert the expected video ids to videos") {
    assertIO(
      showUpdater.youTubeBotIdsToVideos(youTubeBotIds),
      List(YouTubeBotVideos(botId, outputFilePath, captionLanguage, videos))
    )
  }
  test("ShowUpdater.videoToDBShowData should convert the input video without data to None if missing data") {
    assertIO(
      showUpdater.videoToDBShowData(Video(), botId),
      None
    )
  }
  test("ShowUpdater.videoToDBShowData should convert the input video with data to expected DBShowData") {
    assertIO(
      showUpdater.videoToDBShowData(video, botId),
      Some(
        value = expectedDBShowData
      )
    )
  }
  test("ShowUpdater.youTubeBotVideosToDbShowDatas should convert multiple youtube videos to expected DBShowData") {
    assertIO(
      showUpdater.youTubeBotVideosToDbShowDatas(
        List(YouTubeBotVideos(botId, outputFilePath, captionLanguage, videos))
      ),
      expectedYouTubeBotDBShowDatas
    )
  }
  test("ShowUpdater.insertDBShowDatas should insert the expected DBMediaData into the DB") {
    val input: List[YouTubeBotDBShowDatas] = List(
      YouTubeBotDBShowDatas(
        botId = botId,
        outputFilePath = outputFilePath,
        captionLanguage = captionLanguage,
        dbShowDatas = List(expectedDBShowData)
      )
    )
    val expected: List[DBShowData] = List(expectedDBShowData)
    assertIO_(showUpdater.insertDBShowDatas(input)) >>
      assertIO(dbLayerMock.dbShow.getShows(botId), expected)
  }
  test("ShowUpdater.getStoredDbShowDatas should retrieve the ids from the show file") {
    assertIO(
      showUpdater.getStoredDbShowDatas,
      storedYouTubeBotDBShowDatas
    )
  }
  test("ShowUpdater.updateStoredJsons should correctly update the output json with the input") {

    val showFileContent =
      Files.readAllBytes(Paths.get(outputFilePath))

    val assertTest = for
      _ <- showUpdater.updateStoredJsons(outputFilePath, List(expectedDBShowData))
      afterContent: String = Files.readAllLines(Paths.get(outputFilePath)).asScala.reduce(_ + "\n" + _)
    yield
      assert(afterContent.contains(videoId))
      assert(afterContent.contains("videoTitle"))
      assert(afterContent.contains("videoDescription"))

    assertTest >> IO(
      Files.write(Paths.get(outputFilePath), showFileContent)
    )
  }
  test("ShowUpdater.updateShow should run without throwing exceptions") {
    assertIO_(showUpdater.updateShow.use_)
  }
  test("ShowUpdater.mergeShowDatas should successfully merge two db show data lists") {
    val secondDBShowData = DBShowData(
      show_id = "ADACFpS1qJo",
      bot_id = "abar",
      show_title = "mergeShowDatas test",
      show_upload_date = "2023-05-17T10:24:55.000Z",
      show_duration = 69,
      show_description = Some(value = """mergeShowDatas description""".stripMargin),
      show_is_live = false,
      show_origin_automatic_caption = None
    )
    val secondDBShowDatas = List(
      YouTubeBotDBShowDatas(
        botId = botId,
        outputFilePath = outputFilePath,
        captionLanguage = captionLanguage,
        dbShowDatas = List(secondDBShowData)
      )
    )

    assertEquals(
      ShowUpdater.mergeShowDatas(
        storedYouTubeBotDBShowDatas,
        secondDBShowDatas
      ),
      List(
        storedYouTubeBotDBShowDatas.head.copy(dbShowDatas =
          storedYouTubeBotDBShowDatas.head.dbShowDatas :+ secondDBShowData
        )
      )
    )
  }
  test("ShowUpdater.addCaptions should successfully enrich the input with caption") {
    showUpdater
      .addCaptions(expectedYouTubeBotDBShowDatas, List("java"))
      .map(result =>
        assertEquals(result.length, 1)
        assertEquals(result.head.dbShowDatas.length, 1)
        assertEquals(
          result,
          expectedYouTubeBotDBShowDatas.map(expectedYouTubeBotDBShowData =>
            expectedYouTubeBotDBShowData.copy(dbShowDatas =
              expectedYouTubeBotDBShowData.dbShowDatas.map(_.copy(show_origin_automatic_caption = Some(testCaption)))
            )
          )
        )
      )
  }
}
