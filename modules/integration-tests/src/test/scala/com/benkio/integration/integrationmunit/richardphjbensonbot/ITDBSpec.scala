package com.benkio.integration.integrationmunit.richardphjbensonbot

import cats.effect.IO
import cats.implicits.*
import com.benkio.integration.DBFixture
import com.benkio.richardphjbensonbot.data.Audio.messageRepliesAudioData
import com.benkio.richardphjbensonbot.data.Gif.messageRepliesGifData
import com.benkio.richardphjbensonbot.data.Mix.messageRepliesMixData
import com.benkio.richardphjbensonbot.data.Special.messageRepliesSpecialData
import com.benkio.richardphjbensonbot.data.Video.messageRepliesVideoData
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia
import doobie.implicits.*
import munit.CatsEffectSuite

class ITDBSpec extends CatsEffectSuite with DBFixture {

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
              .onError { case _ => IO.println("[ERROR] mp3 missing from the DB: " + mp3) }
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
                .onError { case _ => IO.println("[ERROR] gif missing from the DB: " + gif) }
                .attempt
                .map(_.isRight)
            )
      } yield checks.foldLeft(true)(_ && _)

      testAssert.assert
  }

  databaseFixture.test(
    "messageRepliesVideosData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val testAssert = for {
      mp4s <- messageRepliesVideoData[IO].flatTraverse((r: ReplyBundle[IO]) => ReplyBundle.getMediaFiles[IO](r))
      checks <-
        mp4s
          .traverse((mp4: MediaFile) =>
            DBMedia
              .getMediaQueryByName(mp4.filename)
              .unique
              .transact(transactor)
              .attempt
              .map(_.isRight)
              .onError { case _ => IO.println("[ERROR] mp4 missing from the DB: " + mp4) }
          )
    } yield checks.foldLeft(true)(_ && _)

    testAssert.assert
  }

  databaseFixture.test("messageRepliesMixData should never raise an exception when try to open the file in resounces") {
    fixture =>
      val transactor = fixture.transactor
      val testAssert = for {
        mixs <- messageRepliesMixData[IO].flatTraverse((r: ReplyBundle[IO]) => ReplyBundle.getMediaFiles[IO](r))
        checks <-
          mixs
            .traverse((mix: MediaFile) =>
              DBMedia
                .getMediaQueryByName(mix.filename)
                .unique
                .transact(transactor)
                .onError { case _ => IO.println("[ERROR] mix missing from the DB: " + mix) }
                .attempt
                .map(_.isRight)
            )
      } yield checks.foldLeft(true)(_ && _)

      testAssert.assert
  }

  databaseFixture.test(
    "messageRepliesSpecialData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val testAssert = for {
      specials <- messageRepliesSpecialData[IO].flatTraverse((r: ReplyBundle[IO]) => ReplyBundle.getMediaFiles[IO](r))
      checks <-
        specials
          .traverse((special: MediaFile) =>
            DBMedia
              .getMediaQueryByName(special.filename)
              .unique
              .transact(transactor)
              .onError { case _ => IO.println("[ERROR] special missing from the DB: " + special) }
              .attempt
              .map(_.isRight)
          )
    } yield checks.foldLeft(true)(_ && _)

    testAssert.assert
  }

}

object ITDBResourceAccessSpec {}
