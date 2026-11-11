package com.benkio.integration.integrationmunit.chatcore.repository.db

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBTimeout
import com.benkio.chatcore.repository.db.DBTimeoutData
import com.benkio.integration.DBFixture
import doobie.munit.analysisspec.IOChecker
import doobie.Transactor
import munit.CatsEffectSuite

import java.sql.DriverManager
import java.time.Instant
import scala.concurrent.duration.*

class ITDBTimeoutSpec extends CatsEffectSuite with DBFixture with IOChecker {
  val testTimeoutChatId = 1L
  val testBotId         = SBotId("botId")

  val testTimeout: DBTimeoutData = DBTimeoutData(
    platform = "telegram",
    chat_id = testTimeoutChatId.toString,
    bot_id = testBotId.value,
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
    check(DBTimeout.getOrDefaultQuery(ChatId.telegram(testTimeoutChatId), testBotId))
    check(DBTimeout.setTimeoutQuery(testTimeout))
    check(DBTimeout.removeTimeoutQuery(ChatId.telegram(testTimeoutChatId), testBotId))
    check(DBTimeout.logLastInteractionQuery(ChatId.telegram(testTimeoutChatId), testBotId))
  }

  databaseFixture.test(
    "DBTimeout.getOrDefault should return the default timeout if the chat id is not present in the database"
  ) { fixture =>
    (for {
      // Not present ChatID
      timeout <- fixture.resourceDBLayer.evalMap(dbLayer =>
        dbLayer.dbTimeout.getOrDefault(ChatId.telegram(100L), testBotId)
      )
    } yield {
      assertEquals(timeout.chat_id, "100")
      assertEquals(timeout.timeout_value, "0")
    }).use_
  }

  databaseFixture.test(
    "DBTimeout.getOrDefault should return the timeout if the chat id is present in the database"
  ) { fixture =>
    (for {
      dbTimeout <- fixture.resourceDBLayer.map(_.dbTimeout)
      _         <- Resource.eval(dbTimeout.setTimeout(testTimeout))
      timeout   <- Resource.eval(dbTimeout.getOrDefault(ChatId.telegram(1L), testBotId)) // Present ChatID
    } yield {
      assertEquals(timeout.chat_id, "1")
      assertEquals(timeout.timeout_value, "15000")
    }).use_
  }

  databaseFixture.test(
    "DBTimeout.setTimeout should insert the timeout if the chat id is not present in the database"
  ) { fixture =>
    val chatId  = 2L
    val timeout =
      DBTimeoutData(
        "telegram",
        chatId.toString,
        testBotId.value,
        2.seconds.toMillis.toString,
        Instant.now().getEpochSecond().toString()
      )
    (for {
      dbTimeout <- fixture.resourceDBLayer.map(_.dbTimeout)
      _         <- Resource.eval(dbTimeout.setTimeout(timeout))
      timeout   <- Resource.eval(dbTimeout.getOrDefault(ChatId.telegram(chatId), testBotId))
    } yield {
      assertEquals(timeout.chat_id, "2")
      assertEquals(timeout.timeout_value, "2000")
    }).use_
  }

  databaseFixture.test("DBTimeout.setTimeout should update the timeout if the chat id is present in the database") {
    fixture =>
      val chatId  = 1L
      val timeout =
        DBTimeoutData(
          "telegram",
          chatId.toString,
          testBotId.value,
          2.seconds.toMillis.toString,
          Instant.now().getEpochSecond().toString()
        )
      (for {
        dbTimeout <- fixture.resourceDBLayer.map(_.dbTimeout)
        _         <- Resource.eval(dbTimeout.setTimeout(timeout))
        result    <- Resource.eval(dbTimeout.getOrDefault(ChatId.telegram(chatId), testBotId))
      } yield {
        assertEquals(result.chat_id, "1")
        assertEquals(result.timeout_value, "2000")
      }).use_
  }

  databaseFixture.test(
    "DBTimeout.logLastInteraction should insert the timeout if the chat id is not present in the database"
  ) { fixture =>
    val chatId = 2L
    (for {
      dbTimeout <- fixture.resourceDBLayer.map(_.dbTimeout)
      _         <- Resource.eval(dbTimeout.logLastInteraction(ChatId.telegram(chatId), testBotId))
      timeout   <- Resource.eval(dbTimeout.getOrDefault(ChatId.telegram(chatId), testBotId))
    } yield {
      assertEquals(timeout.chat_id, "2")
      assertEquals(timeout.timeout_value, "0")
    }).use_

  }

