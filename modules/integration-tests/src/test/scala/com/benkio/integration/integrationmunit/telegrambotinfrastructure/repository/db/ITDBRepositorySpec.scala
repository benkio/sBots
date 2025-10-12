package com.benkio.integration.integrationmunit.telegrambotinfrastructure.repository.db

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.integration.DBFixture
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.telegrambotinfrastructure.model.media.getMediaResourceFile
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.reply.Mp3File
import com.benkio.telegrambotinfrastructure.model.reply.Sticker
import com.benkio.telegrambotinfrastructure.repository.db.DBMediaData
import munit.CatsEffectSuite

import java.nio.file.Files

class ITDBRepositorySpec extends CatsEffectSuite with DBFixture {

  val testMediaName          = "rphjb_MaSgus.mp3"
  val testMediaId            = RichardPHJBensonBot.botId
  val testMedia: DBMediaData = DBMediaData(
    media_name = testMediaName,
    bot_id = testMediaId,
    kinds = """"[]"""",
    media_sources =
      """[\"https://www.dropbox.com/scl/fi/t5t952kwidqdyol4mutwv/rphjb_MaSgus.mp3?rlkey=f1fjff8ls4vjhs013plj1hrvs&dl=1\"]""",
    media_count = 0,
    created_at = "1662126019680",
    mime_type = "audio/mpeg"
  )

  databaseFixture.test("DBRepository.getResourceFile should return the expected content") { fixture =>
    val resourceAssert = for {
      dbMedia             <- fixture.resourceDBLayer.map(_.dbMedia)
      preMedia            <- Resource.eval(dbMedia.getMedia(testMediaName, false))
      dbRepository        <- fixture.repositoryResource
      mediaSourcesWrapped <- dbRepository.getResourceFile(Mp3File(testMediaName))
      mediaSources        <- mediaSourcesWrapped.fold(
        e => Resource.eval(IO.raiseError(Throwable(s"Error getResourceFile returned an error $e"))),
        _.traverse(_.getMediaResourceFile.sequence)
      )
      _ = println(s"debug mediaSource: $mediaSources")
      postMedia <- Resource.eval(dbMedia.getMedia(testMediaName, false))
      _ = println(s"debug postMedia: $postMedia")
      _            <- Resource.eval(dbMedia.decrementMediaCount(testMediaName))
      initialMedia <- Resource.eval(dbMedia.getMedia(testMediaName, false))
    } yield {
      val assert1 = postMedia == preMedia.map(x => x.copy(media_count = x.media_count + 1))
      val assert2 = mediaSources.exists(_.fold(false)(f => Files.readAllBytes(f.toPath).length >= (1024 * 5)))
      val assert3 = preMedia == initialMedia
      assert1 && assert2 && assert3
    }
    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test("DBRepository.getResourceFile on a Sticker should return the expected content") { fixture =>
    val resourceAssert = for {
      dbMedia  <- fixture.resourceDBLayer.map(_.dbMedia)
      preMedia <- Resource.eval(dbMedia.getMedia("ytai_PizzaYtancheio.sticker", false))
      _ = println(s"debug preMedia: $preMedia")
      dbRepository <- fixture.repositoryResource
      mediaSource  <- dbRepository.getResourceFile(Sticker("ytai_PizzaYtancheio.sticker"))
      _ = println(s"debug mediaSource: $mediaSource")
      postMedia <- Resource.eval(dbMedia.getMedia("ytai_PizzaYtancheio.sticker", false))
      _ = println(s"debug postMedia: $postMedia")
      _            <- Resource.eval(dbMedia.decrementMediaCount("ytai_PizzaYtancheio.sticker"))
      initialMedia <- Resource.eval(dbMedia.getMedia("ytai_PizzaYtancheio.sticker", false))
    } yield {
      val assert1 = postMedia == preMedia.map(x => x.copy(media_count = x.media_count + 1))
      val assert2 = mediaSource == Right(
        NonEmptyList.one(
          MediaResource.MediaResourceIFile(
            "CAACAgQAAxkBAAEC14Fnn4qAwqMd2BGYk0rsC5oTZvsMrAACzQEAAsMN4w3VywfrQeOnhTYE"
          )
        )
      )
      val assert3 = preMedia == initialMedia
      if assert1 && assert2 && assert3 then ()
      else println(s"[ITDBRepositorySpec:69] ERROR. $assert1 && $assert2 && $assert3")
      assert1 && assert2 && assert3
    }
    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test(
    "DBRepository.getResourcesByKind should return the expected list of files with expected content"
  ) { fixture =>
    val expectedFilenames = List(
      "cala_CalandraCamiciaGialla.jpg",
      "cala_CalandraMagliaRossa.jpg",
      "cala_CazzoDiBudda.jpg",
      "cala_EvilPencil.jpg",
      "cala_FattorinoGLS.jpg",
      "cala_GiamaRhythmLord.jpg",
      "cala_PorcoLadro.jpg"
    )
    val resourceAssert = for {
      dbRepository <- fixture.repositoryResource
      mediaSources <- dbRepository.getResourcesByKind("cards")
      files        <- mediaSources.fold(
        e => Resource.eval(IO.raiseError(Throwable(s"getResourceByKind returned an error $e"))),
        _.reduce.toList.mapFilter(_.getMediaResourceFile).sequence
      )
    } yield files
      .map(file => expectedFilenames.exists(matchFile => matchFile.toList.diff(file.getName().toList).isEmpty))
      .foldLeft(true)(_ && _)
    resourceAssert.use(IO.pure).assert
  }

}
