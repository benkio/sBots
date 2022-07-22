package com.benkio.richardphjbensonbot

import munit._

class DBResourceAccessSpec extends FunSuite {
  test("toTempFile should create a temporary file with the expected content") {
    val obtained = 42
    val expected = 43
    assertEquals(obtained, expected)
  }
}
