package com.benkio.botDB.show

import cats.effect.IO
import cats.implicits.*
import com.benkio.botDB.mocks.YouTubeServiceMock
import com.benkio.botDB.show.ShowUpdater.ShowUpdaterImpl
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import com.google.api.services.youtube.model.*
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.*

class ShowUpdaterSpec extends CatsEffectSuite {

  import com.benkio.botDB.TestData.*

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  // Input Data
  val botName                = "testBot"
  val outputFilePath         = "outputFilePath"
  val videoIds: List[String] = List("6Tw1z", "vo0fM")
  val video: Video =
    Video()
      .setId("bQRuc")
      .setSnippet(
        VideoSnippet()
          .setTitle("VideoTitle")
          .setPublishedAt(
            com.google.api.client.util.DateTime.parseRfc3339("2023-05-17T10:24:55.000Z")
          )
          .setDescription("videoDescription")
      )
      .setContentDetails(
        VideoContentDetails().setDuration("PT10M50S")
      )
  val videos: List[Video] = List(video)
  val mediaResource: MediaResourceIFile[IO] =
    MediaResourceIFile(
      "test mediafile"
    )
  val youTubeBotIds = List(
    YouTubeBotIds(botName = botName, outputFilePath = outputFilePath, videoIds = videoIds)
  )

  // Mocks
  val dbLayerMock = DBLayerMock.mock(
    botName = botName
  )
  val youTubeServiceMock = YouTubeServiceMock(
    onGetAllBotNameIds = youTubeBotIds.pure[IO],
    onGetYouTubeVideos = inputVideoIds =>
      if inputVideoIds == videoIds
      then videos.pure[IO]
      else IO.raiseError(Throwable(s"[ShowUpdaterSpec] Unexpected input video ids: $inputVideoIds"))
  )

  val showUpdater: ShowUpdaterImpl[IO] = ShowUpdaterImpl(
    config = config,
    dbLayer = dbLayerMock,
    youTubeService = youTubeServiceMock
  )
  val expectedDBShowData = DBShowData(
    show_id = "bQRuc",
    bot_name = "testBot",
    show_title = "VideoTitle",
    show_upload_date = "2023-05-17T10:24:55.000Z",
    show_duration = 650,
    show_description = Some(
      value = "videoDescription"
    ),
    show_is_live = false,
    show_origin_automatic_caption = None
  )

  test("ShowUpdater.filterCandidateIds should leave the input unchanged if the stored Ids are empty") {
    assertEquals(ShowUpdater.filterCandidateIds(youTubeBotIds, List.empty), youTubeBotIds)
  }
  test("ShowUpdater.filterCandidateIds should filter out from the input the stored Ids") {
    val otherVideoId1: String = "rTU6G"
    val otherVideoId2: String = "yHLzy"
    val otherIds = List(
      YouTubeBotIds(botName = "testBot2", outputFilePath = "i8EWm", videoIds = List(otherVideoId1)),
      YouTubeBotIds(botName = "testBot3", outputFilePath = "cYVdV", videoIds = List(otherVideoId2))
    )
    assertEquals(
      ShowUpdater.filterCandidateIds(youTubeBotIds ++ otherIds, List(otherVideoId1, otherVideoId2)),
      youTubeBotIds
    )
  }
  test("ShowUpdater.youTubeBotIdsToVideos should convert the expected video ids to videos") {
    assertIO(
      showUpdater.youTubeBotIdsToVideos(youTubeBotIds),
      List(YouTubeBotVideos(botName, outputFilePath, videos))
    )
  }
  test("ShowUpdater.videoToDBShowData should convert the input video without data to None if missing data") {
    assertIO(
      showUpdater.videoToDBShowData(Video(), botName),
      None
    )
  }
  test("ShowUpdater.videoToDBShowData should convert the input video with data to expected DBShowData") {
    assertIO(
      showUpdater.videoToDBShowData(video, botName),
      Some(
        value = expectedDBShowData
      )
    )
  }
  test("ShowUpdater.youTubeBotVideosToDbShowDatas should convert multiple youtube videos to expected DBShowData") {
    assertIO(
      showUpdater.youTubeBotVideosToDbShowDatas(List(YouTubeBotVideos(botName, outputFilePath, videos))),
      List(
        YouTubeBotDBShowDatas(
          botName = botName,
          outputFilePath = outputFilePath,
          dbShowDatas = List(expectedDBShowData)
        )
      )
    )
  }
  test("ShowUpdater.insertDBShowDatas should insert the expected DBMediaData into the DB") {
    val input: List[YouTubeBotDBShowDatas] = List(
      YouTubeBotDBShowDatas(
        botName = botName,
        outputFilePath = outputFilePath,
        dbShowDatas = List(expectedDBShowData)
      )
    )
    val expected: List[DBShowData] = List(
      DBShowData(
        show_id = "bQRuc",
        bot_name = "testBot",
        show_title = "VideoTitle",
        show_upload_date = "2023-05-17T10:24:55.000Z",
        show_duration = 650,
        show_description = Some(
          value = "videoDescription"
        ),
        show_is_live = false,
        show_origin_automatic_caption = None
      )
    )
    assertIO_(showUpdater.insertDBShowDatas(input)) >>
      assertIO(dbLayerMock.dbShow.getShows(botName), expected)
  }
  test("ShowUpdater.updateShow implement") { assert(false) }
  test("ShowUpdater.getStoredIds implement") { assert(false) }
  test("ShowUpdater.updateStoredJsons implement") { assert(false) }
}
