package com.benkio.botDB.db

import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock.DBMediaMock
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import com.benkio.telegrambotinfrastructure.mocks.ResourceAccessMock
import cats.effect.kernel.Ref
import com.benkio.botDB.TestData.*

import com.benkio.botDB.mocks.MigratorMock
import munit.CatsEffectSuite

import java.io.File
import cats.effect.IO

class BotDBControllerSpec extends CatsEffectSuite {

  val inputJson: MediaResource =
    MediaResource.MediaResourceFile(
      new File(getClass.getResource("/").toURI).listFiles().toList.filterNot(_.getName == "com").head
    )
  val mediaEntities: List[DBMediaData] = List(google, amazon, facebook)

  val resourceAccessMock = new ResourceAccessMock(List(inputJson))
  val migratorMock       = new MigratorMock()
  val dbMediaMock        = new DBMediaMock(Ref.unsafe[IO, List[DBMediaData]](mediaEntities))
  val botDBController: BotDBController[IO] = BotDBController(
    cfg = config,
    databaseRepository = dbMediaMock,
    resourceAccess = resourceAccessMock,
    migrator = migratorMock
  )

  test("BotDBController populateMediaTable should call the databaseRepository for data insertion") {
    assertIO_(botDBController.populateMediaTable.use_)
  }

  test("BotDBController build should run the migrations and populate the DB") {
    assertIO_(botDBController.build.use_)
  }
}
