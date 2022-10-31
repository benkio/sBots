package com.benkio.telegrambotinfrastructure.resources.db

import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.DBFixture
import munit.CatsEffectSuite

import cats.effect.IO

import cats.implicits._

class ITDBResourceAccessSpec extends CatsEffectSuite with DBFixture {

  val testMedia = "rphjb_MaSgus.mp3"

  // DBResourceAccess tests

  databaseFixture.test("DBResourceAccess.getResourceByteArray should return the expected content") { fixture =>
    val resourceAssert = for {
      dbMedia          <- fixture.dbMediaResource
      preMedia         <- Resource.eval(dbMedia.getMedia(testMedia, false))
      dbResourceAccess <- fixture.resourceAccessResource
      arrayContent     <- dbResourceAccess.getResourceByteArray(testMedia)
      postMedia        <- Resource.eval(dbMedia.getMedia(testMedia, false))
      _                <- Resource.eval(dbMedia.decrementMediaCount(testMedia))
    } yield {
      val assert1 = postMedia.media_count == (preMedia.media_count + 1)
      val assert2 = arrayContent.length >= (1024 * 5)
      assert1 && assert2
    }

    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test(
    "DBResourceAccess.getResourcesByKind should return the expected list of files with expected content"
  ) { fixture =>
    val expectedFilenames = List(
      "ancheLaRabbiaHaUnCuore.txt",
      "live.txt",
      "perCordeEGrida.txt",
      "puntateCocktailMicidiale.txt",
      "puntateRockMachine.txt"
    )
    val resourceAssert = for {
      dbResourceAccess <- fixture.resourceAccessResource
      files            <- dbResourceAccess.getResourcesByKind("rphjb_LinkSources")
    } yield files
      .map(file => expectedFilenames.exists(matchFile => matchFile.toList.diff(file.getName().toList).isEmpty))
      .foldLeft(true)(_ && _)
    resourceAssert.use(IO.pure).assert
  }

  // DBMedia

  databaseFixture.test(
    "DBMedia.getMediaByMediaCount should return the expected list of media"
  ) { fixture => ??? }

}
