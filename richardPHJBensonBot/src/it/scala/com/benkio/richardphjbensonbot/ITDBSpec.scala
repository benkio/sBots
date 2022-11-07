package com.benkio.richardphjbensonbot

import com.benkio.telegrambotinfrastructure.DBFixture
import munit.CatsEffectSuite

import cats.effect.IO
import cats.effect.Resource
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.MediaFile
import doobie.implicits._

import com.benkio.richardphjbensonbot.data.Audio.messageRepliesAudioData
import com.benkio.richardphjbensonbot.data.Video.messageRepliesVideoData
import com.benkio.richardphjbensonbot.data.Gif.messageRepliesGifData
import com.benkio.richardphjbensonbot.data.Special.messageRepliesSpecialData
import com.benkio.richardphjbensonbot.data.Mix.messageRepliesMixData

class ITDBSpec extends CatsEffectSuite with DBFixture {

  // File Reference Check

  databaseFixture.test(
    "messageRepliesAudioData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val resourceAssert = for {
      resourceDBMedia <- fixture.resourceDBLayer.map(_.dbMedia)
      mp3s            <- Resource.pure(messageRepliesAudioData[IO].flatMap(_.mediafiles))
      checks <- Resource.eval(
        mp3s
          .traverse((mp3: MediaFile) =>
            resourceDBMedia
              .getMediaQueryByName(mp3.filename)
              .unique
              .transact(transactor)
              .onError(_ => IO.println(s"[ERROR] mp3 missing from the DB: " + mp3))
              .attempt
              .attempt
              .map(_.isRight)
          )
      )
    } yield checks.foldLeft(true)(_ && _)

    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test("messageRepliesGifData should never raise an exception when try to open the file in resounces") {
    fixture =>
      val transactor = fixture.transactor
      val resourceAssert = for {
        resourceDBMedia <- fixture.resourceDBLayer.map(_.dbMedia)
        gifs            <- Resource.pure(messageRepliesGifData[IO].flatMap(_.mediafiles))
        checks <- Resource.eval(
          gifs
            .traverse((gif: MediaFile) =>
              resourceDBMedia
                .getMediaQueryByName(gif.filename)
                .unique
                .transact(transactor)
                .onError(_ => IO.println(s"[ERROR] gif missing from the DB: " + gif))
                .attempt
                .map(_.isRight)
            )
        )
      } yield checks.foldLeft(true)(_ && _)

      resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test(
    "messageRepliesVideosData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val resourceAssert = for {
      resourceDBMedia <- fixture.resourceDBLayer.map(_.dbMedia)
      mp4s            <- Resource.pure(messageRepliesVideoData[IO].flatMap(_.mediafiles))
      checks <- Resource.eval(
        mp4s
          .traverse((mp4: MediaFile) =>
            resourceDBMedia
              .getMediaQueryByName(mp4.filename)
              .unique
              .transact(transactor)
              .attempt
              .map(_.isRight)
              .onError(_ => IO.println(s"[ERROR] mp4 missing from the DB: " + mp4))
          )
      )
    } yield checks.foldLeft(true)(_ && _)

    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test("messageRepliesMixData should never raise an exception when try to open the file in resounces") {
    fixture =>
      val transactor = fixture.transactor
      val resourceAssert = for {
        resourceDBMedia <- fixture.resourceDBLayer.map(_.dbMedia)
        mixs            <- Resource.pure(messageRepliesMixData[IO].flatMap(_.mediafiles))
        checks <- Resource.eval(
          mixs
            .traverse((mix: MediaFile) =>
              resourceDBMedia
                .getMediaQueryByName(mix.filename)
                .unique
                .transact(transactor)
                .onError(_ => IO.println(s"[ERROR] mix missing from the DB: " + mix))
                .attempt
                .map(_.isRight)
            )
        )
      } yield checks.foldLeft(true)(_ && _)

      resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test(
    "messageRepliesSpecialData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val resourceAssert = for {
      resourceDBMedia <- fixture.resourceDBLayer.map(_.dbMedia)
      specials        <- Resource.pure(messageRepliesSpecialData[IO].flatMap(_.mediafiles))
      checks <- Resource.eval(
        specials
          .traverse((special: MediaFile) =>
            resourceDBMedia
              .getMediaQueryByName(special.filename)
              .unique
              .transact(transactor)
              .onError(_ => IO.println(s"[ERROR] special missing from the DB: " + special))
              .attempt
              .map(_.isRight)
          )
      )
    } yield checks.foldLeft(true)(_ && _)

    resourceAssert.use(IO.pure).assert
  }

}

object ITDBResourceAccessSpec {}
