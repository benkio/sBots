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
      val transactor     = connectionResourceAccess._3
      val urlFetcher     = UrlFetcher[IO]()
      val resourceAccess = DBResourceAccess[IO](transactor, urlFetcher)
      val obtained       = resourceAccess.getResourceByteArray(testMedia)

      obtained
        .use { byteArray =>
          IO(
            assert(byteArray.length >= 100)
          )
        }
        .unsafeRunSync()
  }

  databaseFixture.test(
    "DBResourceAccess.getResourcesByKind should return the expected list of files with expected content"
  ) { connectionResourceAccess =>
    val transactor     = connectionResourceAccess._3
    val urlFetcher     = UrlFetcher[IO]()
    val resourceAccess = DBResourceAccess[IO](transactor, urlFetcher)
    val obtained       = resourceAccess.getResourcesByKind("rphjb_LinkSources")
    val expectedFilenames = List(
      "ancheLaRabbiaHaUnCuore.txt",
      "live.txt",
      "perCordeEGrida.txt",
      "puntateCocktailMicidiale.txt",
      "puntateRockMachine.txt"
    )

    obtained
      .use { files =>
        IO(
          files.foreach { file =>
            assert(expectedFilenames.exists(matchFile => matchFile.toList.diff(file.getName().toList).isEmpty))
          }
        )
      }
      .unsafeRunSync()

  }

}

object ITDBResourceAccessSpec {

  val timeoutSQL =
    """INSERT INTO timeout (chat_id, timeout_value, last_interaction) VALUES ('1', '15000', '2010-01-01 00:00:01');"""
}