  databaseFixture.test(
    "DBTimeout.logLastInteraction should update the timeout last interaction if the chat id is present in the database"
  ) { fixture =>
    val chatId = 1L

    (for {
      dbTimeout    <- fixture.resourceDBLayer.map(_.dbTimeout)
      _            <- Resource.eval(dbTimeout.setTimeout(testTimeout))
      prevTimeout  <- Resource.eval(dbTimeout.getOrDefault(ChatId.telegram(chatId), testBotId))
      _            <- Resource.eval(dbTimeout.logLastInteraction(ChatId.telegram(chatId), testBotId))
      afterTimeout <- Resource.eval(dbTimeout.getOrDefault(ChatId.telegram(chatId), testBotId))
    } yield {
      assertEquals(prevTimeout.chat_id, "1")
      assertEquals(prevTimeout.timeout_value, "15000")
      assertEquals(afterTimeout.chat_id, "1")
      assertEquals(afterTimeout.timeout_value, "15000")
      assert(prevTimeout.last_interaction.toLong < afterTimeout.last_interaction.toLong)
    }).use_
  }

  databaseFixture.test(
    "DBTimeout.removeTimeout should remove the timeout if the chat id and botName are present in the database"
  ) { fixture =>
    val chatId  = 1L
    val timeout =
      DBTimeoutData(
        "telegram",
        chatId.toString,
        testBotId.value,
        2.seconds.toMillis.toString,
        Instant.now().getEpochSecond().toString()
      )
    (for {
      dbTimeout     <- fixture.resourceDBLayer.map(_.dbTimeout)
      _             <- Resource.eval(dbTimeout.setTimeout(timeout))
      beforeTimeout <- Resource.eval(dbTimeout.getOrDefault(ChatId.telegram(chatId), testBotId))
      _             <- Resource.eval(dbTimeout.removeTimeout(ChatId.telegram(chatId), testBotId))
      afterTimeout  <- Resource.eval(dbTimeout.getOrDefault(ChatId.telegram(chatId), testBotId))
    } yield {
      assertEquals(beforeTimeout.chat_id, "1")
      assertEquals(beforeTimeout.bot_id, testBotId.value)
      assertEquals(beforeTimeout.timeout_value, "2000")
      assertEquals(afterTimeout.chat_id, "1")
      assertEquals(afterTimeout.bot_id, testBotId.value)
      assertEquals(afterTimeout.timeout_value, "0")
    }).use_
  }

  databaseFixture.test(
    "DBTimeout.removeTimeout should do nothing if the chat id and botName are not present in the database"
  ) { fixture =>
    val chatId = 1L
    (for {
      dbTimeout <- fixture.resourceDBLayer.map(_.dbTimeout)
      result    <- Resource.eval(dbTimeout.removeTimeout(ChatId.telegram(chatId), testBotId)).attempt
    } yield assert(result.isRight)).use_
  }

  databaseFixture.test("DBTimeout: the same chat id on two platforms should be two different timeouts") { fixture =>
    val telegram                                           = ChatId.telegram(5L)
    val irc                                                = ChatId(Platform.Irc, "5")
    def row(chatId: ChatId, millis: String): DBTimeoutData =
      DBTimeoutData(chatId.platform.id, chatId.id, testBotId.value, millis, "1")
    (for {
      dbTimeout    <- fixture.resourceDBLayer.map(_.dbTimeout)
      _            <- Resource.eval(dbTimeout.setTimeout(row(telegram, "1000")))
      _            <- Resource.eval(dbTimeout.setTimeout(row(irc, "2000")))
      telegramRead <- Resource.eval(dbTimeout.getOrDefault(telegram, testBotId))
      ircRead      <- Resource.eval(dbTimeout.getOrDefault(irc, testBotId))
      _            <- Resource.eval(dbTimeout.removeTimeout(telegram, testBotId))
      ircAfter     <- Resource.eval(dbTimeout.getOrDefault(irc, testBotId))
    } yield {
      assertEquals(telegramRead.timeout_value, "1000")
      assertEquals(ircRead.timeout_value, "2000")
      assertEquals(ircRead.platform, "irc")
      assertEquals(ircAfter.timeout_value, "2000")
    }).use_
  }
}
