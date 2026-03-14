package com.benkio.chattelegramadapter.model

import munit.FunSuite
import telegramium.bots.Chat
import telegramium.bots.Message as TelegramMessage
import telegramium.bots.InaccessibleMessage
import telegramium.bots.MaybeInaccessibleMessage

class TelegramMessageIdsSpec extends FunSuite {
  val sharedChat: Chat = Chat(id = 123L, `type` = "private")

  test("getIds should extract ids from a Telegram Message") {
    val message: TelegramMessage = TelegramMessage(
      messageId = 10,
      date = 123L.toInt,
      chat = sharedChat,
      text = None,
      caption = None
    )
    val result = TelegramMessageIds.getIds(message)

    assertEquals(result.chatId, 123L)
    assertEquals(result.messageId, 10)
    assertEquals(result.chatType, "private")
  }

  test("getIds should extract ids from an InaccessibleMessage") {
    val message: InaccessibleMessage = InaccessibleMessage(
      messageId = 11,
      date = 456L.toInt,
      chat = sharedChat
    )
    val result = TelegramMessageIds.getIds(message)

    assertEquals(result.chatId, 123L)
    assertEquals(result.messageId, 11)
    assertEquals(result.chatType, "private")
  }

  test("getIds should preserve ids for both message variants") {
    val telegramMessage: MaybeInaccessibleMessage = TelegramMessage(
      messageId = 42,
      date = 999L.toInt,
      chat = sharedChat,
      text = None,
      caption = None
    )
    val inaccessibleMessage: MaybeInaccessibleMessage = InaccessibleMessage(
      messageId = 42,
      date = 999L.toInt,
      chat = sharedChat
    )

    assertEquals(TelegramMessageIds.getIds(telegramMessage), TelegramMessageIds.getIds(inaccessibleMessage))
  }
}
