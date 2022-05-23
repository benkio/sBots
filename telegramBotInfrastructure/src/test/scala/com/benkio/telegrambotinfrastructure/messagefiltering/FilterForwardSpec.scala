package com.benkio.telegrambotinfrastructure.messagefiltering

import munit._

class FilterForwardSpec extends FunSuite {
  test("FilteringForward should return true if the disableForward is false") {
    val obtained = 42
    val expected = 43
    assertEquals(obtained, expected)
  }

  test("FilteringForward should return true if the disableForward is true and the message is not a forward") {
    ???
  }

  test("FilteringForward should return false if the disableForward is true and the message is a forward") {
    ???
  }
}
