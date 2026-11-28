package com.benkio.chattelegramadapter.conversions

import com.benkio.chatcore.model.Mention
import com.benkio.chatcore.model.SBotInfo.SBotName
import com.benkio.chattelegramadapter.conversions.MessageConversions.toModel
import com.benkio.chattelegramadapter.conversions.MessageConversions.toModelMessage
import com.benkio.chattelegramadapter.model.TelegramIds
import com.benkio.chattelegramadapter.model.TelegramToken
import com.benkio.chattelegramadapter.model.TelegramToken.botId
import com.benkio.chattelegramadapter.Arbitraries.given
import iozhik.OpenEnum
import munit.FunSuite
import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import telegramium.bots.Chat
import telegramium.bots.InaccessibleMessage
import telegramium.bots.MaybeInaccessibleMessage
import telegramium.bots.MentionMessageEntity
import telegramium.bots.Message as TelegramMessage
import telegramium.bots.TextMentionMessageEntity
import telegramium.bots.TextQuote
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

  test("toModel should map the mentions, the replied message and the quote") {
    val bot     = TelegramUser(id = 9L, isBot = true, firstName = "bot", username = Some("SBotsBot"))
    val replied = TelegramMessage(
      messageId = 5,
      date = 0,
      chat = Chat(id = 1L, `type` = "group"),
      from = Some(bot),
      text = Some("earlier")
    )
    val message = TelegramMessage(
      messageId = 6,
      date = 0,
      chat = Chat(id = 1L, `type` = "group"),
      text = Some("hi @SBotsBot you"),
      entities = List(OpenEnum.Known(MentionMessageEntity(offset = 3, length = 9))),
      replyToMessage = Some(replied),
      quote = Some(TextQuote(text = "earlier", position = 0))
    )

    val model = message.toModel()
    assertEquals(model.mentions, List(Mention(name = Some("SBotsBot"), text = Some("@SBotsBot"))))
    assertEquals(model.replyTo.flatMap(_.authorName), Some("SBotsBot"))
    assertEquals(model.replyTo.flatMap(_.text), Some("earlier"))
    assertEquals(model.quote, Some("earlier"))
    assert(model.isDirectedAt(SBotName("SBotsBot")))
  }

  test("toModel keeps the visible span of a text mention, whose display name can differ from the username") {
    val user    = TelegramUser(id = 7L, isBot = false, firstName = "Alice Smith", username = Some("alice"))
    val message = TelegramMessage(
      messageId = 7,
      date = 0,
      chat = Chat(id = 1L, `type` = "group"),
      text = Some("ciao Alice Smith!"),
      entities = List(OpenEnum.Known(TextMentionMessageEntity(offset = 5, length = 11, user = user)))
    )

    val model = message.toModel()
    assertEquals(model.mentions, List(Mention(id = Some("7"), name = Some("alice"), text = Some("Alice Smith"))))
    assert(model.isDirectedAt(SBotName("alice"))) // the username is still there for dispatch
  }

  test("toModel of a plain message is not directed at the bot") {
    val message = TelegramMessage(messageId = 1, date = 0, chat = Chat(id = 1L, `type` = "group"), text = Some("hi"))
    assert(!message.toModel().isDirectedAt(SBotName("SBotsBot")))
  }
}
