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

  val (files, folders) =
    new File(getClass.getResource("/").toURI).listFiles().toList.filterNot(_.getName == "com").partition(_.isFile)
  val media1File             = files.head // Just 1, media1.txt
  val inputFiles: List[File] = media1File +: folders

  val resourceAccessMock = new ResourceAccessMock(inputFiles)
  val migratorMock       = new MigratorMock()
  val databaseRepositoryMock = new DatabaseRepositoryMock(
    Ref.unsafe(List(mediaEntity1, mediaEntity2, mediaEntity3))
  )
  val botDBController = BotDBController(
    cfg = config,
    databaseRepository = databaseRepositoryMock,
    resourceAccess = resourceAccessMock,
    migrator = migratorMock
  )

  test("BotDBController flattenResources should flat directories recursively in a list of files") {
    val media2File = new File(getClass.getResource("/kind/media2.txt").toURI)
    val media3File = new File(getClass.getResource("/kind/innerKind/media3.txt").toURI)
    val expected = List(
      (media1File, None),
      (media3File, Some("kind_innerKind")),
      (media2File, Some("kind"))
    )
    assertEquals(expected, BotDBController.flattenResources(inputFiles))
  }

  test("BotDBController populateMediaTable should call the databaseRepository for data insertion") {
    assertIO_(botDBController.populateMediaTable().use_)
  }

  test("BotDBController build should run the migrations and populate the DB") {
    assertIO_(botDBController.build.use_)
  }
}
