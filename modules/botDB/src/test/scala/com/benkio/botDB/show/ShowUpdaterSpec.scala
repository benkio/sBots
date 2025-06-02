package com.benkio.botDB.show

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.mocks.ResourceAccessMock
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.*

class ShowUpdaterSpec extends CatsEffectSuite {

  import com.benkio.botDB.TestData.*

  val youTubeApiKey        = "youTubeApiKey"
  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  val mediaEntities: List[DBMediaData] = List(google, amazon, facebook)
  val mediaResource: MediaResourceIFile[IO] =
    MediaResourceIFile(
      "test mediafile"
    )
  val resourceAccessMock = new ResourceAccessMock(_ => NonEmptyList.one(NonEmptyList.one(mediaResource)).pure[IO])
  val dbLayerMock = DBLayerMock.mock(
    botName = "testBot",
    medias = mediaEntities
  )
}
