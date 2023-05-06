package com.benkio.telegrambotinfrastructure.model

import munit._

import java.time.Instant
import scala.concurrent.duration._

class TimeoutSpec extends FunSuite {
  test("Timeout.isExpired should return true when the timeout is expired") {
    val timeout = Timeout(1L, "botName", 10.millis, Instant.now())
    Thread.sleep(20)
    assert(Timeout.isExpired(timeout))
  }
  test("Timeout.isExpired should return false when the timeout is not expired") {
    val timeout = Timeout(1L, "botName", 1000.millis, Instant.now())
    assert(!Timeout.isExpired(timeout))
  }
  test("Timeout.defaultTimeout should return the expected defaultTimeout") {
    val botName = "botName"
    val actual  = Timeout(1L, botName)
    assertEquals(actual.chatId, 1L)
    assertEquals(actual.botName, botName)
    assertEquals(actual.timeoutValue, 0.millis)
  }
  test("Timeout.apply should return the expected timeout when the input is correct") {
    val timeout = "00:00:04"
    val actual  = Timeout(1L, "botName", timeout)
    assert(actual.isRight)
    assertEquals(actual.toOption.get.chatId, 1L)
    assertEquals(actual.toOption.get.timeoutValue, 4000.millis)
  }
  test("Timeout.apply should return None when the input is incorrect") {
    val timeout = "00:00:0a"
    val actual  = Timeout(1L, "botName", timeout)
    assert(actual.isLeft)
  }
  test("Timeout.timeStringToDuration return the expected duration if the input is in format hh:mm:ss") {
    assertEquals(Timeout.timeStringToDuration("20:32:45"), 73965.seconds)
  }
}
