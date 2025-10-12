package com.benkio.integration.integrationmunit.telegrambotinfrastructure.repository.db

import cats.effect.IO
import cats.effect.Resource
import com.benkio.integration.DBFixture
import com.benkio.telegrambotinfrastructure.repository.db.DBTimeout
import com.benkio.telegrambotinfrastructure.repository.db.DBTimeoutData
import doobie.munit.analysisspec.IOChecker
import doobie.Transactor
import munit.CatsEffectSuite

import java.sql.DriverManager
import java.time.Instant
import scala.concurrent.duration.*

class ITDBTimeoutSpec extends CatsEffectSuite with DBFixture with IOChecker {
  val testTimeoutChatId = 1L
  val testBotId         = "botId"

  val testTimeout: DBTimeoutData = DBTimeoutData(
    chat_id = testTimeoutChatId,
    bot_id = testBotId,
    timeout_value = "15000",
    last_interaction = "1669302207"
  )

  override def transactor: doobie.Transactor[cats.effect.IO] = {
    Class.forName("org.sqlite.JDBC")
    val conn = DriverManager.getConnection(DBFixture.dbUrl)
    DBFixture.runMigrations(DBFixture.dbUrl, DBFixture.migrationTable, DBFixture.migrationPath)
    val transactor = Transactor.fromConnection[IO](conn, None)
    transactor
  }

  test(
    "DBTimeout queries should check".ignore
  ) {
    check(DBTimeout.getOrDefaultQuery(testTimeoutChatId, testBotId))
    check(DBTimeout.setTimeoutQuery(testTimeout))
    check(DBTimeout.removeTimeoutQuery(testTimeout.chat_id, testTimeout.bot_id))
    check(DBTimeout.logLastInteractionQuery(testTimeoutChatId, testBotId))
  }

  databaseFixture.test(
    "DBTimeout.getOrDefault should return the default timeout if the chat id is not present in the database"
  ) { fixture =>
    (for
      // Not present ChatID
      timeout <- fixture.resourceDBLayer.evalMap(dbLayer => dbLayer.dbTimeout.getOrDefault(100L, testBotId))
    yield
      assertEquals(timeout.chat_id, 100L)
      assertEquals(timeout.timeout_value, "0")
    ).use_
  }

  databaseFixture.test(
    "DBTimeout.getOrDefault should return the timeout if the chat id is present in the database"
  ) { fixture =>
    (for {
      dbTimeout <- fixture.resourceDBLayer.map(_.dbTimeout)
      _         <- Resource.eval(dbTimeout.setTimeout(testTimeout))
      timeout   <- Resource.eval(dbTimeout.getOrDefault(1L, testBotId)) // Present ChatID
    } yield {
      assertEquals(timeout.chat_id, 1L)
      assertEquals(timeout.timeout_value, "15000")
    }).use_
  }

  databaseFixture.test(
    "DBTimeout.setTimeout should insert the timeout if the chat id is not present in the database"
  ) { fixture =>
    val chatId  = 2L
    val timeout =
      DBTimeoutData(chatId, testBotId, 2.seconds.toMillis.toString, Instant.now().getEpochSecond().toString())
    (for {
      dbTimeout <- fixture.resourceDBLayer.map(_.dbTimeout)
      _         <- Resource.eval(dbTimeout.setTimeout(timeout))
      timeout   <- Resource.eval(dbTimeout.getOrDefault(chatId, testBotId))
    } yield
      assertEquals(timeout.chat_id, 2L)
      assertEquals(timeout.timeout_value, "2000")
    ).use_
  }

  databaseFixture.test("DBTimeout.setTimeout should update the timeout if the chat id is present in the database") {
    fixture =>
      val chatId  = 1L
      val timeout =
        DBTimeoutData(chatId, testBotId, 2.seconds.toMillis.toString, Instant.now().getEpochSecond().toString())
      (for {
        dbTimeout <- fixture.resourceDBLayer.map(_.dbTimeout)
        _         <- Resource.eval(dbTimeout.setTimeout(timeout))
        result    <- Resource.eval(dbTimeout.getOrDefault(chatId, testBotId))
      } yield
        assertEquals(result.chat_id, 1L)
        assertEquals(result.timeout_value, "2000")
      ).use_
  }

  databaseFixture.test(
    "DBTimeout.logLastInteraction should insert the timeout if the chat id is not present in the database"
  ) { fixture =>
    val chatId = 2L
    (for {
      dbTimeout <- fixture.resourceDBLayer.map(_.dbTimeout)
      _         <- Resource.eval(dbTimeout.logLastInteraction(chatId, testBotId))
      timeout   <- Resource.eval(dbTimeout.getOrDefault(chatId, testBotId))
    } yield
      assertEquals(timeout.chat_id, 2L)
      assertEquals(timeout.timeout_value, "0")
    ).use_

  }

  databaseFixture.test(
    "DBTimeout.logLastInteraction should update the timeout last interaction if the chat id is present in the database"
  ) { fixture =>
    val chatId = 1L

    (for {
      dbTimeout    <- fixture.resourceDBLayer.map(_.dbTimeout)
      _            <- Resource.eval(dbTimeout.setTimeout(testTimeout))
      prevTimeout  <- Resource.eval(dbTimeout.getOrDefault(chatId, testBotId))
      _            <- Resource.eval(dbTimeout.logLastInteraction(chatId, testBotId))
      afterTimeout <- Resource.eval(dbTimeout.getOrDefault(chatId, testBotId))
    } yield
      assertEquals(prevTimeout.chat_id, 1L)
      assertEquals(prevTimeout.timeout_value, "15000")
      assertEquals(afterTimeout.chat_id, 1L)
      assertEquals(afterTimeout.timeout_value, "15000")
      assert(prevTimeout.last_interaction.toLong < afterTimeout.last_interaction.toLong)
    ).use_
  }

  databaseFixture.test(
    "DBTimeout.removeTimeout should remove the timeout if the chat id and botName are present in the database"
  ) { fixture =>
    val chatId  = 1L
    val timeout =
      DBTimeoutData(chatId, testBotId, 2.seconds.toMillis.toString, Instant.now().getEpochSecond().toString())
    (for {
      dbTimeout     <- fixture.resourceDBLayer.map(_.dbTimeout)
      _             <- Resource.eval(dbTimeout.setTimeout(timeout))
      beforeTimeout <- Resource.eval(dbTimeout.getOrDefault(chatId, testBotId))
      _             <- Resource.eval(dbTimeout.removeTimeout(chatId, testBotId))
      afterTimeout  <- Resource.eval(dbTimeout.getOrDefault(chatId, testBotId))
    } yield
      assertEquals(beforeTimeout.chat_id, 1L)
      assertEquals(beforeTimeout.bot_id, testBotId)
      assertEquals(beforeTimeout.timeout_value, "2000")
      assertEquals(afterTimeout.chat_id, 1L)
      assertEquals(afterTimeout.bot_id, testBotId)
      assertEquals(afterTimeout.timeout_value, "0")
    ).use_
  }

  databaseFixture.test(
    "DBTimeout.removeTimeout should do nothing if the chat id and botName are not present in the database"
  ) { fixture =>
    val chatId = 1L
    (for {
      dbTimeout <- fixture.resourceDBLayer.map(_.dbTimeout)
      result    <- Resource.eval(dbTimeout.removeTimeout(chatId, testBotId)).attempt
    } yield assert(result.isRight)).use_
  }
}
