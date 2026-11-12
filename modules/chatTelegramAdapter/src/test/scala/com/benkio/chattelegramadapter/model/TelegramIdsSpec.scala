package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.Platform
import com.benkio.chattelegramadapter.model.TelegramIds.telegramId
import munit.ScalaCheckSuite
import org.scalacheck.Prop.*

class TelegramIdsSpec extends ScalaCheckSuite {

  test("chatId should build a Telegram chat id with a string id") {
    assertEquals(TelegramIds.chatId(123L), ChatId(Platform.Telegram, "123"))
  }

  test("telegramId should return the numeric id of a Telegram chat id") {
    assertEquals(TelegramIds.chatId(-42L).telegramId, -42L)
  }

  test("telegramId should fail for chat ids of other platforms") {
    val _ = intercept[IllegalArgumentException](ChatId(Platform.Irc, "#channel").telegramId)
  }

  property("a Telegram message id keeps its number") {
    forAll { (id: Int) =>
      assertEquals(TelegramIds.messageId(id).value, id.toString)
      assertEquals(TelegramIds.messageId(id).telegramId, id)
    }
  }

  test("an id that is not a Telegram number is not a Telegram id") {
    val _ = intercept[NumberFormatException](MessageId("1700000000.000200").telegramId)
    val _ = intercept[NumberFormatException](MessageId("1234567890123456789").telegramId)
  }
}
