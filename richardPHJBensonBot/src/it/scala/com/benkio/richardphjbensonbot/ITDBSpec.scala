package com.benkio.richardphjbensonbot

import com.benkio.telegrambotinfrastructure.model.Timeout

import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.db.DBTimeout

import com.benkio.telegrambotinfrastructure.DBFixture
import munit.CatsEffectSuite

import cats.effect.IO
import cats.effect.Resource
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.MediaFile
import doobie.implicits._

import java.sql.Timestamp
import java.time.Instant

import scala.concurrent.duration._

import com.benkio.richardphjbensonbot.data.Audio.messageRepliesAudioData
import com.benkio.richardphjbensonbot.data.Video.messageRepliesVideoData
import com.benkio.richardphjbensonbot.data.Gif.messageRepliesGifData
import com.benkio.richardphjbensonbot.data.Special.messageRepliesSpecialData
import com.benkio.richardphjbensonbot.data.Mix.messageRepliesMixData

class ITDBSpec extends CatsEffectSuite with DBFixture {

  // DBTimeout tests

  databaseFixture.test(
    "DBTimeout.getOrDefault should return the default timeout if the chat id is not present in the database"
  ) { fixture =>
    val transactor                    = fixture.transactor
    val dbTimeout: IO[DBTimeout[IO]]  = DBLayer[IO](transactor, null).map(_.dbTimeout)
    val actual: Resource[IO, Timeout] = Resource.eval(dbTimeout.flatMap(_.getOrDefault(100L))) // Not present ChatID

    actual
      .use { timeout =>
        IO {
          assertEquals(timeout.chat_id, 100L)
          assertEquals(timeout.timeout_value, "0")
        }
      }
      .unsafeRunSync()

  }

  databaseFixture.test(
    "DBTimeout.getOrDefault should return the timeout if the chat id is present in the database"
  ) { fixture =>
    val connection = fixture.connection
    val transactor = fixture.transactor
    val dbTimeout  = DBLayer[IO](transactor, null).map(_.dbTimeout)
    val actual     = Resource.eval(dbTimeout.flatMap(_.getOrDefault(1L))) // Present ChatID
    connection.createStatement().executeUpdate(ITDBResourceAccessSpec.timeoutSQL)
    actual
      .use { timeout =>
        IO {
          assertEquals(timeout.chat_id, 1L)
          assertEquals(timeout.timeout_value, "15000")
        }
      }
      .unsafeRunSync()

  }

  databaseFixture.test(
    "DBTimeout.setTimeout should insert the timeout if the chat id is not present in the database"
  ) { fixture =>
    val transactor = fixture.transactor
    val dbTimeout  = DBLayer[IO](transactor, null).map(_.dbTimeout)
    val chatId     = 2L
    val timeout    = Timeout(chatId, 2.seconds.toMillis.toString, Timestamp.from(Instant.now()))
    val actual = for {
      _      <- Resource.eval(dbTimeout.flatMap(_.setTimeout(timeout)))
      result <- Resource.eval(dbTimeout.flatMap(_.getOrDefault(chatId)))
    } yield result
    actual
      .use { timeout =>
        IO {
          assertEquals(timeout.chat_id, 2L)
          assertEquals(timeout.timeout_value, "2000")
        }
      }
      .unsafeRunSync()
  }

  databaseFixture.test("DBTimeout.setTimeout should update the timeout if the chat id is present in the database") {
    fixture =>
      val transactor = fixture.transactor
      val dbTimeout  = DBLayer[IO](transactor, null).map(_.dbTimeout)
      val chatId     = 1L
      val timeout    = Timeout(chatId, 2.seconds.toMillis.toString, Timestamp.from(Instant.now()))
      val actual = for {
        _      <- Resource.eval(dbTimeout.flatMap(_.setTimeout(timeout)))
        result <- Resource.eval(dbTimeout.flatMap(_.getOrDefault(chatId)))
      } yield result
      actual
        .use { timeout =>
          IO {
            assertEquals(timeout.chat_id, 1L)
            assertEquals(timeout.timeout_value, "2000")
          }
        }
        .unsafeRunSync()

  }

  databaseFixture.test(
    "DBTimeout.logLastInteraction should insert the timeout if the chat id is not present in the database"
  ) { fixture =>
    val transactor = fixture.transactor
    val dbTimeout  = DBLayer[IO](transactor, null).map(_.dbTimeout)
    val chatId     = 2L
    val actual = for {
      _      <- Resource.eval(dbTimeout.flatMap(_.logLastInteraction(chatId)))
      result <- Resource.eval(dbTimeout.flatMap(_.getOrDefault(chatId)))
    } yield result
    actual
      .use { timeout =>
        IO {
          assertEquals(timeout.chat_id, 2L)
          assertEquals(timeout.timeout_value, "0")
        }
      }
      .unsafeRunSync()

  }

  databaseFixture.test(
    "DBTimeout.logLastInteraction should update the timeout last interaction if the chat id is present in the database"
  ) { fixture =>
    val connection = fixture.connection
    val transactor = fixture.transactor
    val dbTimeout  = DBLayer[IO](transactor, null).map(_.dbTimeout)
    val chatId     = 1L

    connection.createStatement().executeUpdate(ITDBResourceAccessSpec.timeoutSQL)

    val actual = for {
      prevResult  <- Resource.eval(dbTimeout.flatMap(_.getOrDefault(chatId)))
      _           <- Resource.eval(dbTimeout.flatMap(_.logLastInteraction(chatId)))
      afterResult <- Resource.eval(dbTimeout.flatMap(_.getOrDefault(chatId)))
    } yield (prevResult, afterResult)
    actual
      .use { case (prevTimeout, afterTimeout) =>
        IO {
          assertEquals(prevTimeout.chat_id, 1L)
          assertEquals(prevTimeout.timeout_value, "15000")
          assertEquals(afterTimeout.chat_id, 1L)
          assertEquals(afterTimeout.timeout_value, "15000")
          assert(prevTimeout.last_interaction.before(afterTimeout.last_interaction))
        }
      }
      .unsafeRunSync()

  }

  // File Reference Check

  databaseFixture.test(
    "messageRepliesAudioData should never raise an exception when try to open the file in resounces"
  ) { fixture =>
    val transactor = fixture.transactor
    val resourceAssert = for {
      resourceDBMedia <- fixture.resourceDBMedia
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
        resourceDBMedia <- fixture.resourceDBMedia
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
      resourceDBMedia <- fixture.resourceDBMedia
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
        resourceDBMedia <- fixture.resourceDBMedia
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
      resourceDBMedia <- fixture.resourceDBMedia
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

object ITDBResourceAccessSpec {

  val timeoutSQL =
    """INSERT INTO timeout (chat_id, timeout_value, last_interaction) VALUES ('1', '15000', '2010-01-01 00:00:01');"""
}
