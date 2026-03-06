package com.benkio.integration.integrationmunit.chatcore.patterns

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.patterns.CommandPatterns.TimeoutCommand
import com.benkio.chatcore.repository.db.DBTimeoutData
import com.benkio.chattelegramadapter.SBot
import com.benkio.integration.DBFixture
import com.benkio.integrationtest.Logger.given
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import munit.CatsEffectSuite

class ITTimeoutCommandSpec extends CatsEffectSuite with DBFixture {

  val sBotConfig  = SBot.buildSBotConfig(RichardPHJBensonBot.sBotInfo)
  val botName     = sBotConfig.sBotInfo.botName
  val botId       = sBotConfig.sBotInfo.botId
  val chatIdValue = 0L
  val chatId      = ChatId(chatIdValue)

  val msg: Message = Message(messageId = 0, date = 0, chatId = chatId, chatType = "private")

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
            sBotInfo = sBotConfig.sBotInfo,
            ttl = sBotConfig.messageTimeToLive
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
            sBotInfo = sBotConfig.sBotInfo,
            ttl = sBotConfig.messageTimeToLive
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
            sBotInfo = sBotConfig.sBotInfo,
            ttl = sBotConfig.messageTimeToLive
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
