package com.benkio.chatcore.model

import cats.syntax.show.*
import munit.ScalaCheckSuite
import org.scalacheck.Prop.*

class MessageIdSpec extends ScalaCheckSuite {

  property("a Telegram message id keeps its number") {
    forAll { (id: Int) =>
      assertEquals(MessageId.telegram(id).value, id.toString)
      assertEquals(MessageId.telegram(id).telegramId, id)
    }
  }

  test("the ids of other platforms are kept as they are") {
    val slack   = MessageId("1700000000.000200")
    val discord = MessageId("1234567890123456789")
    assertEquals(slack.value, "1700000000.000200")
    assertEquals(discord.value, "1234567890123456789")
    assertEquals(slack.show, "1700000000.000200")
  }

  test("an id that is not a Telegram number is not a Telegram id") {
    val _ = intercept[NumberFormatException](MessageId("1700000000.000200").telegramId)
    val _ = intercept[NumberFormatException](MessageId("1234567890123456789").telegramId)
  }
}
