package com.benkio.integration.integrationmunit.telegrambotinfrastructure.patterns

import cats.effect.IO
import cats.effect.Resource
import com.benkio.integration.DBFixture
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import com.benkio.telegrambotinfrastructure.model.ChatId
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.TimeoutCommand
import com.benkio.telegrambotinfrastructure.repository.db.DBTimeoutData
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

class ITTimeoutCommandSpec extends CatsEffectSuite with DBFixture {

  val botName     = RichardPHJBensonBot.sBotConfig.sBotInfo.botName
  val botId       = RichardPHJBensonBot.sBotConfig.sBotInfo.botId
  val chatIdValue = 0L
  val chatId      = ChatId(chatIdValue)

  val msg: Message = Message(
    messageId = 0,
    date = 0,
    chat = Chat(id = chatIdValue, `type` = "private")
  )

  databaseFixture.test(
    "TimeoutLogic Command should return an error string if the input is not properly formatted"
  ) { fixture =>
    val wrongInput = "00:00:0F"
    val result     = for {
      dbLayer       <- fixture.resourceDBLayer
      beforeTimeout <- Resource.eval(dbLayer.dbTimeout.getOrDefault(chatId = chatIdValue, botId = botId))
      reply         <- Resource.eval(
        TimeoutCommand
          .timeoutLogic[IO](
            msg = msg.copy(text = Some(s"/timeout $wrongInput")),
            dbTimeout = dbLayer.dbTimeout,
            sBotInfo = RichardPHJBensonBot.sBotConfig.sBotInfo,
            ttl = RichardPHJBensonBot.sBotConfig.messageTimeToLive
          )
          .attempt
      )
      afterTimeout <- Resource.eval(dbLayer.dbTimeout.getOrDefault(chatId = chatIdValue, botId = botId))
    } yield {
      assertEquals(beforeTimeout, afterTimeout)
      assertEquals(
        reply.map(_.head.value),
        Right("Timeout set failed: wrong input format for 00:00:0F, the input must be in the form '/timeout 00:00:00'")
      )
    }
    result.use_
  }

  databaseFixture.test(
    "TimeoutLogic Command should return a successful string and create a timeout in the db if the input is properly formatted"
  ) { fixture =>
    val wrongInput = "00:00:10"
    val result     = for {
      dbLayer       <- fixture.resourceDBLayer
      beforeTimeout <- Resource.eval(dbLayer.dbTimeout.getOrDefault(chatId = chatIdValue, botId = botId))
      reply         <- Resource.eval(
        TimeoutCommand
          .timeoutLogic[IO](
            msg = msg.copy(text = Some(s"/timeout $wrongInput")),
            dbTimeout = dbLayer.dbTimeout,
            sBotInfo = RichardPHJBensonBot.sBotConfig.sBotInfo,
            ttl = RichardPHJBensonBot.sBotConfig.messageTimeToLive
          )
          .attempt
      )
      afterTimeout <- Resource.eval(dbLayer.dbTimeout.getOrDefault(chatId = chatIdValue, botId = botId))
    } yield {
      assertEquals(
        beforeTimeout,
        DBTimeoutData(
          chat_id = chatIdValue,
          bot_id = botId.value,
          timeout_value = "0",
          last_interaction = beforeTimeout.last_interaction
        )
      )
      assertEquals(
        afterTimeout,
        DBTimeoutData(
          chat_id = chatIdValue,
          bot_id = botId.value,
          timeout_value = "10000",
          last_interaction = afterTimeout.last_interaction
        )
      )
      assertEquals(
        reply.map(_.head.value),
        Right("Timeout set successfully to 00:00:10.000")
      )
    }
    result.use_
  }

  databaseFixture.test(
    "TimeoutLogic Command should remove the timeout in the db if the input is empty"
  ) { fixture =>
    val result = for {
      dbLayer       <- fixture.resourceDBLayer
      beforeTimeout <- Resource.eval(dbLayer.dbTimeout.getOrDefault(chatId = chatIdValue, botId = botId))
      reply         <- Resource.eval(
        TimeoutCommand
          .timeoutLogic[IO](
            msg = msg.copy(text = Some("/timeout ")),
            dbTimeout = dbLayer.dbTimeout,
            sBotInfo = RichardPHJBensonBot.sBotConfig.sBotInfo,
            ttl = RichardPHJBensonBot.sBotConfig.messageTimeToLive
          )
          .attempt
      )
      afterTimeout <- Resource.eval(dbLayer.dbTimeout.getOrDefault(chatId = chatIdValue, botId = botId))
    } yield {
      assertEquals(beforeTimeout.chat_id, afterTimeout.chat_id)
      assertEquals(beforeTimeout.bot_id, afterTimeout.bot_id)
      assertEquals(beforeTimeout.timeout_value, afterTimeout.timeout_value)
      assertEquals(
        reply.map(_.head.value),
        Right("Timeout removed")
      )
    }
    result.use_
  }
}
