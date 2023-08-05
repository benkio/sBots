package com.benkio.integration.integrationmunit.telegrambotinfrastructure.resources.db

import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData

import cats.effect.Resource
import com.benkio.integration.DBFixture
import munit.CatsEffectSuite

import cats.effect.IO

import cats.implicits._

class ITDBResourceAccessSpec extends CatsEffectSuite with DBFixture {

  val testMediaName = "rphjb_MaSgus.mp3"
  val testMedia: DBMediaData = DBMediaData(
    testMediaName,
    None,
    "https://www.dropbox.com/sh/xqaatugvq8zcoyu/AACBnRH33traQAKBGy9bidu0a/rphjb_MaSgus.mp3?dl=1",
    0,
    "1662126019680"
  )

  databaseFixture.test("DBResourceAccess.getResourceByteArray should return the expected content") { fixture =>
    val resourceAssert = for {
      dbMedia          <- fixture.resourceDBLayer.map(_.dbMedia)
      preMedia         <- Resource.eval(dbMedia.getMedia(testMediaName, false))
      dbResourceAccess <- fixture.resourceAccessResource
      arrayContent     <- dbResourceAccess.getResourceByteArray(testMediaName)
      postMedia        <- Resource.eval(dbMedia.getMedia(testMediaName, false))
      _                <- Resource.eval(dbMedia.decrementMediaCount(testMediaName))
      initialMedia     <- Resource.eval(dbMedia.getMedia(testMediaName, false))
    } yield {
      val assert1 = postMedia == preMedia.copy(media_count = preMedia.media_count + 1)
      val assert2 = arrayContent.length >= (1024 * 5)
      val assert3 = preMedia == initialMedia
      assert1 && assert2 && assert3
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

}
