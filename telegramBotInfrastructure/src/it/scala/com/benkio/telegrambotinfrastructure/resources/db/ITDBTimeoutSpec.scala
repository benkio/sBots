package com.benkio.telegrambotinfrastructure.resources.db

import doobie.Transactor
import java.sql.DriverManager
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.DBFixture
import munit.CatsEffectSuite
import cats.effect.IO

import java.time.Instant
import scala.concurrent.duration._
import doobie.munit.analysisspec.IOChecker

class ITDBTimeoutSpec extends CatsEffectSuite with DBFixture with IOChecker {
  val testTimeoutChatId = 1L

  val testTimeout: DBTimeoutData = DBTimeoutData(
    chat_id = testTimeoutChatId,
    timeout_value = "15000",
    last_interaction = "1669302207"
  )

  override def transactor: doobie.Transactor[cats.effect.IO] = {
    Class.forName("org.sqlite.JDBC")
    val conn = DriverManager.getConnection(dbUrl)
    runMigrations(dbUrl, migrationTable, migrationPath)
    val transactor = Transactor.fromConnection[IO](conn)
    transactor
  }

  databaseFixture.test(
    "DBTimeout queries should check".ignore
  ) { fixture =>
    fixture.resourceDBLayer
      .map(_.dbTimeout)
      .use(dbTimeout =>
        for {
          _ <- IO(checkOutput(dbTimeout.getOrDefaultQuery(testTimeoutChatId)))
          _ <- IO(check(dbTimeout.setTimeoutQuery(testTimeout)))
          _ <- IO(check(dbTimeout.logLastInteractionQuery(testTimeoutChatId)))
        } yield ()
      )
      .assert
  }

  databaseFixture.test(
    "DBTimeout.getOrDefault should return the default timeout if the chat id is not present in the database"
  ) { fixture =>
    val actual: Resource[IO, DBTimeoutData] =
      fixture.resourceDBLayer.evalMap(dbLayer => dbLayer.dbTimeout.getOrDefault(100L) // Not present ChatID
      )

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
    val actual = for {
      dbTimeout <- fixture.resourceDBLayer.map(_.dbTimeout)
      _         <- Resource.eval(dbTimeout.setTimeout(testTimeout))
      timeout   <- Resource.eval(dbTimeout.getOrDefault(1L)) // Present ChatID
    } yield timeout

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
    val chatId  = 2L
    val timeout = DBTimeoutData(chatId, 2.seconds.toMillis.toString, Instant.now().getEpochSecond().toString())
    val actual = for {
      dbTimeout <- fixture.resourceDBLayer.map(_.dbTimeout)
      _         <- Resource.eval(dbTimeout.setTimeout(timeout))
      result    <- Resource.eval(dbTimeout.getOrDefault(chatId))
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
      val chatId  = 1L
      val timeout = DBTimeoutData(chatId, 2.seconds.toMillis.toString, Instant.now().getEpochSecond().toString())
      val actual = for {
        dbTimeout <- fixture.resourceDBLayer.map(_.dbTimeout)
        _         <- Resource.eval(dbTimeout.setTimeout(timeout))
        result    <- Resource.eval(dbTimeout.getOrDefault(chatId))
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
    val chatId = 2L
    val actual = for {
      dbTimeout <- fixture.resourceDBLayer.map(_.dbTimeout)
      _         <- Resource.eval(dbTimeout.logLastInteraction(chatId))
      result    <- Resource.eval(dbTimeout.getOrDefault(chatId))
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
    val chatId = 1L

    val actual = for {
      dbTimeout   <- fixture.resourceDBLayer.map(_.dbTimeout)
      _           <- Resource.eval(dbTimeout.setTimeout(testTimeout))
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
          assert(prevTimeout.last_interaction.toLong < afterTimeout.last_interaction.toLong)
        }
      }
      .unsafeRunSync()

  }
}
