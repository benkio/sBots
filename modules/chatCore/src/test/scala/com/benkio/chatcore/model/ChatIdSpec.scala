package com.benkio.chatcore.model

import munit.*

class ChatIdSpec extends FunSuite {

  test("ChatId.telegram should build a Telegram chat id with a string id") {
    assertEquals(ChatId.telegram(123L), ChatId(Platform.Telegram, "123"))
  }

  test("telegramId should return the numeric id of a Telegram chat id") {
    assertEquals(ChatId.telegram(-42L).telegramId, -42L)
  }

  test("telegramId should fail for chat ids of other platforms") {
    intercept[IllegalArgumentException](ChatId(Platform.Irc, "#channel").telegramId)
  }

  test("chat ids on different platforms with the same id are different") {
    assertNotEquals(ChatId(Platform.Telegram, "1"), ChatId(Platform.Discord, "1"))
  }

  test("Platform.fromId should resolve known platforms and reject unknown ones") {
    assertEquals(Platform.fromId("irc"), Right(Platform.Irc))
    assertEquals(Platform.fromId("Slack"), Right(Platform.Slack))
    assert(Platform.fromId("matrix").isLeft)
  }
}
