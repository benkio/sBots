package com.benkio.integration.integrationmunit.telegrambotinfrastructure.resources.db

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.integration.DBFixture
import com.benkio.telegrambotinfrastructure.model.media.getMediaResourceFile
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.reply.Mp3File
import com.benkio.telegrambotinfrastructure.model.reply.Sticker
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import munit.CatsEffectSuite

import java.nio.file.Files

class ITDBResourceAccessSpec extends CatsEffectSuite with DBFixture {

  val testMediaName = "rphjb_MaSgus.mp3"
  val testMedia: DBMediaData = DBMediaData(
    media_name = testMediaName,
    kinds = """"[]"""",
    media_sources =
      """[\"https://www.dropbox.com/scl/fi/t5t952kwidqdyol4mutwv/rphjb_MaSgus.mp3?rlkey=f1fjff8ls4vjhs013plj1hrvs&dl=1\"]""",
    media_count = 0,
    created_at = "1662126019680",
    mime_type = "audio/mpeg"
  )

  databaseFixture.test("DBResourceAccess.getResourceFile should return the expected content") { fixture =>
    val resourceAssert = for {
      dbMedia  <- fixture.resourceDBLayer.map(_.dbMedia)
      preMedia <- Resource.eval(dbMedia.getMedia(testMediaName, false))
      _ = println(s"debug preMedia: $preMedia")
      dbResourceAccess <- fixture.resourceAccessResource
      mediaSource      <- dbResourceAccess.getResourceFile(Mp3File(testMediaName))
      _ = println(s"debug mediaSource: $mediaSource")
      postMedia <- Resource.eval(dbMedia.getMedia(testMediaName, false))
      _ = println(s"debug postMedia: $postMedia")
      _            <- Resource.eval(dbMedia.decrementMediaCount(testMediaName))
      initialMedia <- Resource.eval(dbMedia.getMedia(testMediaName, false))
    } yield {
      val assert1 = postMedia == preMedia.copy(media_count = preMedia.media_count + 1)
      val assert2 = mediaSource.getMediaResourceFile.fold(false)(f => Files.readAllBytes(f.toPath).length >= (1024 * 5))
      val assert3 = preMedia == initialMedia
      assert1 && assert2 && assert3
    }
    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test("DBResourceAccess.getResourceFile on a Sticker should return the expected content") { fixture =>
    val resourceAssert = for {
      dbMedia  <- fixture.resourceDBLayer.map(_.dbMedia)
      preMedia <- Resource.eval(dbMedia.getMedia("ytai_PizzaYtancheio.sticker", false))
      _ = println(s"debug preMedia: $preMedia")
      dbResourceAccess <- fixture.resourceAccessResource
      mediaSource      <- dbResourceAccess.getResourceFile(Sticker("ytai_PizzaYtancheio.sticker"))
      _ = println(s"debug mediaSource: $mediaSource")
      postMedia <- Resource.eval(dbMedia.getMedia("ytai_PizzaYtancheio.sticker", false))
      _ = println(s"debug postMedia: $postMedia")
      _            <- Resource.eval(dbMedia.decrementMediaCount("ytai_PizzaYtancheio.sticker"))
      initialMedia <- Resource.eval(dbMedia.getMedia("ytai_PizzaYtancheio.sticker", false))
    } yield {
      val assert1 = postMedia == preMedia.copy(media_count = preMedia.media_count + 1)
      val assert2 = mediaSource == MediaResource.MediaResourceIFile(
        "CAACAgQAAxkBAAEC14Fnn4qAwqMd2BGYk0rsC5oTZvsMrAACzQEAAsMN4w3VywfrQeOnhTYE"
      )
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
      mediaSources     <- dbResourceAccess.getResourcesByKind("rphjb_LinkSources")
      files = mediaSources.mapFilter(_.getMediaResourceFile)
    } yield files
      .map(file => expectedFilenames.exists(matchFile => matchFile.toList.diff(file.getName().toList).isEmpty))
      .foldLeft(true)(_ && _)
    resourceAssert.use(IO.pure).assert
  }

}
