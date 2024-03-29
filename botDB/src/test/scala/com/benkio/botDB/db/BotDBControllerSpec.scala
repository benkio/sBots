package com.benkio.botDB.db

import com.benkio.telegrambotinfrastructure.mocks.ResourceAccessMock
import cats.effect.kernel.Ref
import com.benkio.botDB.TestData.*

import com.benkio.botDB.mocks.DatabaseRepositoryMock
import com.benkio.botDB.mocks.MigratorMock
import munit.CatsEffectSuite

import java.io.File
import cats.effect.IO
import com.benkio.botDB.db.schema.MediaEntity

class BotDBControllerSpec extends CatsEffectSuite {

  val inputJson: File =
    new File(getClass.getResource("/").toURI).listFiles().toList.filterNot(_.getName == "com").head
  val mediaEntities: List[MediaEntity] = List(google, amazon, facebook)

  val resourceAccessMock = new ResourceAccessMock(List(inputJson))
  val migratorMock       = new MigratorMock()
  val databaseRepositoryMock = new DatabaseRepositoryMock(
    Ref.unsafe(mediaEntities)
  )
  val botDBController: BotDBController[IO] = BotDBController(
    cfg = config,
    databaseRepository = databaseRepositoryMock,
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
