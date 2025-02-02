package com.benkio.telegrambotinfrastructure.model

import com.benkio.telegrambotinfrastructure.model.media.Media
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
      mediaSources = List(Right(Uri.unsafeFromString("https://benkio.github.io"))),
      mediaCount = 0,
      createdAt = Instant.parse("2022-11-01T12:54:23Z")
    )
    assertEquals(input.show, """0    | test_name | ["https://benkio.github.io"]""")
  }
  test("Media.mediaListToString should return the expected string") {
    val input: Media = Media(
      mediaName = "test_name",
      kinds = List.empty,
      mediaSources = List(Right(Uri.unsafeFromString("https://benkio.github.io"))),
      mediaCount = 0,
      createdAt = Instant.parse("2022-11-01T12:54:23Z")
    )
    val input2: Media          = input.copy(mediaName = "test name 2", mediaCount = 1)
    val input3: Media          = input.copy(mediaName = "test name 3", mediaCount = 2)
    val inputList: List[Media] = List(input, input2, input3)
    assertEquals(
      Media.mediaListToString(inputList),
      """0    | test_name | ["https://benkio.github.io"]
1    | test name 2 | ["https://benkio.github.io"]
2    | test name 3 | ["https://benkio.github.io"]""".stripMargin
    )
  }

  test("Media.apply should correctly parse a valid db record") {
    val input: DBMediaData = DBMediaData(
      media_name = "rphjb_Animali.mp3",
      kinds = """"[\"kind\"]"""",
      media_sources =
        """"[\"https://www.dropbox.com/scl/fi/hjonp4gt8jqjgpnqf6wgh/rphjb_Animali.mp3?rlkey=oy88fu1htok2npygddon3q5oz&dl=1\"]"""",
      mime_type = "audio/mpeg",
      media_count = 0,
      created_at = "1662126018293"
    )

    val result = Media(input)
    assert(result.isRight)
    assert(
      result.map(_.mediaName) == Right("rphjb_Animali.mp3"),
      s"""[MediaSpec] Expected: Right("rphjb_Animali.mp3"), got: ${result.map(_.mediaName)}"""
    )
    assert(
      result.map(_.kinds) == Right(List("kind")),
      s"""[MediaSpec] Expected: Right(List("kind")), got: ${result.map(_.kinds)}"""
    )
    assert(
      result.map(_.mediaSources) == Right(
        List(
          Right(
            Uri.unsafeFromString(
              "https://www.dropbox.com/scl/fi/hjonp4gt8jqjgpnqf6wgh/rphjb_Animali.mp3?rlkey=oy88fu1htok2npygddon3q5oz&dl=1"
            )
          )
        )
      ),
      s"""[MediaSpec] Expected: List(Right(uri"https://www.dropbox.com/scl/fi/hjonp4gt8jqjgpnqf6wgh/rphjb_Animali.mp3?rlkey=oy88fu1htok2npygddon3q5oz&dl=1")), got: ${result
          .map(_.mediaSources)}"""
    )
    assert(
      result.map(_.mediaCount) == Right(0),
      s"""[MediaSpec] Expected: Right(0), got: ${result.map(_.mediaCount)}"""
    )
  }
}
