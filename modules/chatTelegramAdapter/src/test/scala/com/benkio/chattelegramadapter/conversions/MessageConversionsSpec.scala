package com.benkio.chattelegramadapter.conversions

import com.benkio.chattelegramadapter.conversions.MessageConversions.toModel
import com.benkio.chattelegramadapter.conversions.MessageConversions.toModelMessage
import com.benkio.chattelegramadapter.model.TelegramIds
import com.benkio.chattelegramadapter.model.TelegramToken
import com.benkio.chattelegramadapter.model.TelegramToken.botId
import com.benkio.chattelegramadapter.Arbitraries.given
import munit.FunSuite
import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import telegramium.bots.Chat
import telegramium.bots.InaccessibleMessage
import telegramium.bots.MaybeInaccessibleMessage
import telegramium.bots.Message as TelegramMessage
import telegramium.bots.User as TelegramUser

class MessageConversionsSpec extends ScalaCheckSuite {
  test("toModelMessage should convert MaybeInaccessibleMessage to core Message when possible") {
    {
      forAll { (maybeInaccessibleMessage: MaybeInaccessibleMessage) =>
        val message = maybeInaccessibleMessage.toModelMessage
        assert(message.isDefined)
        assertEquals(
          message.get.messageId,
          maybeInaccessibleMessage match {
            case msg: TelegramMessage     => Some(TelegramIds.messageId(msg.messageId))
            case msg: InaccessibleMessage => Some(TelegramIds.messageId(msg.messageId))
          }
        )
      }
    }
  }

  test("toModel should split the bot itself out of the new chat members") {
    val botId   = 42L
    val token   = TelegramToken(s"$botId:secret")
    val bot     = TelegramUser(id = botId, isBot = true, firstName = "bot")
    val other   = TelegramUser(id = 7L, isBot = false, firstName = "other")
    val message = TelegramMessage(
      messageId = 1,
      date = 0,
      chat = Chat(id = 1L, `type` = "group"),
      newChatMembers = List(bot, other)
    )

    val withBotId = message.toModel(token.botId)
    assert(withBotId.botAdded)
    assertEquals(withBotId.newChatMembers.map(_.id), List(7L))

    val withoutBotId = message.toModel()
    assert(!withoutBotId.botAdded)
    assertEquals(withoutBotId.newChatMembers.map(_.id), List(botId, 7L))
  }
}
