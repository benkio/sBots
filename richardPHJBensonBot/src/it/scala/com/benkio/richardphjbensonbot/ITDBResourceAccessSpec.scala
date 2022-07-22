package com.benkio.richardphjbensonbot

import munit._

class ITDBResourceAccessSpec extends FunSuite {
  test("getResourceByteArray should return the expected content") {
    val obtained = 42
    val expected = 43
    assertEquals(obtained, expected)
  }
    test("getResourcesByKind should return the expected list of files with expected content") {
    val obtained = 42
    val expected = 43
    assertEquals(obtained, expected)
  }
}
