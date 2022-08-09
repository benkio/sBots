package com.benkio.richardphjbensonbot

import munit._
import telegramium.bots.Chat
import telegramium.bots.Message

import java.sql.Timestamp
import java.time.Instant

class DBTimeoutSpec extends FunSuite {
  test("Timeout.isExpired should return true when the timeout is expired") {
    val now     = Timestamp.from(Instant.now())
    val timeout = Timeout(1L, "10", now)
    Thread.sleep(20)
    assert(Timeout.isExpired(timeout))
  }
  test("Timeout.isExpired should return false when the timeout is not expired") {
    val now     = Timestamp.from(Instant.now())
    val timeout = Timeout(1L, "1000", now)
    assert(!Timeout.isExpired(timeout))
  }
  test("Timeout.defaultTimeout should return the expected defaultTimeout") {
    val actual = Timeout.defaultTimeout(1L)
    assertEquals(actual.chat_id, 1L)
    assertEquals(actual.timeout_value, "0")
  }
  test("Timeout.apply should return the expected timeout when the input is correct") {
    val message = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 1, `type` = "test")
    )
    val timeout = "00:00:04"
    val actual  = Timeout(message, timeout)
    assert(actual.isDefined)
    assertEquals(actual.get.chat_id, 1L)
    assertEquals(actual.get.timeout_value, "4000")
  }
  test("Timeout.apply should return None when the input is incorrect") {
    val message = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 1, `type` = "test")
    )
    val timeout = "00:00:0a"
    val actual  = Timeout(message, timeout)
    assert(actual.isEmpty)
  }
}
