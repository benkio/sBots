package com.benkio.chatcore

import munit.FunSuite

import scala.concurrent.duration.*

class AutoTimeoutSpec extends FunSuite {

  test("forMembers is logarithmic in the number of members") {
    assertEquals(AutoTimeout.forMembers(2), 5.seconds)
    assertEquals(AutoTimeout.forMembers(10), 17.seconds)
    assertEquals(AutoTimeout.forMembers(100), 33.seconds)
    assertEquals(AutoTimeout.forMembers(1000), 50.seconds)
    assertEquals(AutoTimeout.forMembers(10000), 66.seconds)
  }

  test("forMembers is never under the minimum") {
    assertEquals(AutoTimeout.forMembers(1), AutoTimeout.minimum)
    assertEquals(AutoTimeout.forMembers(0), AutoTimeout.minimum)
  }
}
