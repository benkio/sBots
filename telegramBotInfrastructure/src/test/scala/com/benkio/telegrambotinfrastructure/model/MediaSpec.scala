package com.benkio.telegrambotinfrastructure.model

import cats.syntax.all._
import munit._

class MediaSpec extends FunSuite {
  test("Media show instance should return the expected string") {
    val input: Media = Media(
      media_name = "test_name",
      kind = None,
      media_url = "https://benkio.github.io",
      media_count = 0,
      created_at = "2022-11-01T12:54:23Z"
    )
    assertEquals(input.show, "0    | test_name | https://benkio.github.io")
  }
  test("Media.mediaListToString should return the expected string") {
    val input: Media = Media(
      media_name = "test_name",
      kind = None,
      media_url = "https://benkio.github.io",
      media_count = 0,
      created_at = "2022-11-01T12:54:23Z"
    )
    val input2: Media          = input.copy(media_name = "test name 2", media_count = 1)
    val input3: Media          = input.copy(media_name = "test name 3", media_count = 2)
    val inputList: List[Media] = List(input, input2, input3)
    assertEquals(
      Media.mediaListToString(inputList),
      """0    | test_name | https://benkio.github.io
1    | test name 2 | https://benkio.github.io
2    | test name 3 | https://benkio.github.io""".stripMargin
    )
  }
}
