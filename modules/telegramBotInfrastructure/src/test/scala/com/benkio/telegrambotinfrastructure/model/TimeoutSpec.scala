package com.benkio.telegrambotinfrastructure.model

import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import munit.*

import java.time.Instant
import scala.concurrent.duration.*

class TimeoutSpec extends FunSuite {
  test("Timeout.isExpired should return true when the timeout is expired") {
    val timeout = Timeout(ChatId(1L), SBotId("botId"), 10.millis, Instant.now())
    Thread.sleep(20)
    assert(Timeout.isExpired(timeout))
  }
  test("Timeout.isExpired should return false when the timeout is not expired") {
    val timeout = Timeout(ChatId(1L), SBotId("botId"), 1000.millis, Instant.now())
    assert(!Timeout.isExpired(timeout))
  }
  test("Timeout.defaultTimeout should return the expected defaultTimeout") {
    val botId  = SBotId("botId")
    val actual = Timeout(ChatId(1L), botId)
    assertEquals(actual.chatId.value, 1L)
    assertEquals(actual.botId, botId)
    assertEquals(actual.timeoutValue, 0.millis)
  }
  test("Timeout.apply should return the expected timeout when the input is correct") {
    val timeout = "00:00:04"
    val actual  = Timeout(ChatId(1L), SBotId("botId"), timeout)
    assert(actual.isRight)
    assertEquals(actual.toOption.get.chatId.value, 1L)
    assertEquals(actual.toOption.get.timeoutValue, 4000.millis)
  }
  test("Timeout.apply should return None when the input is incorrect") {
    val timeout = "00:00:0a"
    val actual  = Timeout(ChatId(1L), SBotId("botId"), timeout)
    assert(actual.isLeft)
  }
  test("Timeout.timeStringToDuration return the expected duration if the input is in format hh:mm:ss") {
    assertEquals(Timeout.timeStringToDuration("20:32:45"), 73965.seconds)
  }
}
