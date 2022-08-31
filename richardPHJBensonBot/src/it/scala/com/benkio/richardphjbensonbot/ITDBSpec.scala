package com.benkio.richardphjbensonbot

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

  val testMedia = "rphjb_06.gif"

  // DBTimeout tests

  databaseFixture.test(
    "DBTimeout.getOrDefault should return the default timeout if the chat id is not present in the database"
  ) { connectionResourceAccess =>
    val transactor = connectionResourceAccess._3
    val dbTimeout  = DBTimeout[IO](transactor)
    val actual     = Resource.eval(dbTimeout.getOrDefault(100L)) // Not present ChatID

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
  ) { connectionResourceAccess =>
    val connection = connectionResourceAccess._1
    val transactor = connectionResourceAccess._3
    val dbTimeout  = DBTimeout[IO](transactor)
    val actual     = Resource.eval(dbTimeout.getOrDefault(1L)) // Present ChatID
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
  ) { connectionResourceAccess =>
    val transactor = connectionResourceAccess._3
    val dbTimeout  = DBTimeout[IO](transactor)
    val chatId     = 2L
    val timeout    = Timeout(chatId, 2.seconds.toMillis.toString, Timestamp.from(Instant.now()))
    val actual = for {
      _      <- Resource.eval(dbTimeout.setTimeout(timeout))
      result <- Resource.eval(dbTimeout.getOrDefault(chatId))
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
    connectionResourceAccess =>
      val transactor = connectionResourceAccess._3
      val dbTimeout  = DBTimeout[IO](transactor)
      val chatId     = 1L
      val timeout    = Timeout(chatId, 2.seconds.toMillis.toString, Timestamp.from(Instant.now()))
      val actual = for {
        _      <- Resource.eval(dbTimeout.setTimeout(timeout))
        result <- Resource.eval(dbTimeout.getOrDefault(chatId))
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
  ) { connectionResourceAccess =>
    val transactor = connectionResourceAccess._3
    val dbTimeout  = DBTimeout[IO](transactor)
    val chatId     = 2L
    val actual = for {
      _      <- Resource.eval(dbTimeout.logLastInteraction(chatId))
      result <- Resource.eval(dbTimeout.getOrDefault(chatId))
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
  ) { connectionResourceAccess =>
    val connection = connectionResourceAccess._1
    val transactor = connectionResourceAccess._3
    val dbTimeout  = DBTimeout[IO](transactor)
    val chatId     = 1L

    connection.createStatement().executeUpdate(ITDBResourceAccessSpec.timeoutSQL)

    val actual = for {
      prevResult  <- Resource.eval(dbTimeout.getOrDefault(chatId))
      _           <- Resource.eval(dbTimeout.logLastInteraction(chatId))
      afterResult <- Resource.eval(dbTimeout.getOrDefault(chatId))
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
    "messageRepliesVideosData should never raise an exception when try to open the file in resounces"
  ) { connectionResourceAccess =>
    val transactor = connectionResourceAccess._3
    val resourceAssert = for {
      dbResourceAccess <- connectionResourceAccess._2
      mp4s             <- Resource.pure(messageRepliesVideoData[IO].flatMap(_.mediafiles))
      checks <- Resource.eval(
        mp4s
          .traverse((mp4: MediaFile) =>
            dbResourceAccess
              .getUrlByName(mp4.filename)
              .unique
              .transact(transactor)
              .map { case (dbFilename, _) => mp4.filename == dbFilename }
          )
      )
    } yield checks.foldLeft(true)(_ && _)

    resourceAssert.use(IO.pure).assert
  }

  databaseFixture.test("messageRepliesMixData should never raise an exception when try to open the file in resounces") {
    connectionResourceAccess =>
      val transactor = connectionResourceAccess._3
      val resourceAssert = for {
        dbResourceAccess <- connectionResourceAccess._2
        mixs             <- Resource.pure(messageRepliesMixData[IO].flatMap(_.mediafiles))
        checks <- Resource.eval(
          mixs
            .traverse((mix: MediaFile) =>
              dbResourceAccess
                .getUrlByName(mix.filename)
                .unique
                .transact(transactor)
                .map { case (dbFilename, _) => mix.filename == dbFilename }
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

object ITDBResourceAccessSpec {

  val timeoutSQL =
    """INSERT INTO timeout (chat_id, timeout_value, last_interaction) VALUES ('1', '15000', '2010-01-01 00:00:01');"""
}
