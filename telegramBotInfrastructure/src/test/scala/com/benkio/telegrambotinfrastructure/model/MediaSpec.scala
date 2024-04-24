package com.benkio.telegrambotinfrastructure.model

import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import munit.*
import org.http4s.Uri

import java.time.Instant

class MediaSpec extends FunSuite {
  test("Media show instance should return the expected string") {
    val input: Media = Media(
      mediaName = "test_name",
      kinds = List.empty,
      mediaUrl = Uri.unsafeFromString("https://benkio.github.io"),
      mediaCount = 0,
      createdAt = Instant.parse("2022-11-01T12:54:23Z")
    )
    assertEquals(input.show, "0    | test_name | https://benkio.github.io")
  }
  test("Media.mediaListToString should return the expected string") {
    val input: Media = Media(
      mediaName = "test_name",
      kinds = List.empty,
      mediaUrl = Uri.unsafeFromString("https://benkio.github.io"),
      mediaCount = 0,
      createdAt = Instant.parse("2022-11-01T12:54:23Z")
    )
    val input2: Media          = input.copy(mediaName = "test name 2", mediaCount = 1)
    val input3: Media          = input.copy(mediaName = "test name 3", mediaCount = 2)
    val inputList: List[Media] = List(input, input2, input3)
    assertEquals(
      Media.mediaListToString(inputList),
      """0    | test_name | https://benkio.github.io
1    | test name 2 | https://benkio.github.io
2    | test name 3 | https://benkio.github.io""".stripMargin
    )
  }

  test("Media.apply should correctly parse a valid db record") {
    val input: DBMediaData = DBMediaData(
      media_name = "rphjb_Animali.mp3",
      kinds = Some("[]"),
      media_url =
        "https://www.dropbox.com/scl/fi/hjonp4gt8jqjgpnqf6wgh/rphjb_Animali.mp3?rlkey=oy88fu1htok2npygddon3q5oz&dl=1",
      media_count = 0,
      created_at = "1662126018293"
    )

    assert(Media(input).isRight)
  }
}
