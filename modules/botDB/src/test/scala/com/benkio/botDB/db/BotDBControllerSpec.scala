package com.benkio.botDB.db

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.botDB.mocks.MigratorMock
import com.benkio.botDB.mocks.ShowFetcherMock
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

class BotDBControllerSpec extends CatsEffectSuite {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)
  val inputJson: MediaResourceFile[IO] =
    MediaResourceFile(
      Resource.pure(File(getClass.getResource("/").toURI).listFiles().toList.filterNot(_.getName == "com").head)
    )
  val mediaEntities: List[DBMediaData] = List(google, amazon, facebook)

  val resourceAccessMock = new ResourceAccessMock(_ => NonEmptyList.one(NonEmptyList.one(inputJson)).pure[IO])
  val migratorMock       = new MigratorMock()
  val showFetcherMock    = new ShowFetcherMock()
  val dbLayerMock = DBLayerMock.mock(
    botName = "testBot",
    medias = mediaEntities
  )
  val botDBController: BotDBController[IO] = BotDBController(
    cfg = config,
    dbLayer = dbLayerMock,
    resourceAccess = resourceAccessMock,
    migrator = migratorMock,
    showFetcher = showFetcherMock
  )

  test("BotDBController populateMediaTable should call the databaseRepository for data insertion") {
    assertIO_(botDBController.populateMediaTable.use_)
  }

  test("BotDBController build should run the migrations and populate the DB") {
    assertIO_(botDBController.build.use_)
  }
}
