package com.benkio.botDB.media

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.botDB.TestData.*
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.mocks.ResourceAccessMock
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceFile
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite

import java.io.File

class MediaUpdaterSpec extends CatsEffectSuite {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)
  val inputJson: MediaResourceFile[IO] =
    MediaResourceFile(
      Resource.pure(File(getClass.getResource("/").toURI).listFiles().toList.filterNot(_.getName == "com").head)
    )
  val mediaEntities: List[DBMediaData] = List(google, amazon, facebook)

  val resourceAccessMock = new ResourceAccessMock(_ => NonEmptyList.one(NonEmptyList.one(inputJson)).pure[IO])
  val dbLayerMock = DBLayerMock.mock(
    botName = "testBot",
    medias = mediaEntities
  )
  val mediaUpdater: MediaUpdater[IO] = MediaUpdater(
    config = config,
    dbLayer = dbLayerMock,
    resourceAccess = resourceAccessMock
  )

  test("MediaUpdater updateMedia should call the databaseRepository for data insertion") {
    assertIO_(mediaUpdater.updateMedia.use_)
  }
}
