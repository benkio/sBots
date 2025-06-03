package com.benkio.botDB.show

import cats.effect.IO
import cats.implicits.*
import com.benkio.botDB.mocks.YouTubeServiceMock
import com.benkio.botDB.show.ShowUpdater.ShowUpdaterImpl
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import com.google.api.services.youtube.model.Video
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.*

class ShowUpdaterSpec extends CatsEffectSuite {

  import com.benkio.botDB.TestData.*

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  // Input Data
  val botName                          = "testBot"
  val outputFilePath                   = "outputFilePath"
  val videoIds: List[String]           = List("6Tw1z", "vo0fM")
  val videos: List[Video]              = List(Video())
  val mediaEntities: List[DBMediaData] = List(google, amazon, facebook)
  val mediaResource: MediaResourceIFile[IO] =
    MediaResourceIFile(
      "test mediafile"
    )
  val youTubeBotIds = List(YouTubeBotIds(botName = botName, outputFilePath = outputFilePath, videoIds = videoIds))

  // Mocks
  val dbLayerMock = DBLayerMock.mock(
    botName = botName,
    medias = mediaEntities
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

  test("ShowUpdater.filterCandidateIds should leave the input unchanged if the stored Ids are empty") {
    assertEquals(ShowUpdater.filterCandidateIds(youTubeBotIds, List.empty), youTubeBotIds)
  }
  test("ShowUpdater.filterCandidateIds should filter out from the input the stored Ids") {
    // TODO
    assert(false)
  }
}
