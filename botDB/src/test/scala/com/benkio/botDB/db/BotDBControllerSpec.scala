package com.benkio.botDB.db

import cats.effect.kernel.Ref
import com.benkio.botDB.TestData._
import com.benkio.botDB.db.BotDBController
import com.benkio.botDB.mocks.DatabaseRepositoryMock
import com.benkio.botDB.mocks.MigratorMock
import com.benkio.botDB.mocks.ResourceAccessMock
import munit.CatsEffectSuite

import java.io.File

class BotDBControllerSpec extends CatsEffectSuite {

  val inputCsv =
    new File(getClass.getResource("/").toURI).listFiles().toList.filterNot(_.getName == "com").head
  val mediaEntities = List(google, amazon, facebook)

  val resourceAccessMock = new ResourceAccessMock(List(inputCsv))
  val migratorMock       = new MigratorMock()
  val databaseRepositoryMock = new DatabaseRepositoryMock(
    Ref.unsafe(mediaEntities)
  )
  val botDBController = BotDBController(
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
