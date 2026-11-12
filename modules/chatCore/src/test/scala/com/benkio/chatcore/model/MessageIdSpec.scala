package com.benkio.chatcore.model

import cats.syntax.show.*
import munit.FunSuite

class MessageIdSpec extends FunSuite {

  test("the ids of other platforms are kept as they are") {
    val slack   = MessageId("1700000000.000200")
    val discord = MessageId("1234567890123456789")
    assertEquals(slack.value, "1700000000.000200")
    assertEquals(discord.value, "1234567890123456789")
    assertEquals(slack.show, "1700000000.000200")
  }
}
