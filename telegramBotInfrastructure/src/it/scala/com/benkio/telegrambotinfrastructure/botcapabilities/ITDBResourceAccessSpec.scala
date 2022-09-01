package com.benkio.telegrambotinfrastructure.botcapabilities

import com.benkio.telegrambotinfrastructure.DBFixture
import munit.CatsEffectSuite

import cats.effect.IO

import cats.implicits._

class ITDBResourceAccessSpec extends CatsEffectSuite with DBFixture {

  val testMedia = "rphjb_MaSgus.mp3"

  // DBResourceAccess tests

  databaseFixture.test("DBResourceAccess.getResourceByteArray should return the expected content") {
    connectionResourceAccess =>
      val resourceAssert = for {
        dbResourceAccess <- connectionResourceAccess._2
        arrayContent     <- dbResourceAccess.getResourceByteArray(testMedia)
      } yield arrayContent.length >= (1024 * 5)

      resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test(
    "DBResourceAccess.getResourcesByKind should return the expected list of files with expected content"
  ) { connectionResourceAccess =>
    val expectedFilenames = List(
      "ancheLaRabbiaHaUnCuore.txt",
      "live.txt",
      "perCordeEGrida.txt",
      "puntateCocktailMicidiale.txt",
      "puntateRockMachine.txt"
    )
    val resourceAssert = for {
      dbResourceAccess <- connectionResourceAccess._2
      files            <- dbResourceAccess.getResourcesByKind("rphjb_LinkSources")
    } yield files
      .map(file => expectedFilenames.exists(matchFile => matchFile.toList.diff(file.getName().toList).isEmpty))
      .foldLeft(true)(_ && _)
    resourceAssert.use(IO.pure).assert
  }

}

object ITDBResourceAccessSpec {

  val timeoutSQL =
    """INSERT INTO timeout (chat_id, timeout_value, last_interaction) VALUES ('1', '15000', '2010-01-01 00:00:01');"""
}
