package com.benkio.integration.integrationmunit.chatcore.patterns

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.patterns.CommandPatterns.GetTimeoutCommand
import com.benkio.chatcore.patterns.CommandPatterns.SetTimeoutCommand
import com.benkio.chattelegramadapter.SBot
import com.benkio.integration.DBFixture
import com.benkio.integrationtest.Logger.given
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import munit.CatsEffectSuite

class ITGetTimeoutCommandSpec extends CatsEffectSuite with DBFixture {

  val sBotConfig  = SBot.buildSBotConfig(RichardPHJBensonBot.sBotInfo)
  val botName     = sBotConfig.sBotInfo.botName
  val botId       = sBotConfig.sBotInfo.botId
  val chatIdValue = 0L
  val chatId      = ChatId(chatIdValue)

  val msg: Message = Message(messageId = 0, date = 0, chatId = chatId, chatType = "private")

  databaseFixture.test(
    "GetTimeoutLogic Command should return the current chat timeout"
  ) { fixture =>
    val input  = "00:00:10"
    val result = for {
      dbLayer <- fixture.resourceDBLayer
      _       <- Resource.eval(
        SetTimeoutCommand
          .setTimeoutLogic[IO](
            msg = msg.copy(text = Some(s"/settimeout $input")),
            dbTimeout = dbLayer.dbTimeout,
            sBotInfo = sBotConfig.sBotInfo,
            ttl = sBotConfig.messageTimeToLive
          )
          .attempt
      )
      reply <- Resource.eval(
        GetTimeoutCommand
          .getTimeoutLogic[IO](
            msg = msg.copy(text = Some("/gettimeout")),
            dbTimeout = dbLayer.dbTimeout,
            sBotInfo = sBotConfig.sBotInfo,
            ttl = sBotConfig.messageTimeToLive
          )
          .attempt
      )
    } yield {
      assertEquals(
        reply.map(_.show),
        Right("The Timeout is 00:00:10.000")
      )
    }
    result.use_
  }
}
