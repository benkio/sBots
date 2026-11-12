package com.benkio.chatcore.model

import munit.*

class ChatIdSpec extends FunSuite {

  test("chat ids on different platforms with the same id are different") {
    assertNotEquals(ChatId(Platform.Telegram, "1"), ChatId(Platform.Discord, "1"))
  }

  test("Platform.fromId should resolve known platforms and reject unknown ones") {
    assertEquals(Platform.fromId("irc"), Right(Platform.Irc))
    assertEquals(Platform.fromId("Slack"), Right(Platform.Slack))
    assert(Platform.fromId("matrix").isLeft)
  }
}
