package com.benkio.richardphjbensonbot

import munit._

import java.sql.Timestamp
import java.time.Instant

class DBTimeoutSpec extends FunSuite {
  test("Timeout.isExpired should return true when the timeout is expired") {
    val now = Timestamp.from(Instant.now())
    val timeout = Timeout(1L, "10", now)
    Thread.sleep(20)
    assert(Timeout.isExpired(timeout))
  }
  test("Timeout.isExpired should return false when the timeout is not expired") {
    val now = Timestamp.from(Instant.now())
    val timeout = Timeout(1L, "1000", now)
    assert(!Timeout.isExpired(timeout))
  }
}
