package com.benkio.abarberobot

import com.benkio.telegrambotinfrastructure.DBFixture
import munit.CatsEffectSuite

import cats.effect.IO
import cats.effect.Resource
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.MediaFile
import doobie.implicits._

class ITDBSpec extends CatsEffectSuite with DBFixture {

  import ABarberoBot._

  // File Reference Check

  databaseFixture.test(
    "messageRepliesAudioData should never raise an exception when try to open the file in resounces"
  ) { connectionResourceAccess =>
    val transactor = connectionResourceAccess._3
    val resourceAssert = for {
      dbResourceAccess <- connectionResourceAccess._2
      mp3s             <- Resource.pure(messageRepliesAudioData[IO].flatMap(_.mediafiles))
      checks <- Resource.eval(
        mp3s
          .traverse((mp3: MediaFile) =>
            dbResourceAccess
              .getUrlByName(mp3.filename)
              .unique
              .transact(transactor)
              .map { case (dbFilename, _) => mp3.filename == dbFilename }
          )
      )
    } yield checks.foldLeft(true)(_ && _)

    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test("messageRepliesGifData should never raise an exception when try to open the file in resounces") {
    connectionResourceAccess =>
      val transactor = connectionResourceAccess._3
      val resourceAssert = for {
        dbResourceAccess <- connectionResourceAccess._2
        gifs             <- Resource.pure(messageRepliesGifData[IO].flatMap(_.mediafiles))
        checks <- Resource.eval(
          gifs
            .traverse((gif: MediaFile) =>
              dbResourceAccess
                .getUrlByName(gif.filename)
                .unique
                .transact(transactor)
                .map { case (dbFilename, _) => gif.filename == dbFilename }
            )
        )
      } yield checks.foldLeft(true)(_ && _)

      resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test(
    "messageRepliesSpecialData should never raise an exception when try to open the file in resounces"
  ) { connectionResourceAccess =>
    val transactor = connectionResourceAccess._3
    val resourceAssert = for {
      dbResourceAccess <- connectionResourceAccess._2
      specials         <- Resource.pure(messageRepliesSpecialData[IO].flatMap(_.mediafiles))
      checks <- Resource.eval(
        specials
          .traverse((special: MediaFile) =>
            dbResourceAccess
              .getUrlByName(special.filename)
              .unique
              .transact(transactor)
              .map { case (dbFilename, _) => special.filename == dbFilename }
          )
      )
    } yield checks.foldLeft(true)(_ && _)

    resourceAssert.use(IO.pure).assert
  }

}
