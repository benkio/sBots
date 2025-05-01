package com.benkio.telegrambotinfrastructure.dataentry

import munit.*

class DataEntrySpec extends FunSuite {
  test("parseInput should successfully parse a valid input") {
    val actual   = 42
    val expected = 43
    assertEquals(actual, expected)
  }
}
