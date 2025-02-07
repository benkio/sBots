package com.benkio.integration.integrationmunit.youtuboanchei0bot

import cats.effect.IO
import cats.implicits.*
import com.benkio.integration.DBFixture
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia
import com.benkio.youtuboanchei0bot.YouTuboAncheI0Bot
import doobie.implicits.*
import munit.CatsEffectSuite

class ITDBSpec extends CatsEffectSuite with DBFixture {

  import YouTuboAncheI0Bot.*

  // File Reference Check

  databaseFixture.test(
    "messageRepliesAudioData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val testAssert = for {
      mp3s <- messageRepliesAudioData[IO].flatTraverse((r: ReplyBundle[IO]) => ReplyBundle.getMediaFiles[IO](r))
      checks <-
        mp3s
          .traverse((mp3: MediaFile) =>
            DBMedia
              .getMediaQueryByName(mp3.filename)
              .unique
              .transact(transactor)
              .onError(_ => IO.println("[ERROR] mp3 missing from the DB: " + mp3))
              .attempt
              .map(_.isRight)
          )

    } yield checks.foldLeft(true)(_ && _)

    testAssert.assert
  }

  databaseFixture.test("messageRepliesGifData should never raise an exception when try to open the file in resounces") {
    fixture =>
      val transactor = fixture.transactor
      val testAssert = for {
        gifs <- messageRepliesGifData[IO].flatTraverse((r: ReplyBundle[IO]) => ReplyBundle.getMediaFiles[IO](r))
        checks <-
          gifs
            .traverse((gif: MediaFile) =>
              DBMedia
                .getMediaQueryByName(gif.filename)
                .unique
                .transact(transactor)
                .onError(_ => IO.println("[ERROR] gif missing from the DB: " + gif))
                .attempt
                .map(_.isRight)
            )
      } yield checks.foldLeft(true)(_ && _)

      testAssert.assert
  }

  databaseFixture.test(
    "messageRepliesMixData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val testAssert = for {
      specials <- messageRepliesMixData[IO].flatTraverse((r: ReplyBundle[IO]) => ReplyBundle.getMediaFiles[IO](r))
      checks <-
        specials
          .traverse((special: MediaFile) =>
            DBMedia
              .getMediaQueryByName(special.filename)
              .unique
              .transact(transactor)
              .onError(_ => IO.println("[ERROR] special missing from the DB: " + special))
              .attempt
              .map(_.isRight)
          )

    } yield checks.foldLeft(true)(_ && _)

    testAssert.assert
  }

  databaseFixture.test(
    "messageRepliesVideoData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val testAssert = for {
      specials <- messageRepliesVideoData[IO].flatTraverse((r: ReplyBundle[IO]) => ReplyBundle.getMediaFiles[IO](r))
      checks <-
        specials
          .traverse((special: MediaFile) =>
            DBMedia
              .getMediaQueryByName(special.filename)
              .unique
              .transact(transactor)
              .onError(_ => IO.println("[ERROR] special missing from the DB: " + special))
              .attempt
              .map(_.isRight)
          )

    } yield checks.foldLeft(true)(_ && _)

    testAssert.assert
  }

}
