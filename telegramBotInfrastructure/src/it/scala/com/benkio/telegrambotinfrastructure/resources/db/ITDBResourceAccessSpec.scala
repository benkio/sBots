package com.benkio.telegrambotinfrastructure.resources.db

import com.benkio.telegrambotinfrastructure.model.Media
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
      dbMedia          <- fixture.resourceDBMedia
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
  ) { fixture =>
    val expected: List[Media] = List(
      Media(
        "xah_Asshole.mp3",
        Some("Ass"),
        "https://www.dropbox.com/sh/h9j3xata6j6d6h5/AAA75bTQjYFsnGLLhZsSbIBXa/Ass/xah_Asshole.mp3?dl=1",
        0,
        "1662126014510"
      ),
      Media(
        "xah_CCppSucksStonkyAss.mp3",
        Some("Sucks"),
        "https://www.dropbox.com/s/hth49nkewdrrrlw/xah_CCppSucksStonkyAss.mp3?dl=1",
        0,
        "1662126014670"
      ),
      Media(
        "xah_EmacsLispPainAss.mp3",
        Some("Emacs"),
        "https://www.dropbox.com/sh/h9j3xata6j6d6h5/AADMexmO4NL3-Ts6vWlamB9Xa/Emacs/xah_EmacsLispPainAss.mp3?dl=1",
        0,
        "1662126014677"
      )
    )

    val resourceAssert = for {
      dbMedia <- fixture.resourceDBMedia
      medias  <- Resource.eval(dbMedia.getMediaByMediaCount(limit = 3))
    } yield medias == expected
    resourceAssert.use(IO.pure).assert
  }

}
