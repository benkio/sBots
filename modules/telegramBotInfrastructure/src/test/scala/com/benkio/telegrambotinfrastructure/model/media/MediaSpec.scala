package com.benkio.telegrambotinfrastructure.model.media

import com.benkio.telegrambotinfrastructure.model.MimeType
import com.benkio.telegrambotinfrastructure.model.SBotId
import com.benkio.telegrambotinfrastructure.repository.db.DBMediaData
import munit.*
import org.http4s.Uri

import java.time.Instant

class MediaSpec extends FunSuite {
  test("Media show instance should return the expected string") {
    val input: Media = Media(
      mediaName = "test_name",
      botId = SBotId("botid"),
      kinds = List.empty,
      mimeType = MimeType.MP4,
      mediaSources = List(Right(Uri.unsafeFromString("https://benkio.github.io"))),
      mediaCount = 0,
      createdAt = Instant.parse("2022-11-01T12:54:23Z")
    )
    assertEquals(input.toHtmlRow, """| 0     | <a href="https://benkio.github.io">test_name</a> |""")
  }
  test("Media.mediaListToHTML should return the expected string") {
    val input: Media = Media(
      mediaName = "test_name",
      botId = SBotId("botid"),
      kinds = List.empty,
      mimeType = MimeType.MP4,
      mediaSources = List(Right(Uri.unsafeFromString("https://benkio.github.io"))),
      mediaCount = 0,
      createdAt = Instant.parse("2022-11-01T12:54:23Z")
    )
    val input2: Media          = input.copy(mediaName = "test name 2", mediaCount = 1)
    val input3: Media          = input.copy(mediaName = "test name 3", mediaCount = 2)
    val inputList: List[Media] = List(input, input2, input3)
    assertEquals(
      Media.mediaListToHTML(inputList),
      """<pre>
| Count | File                |
|-------|---------------------|
| 0     | <a href="https://benkio.github.io">test_name</a> |
| 1     | <a href="https://benkio.github.io">test name 2</a> |
| 2     | <a href="https://benkio.github.io">test name 3</a> |
</pre>"""
    )
  }

  test("Media.apply should correctly parse a valid db record") {
    val input: DBMediaData = DBMediaData(
      media_name = "rphjb_Animali.mp3",
      bot_id = "rphjb",
      kinds = """"[\"kind\"]"""",
      media_sources =
        """"[\"CQACAgQAAxkBAAEC15xnn5dbOnZ6NOodXkiKvxMsJrHqVAACjRgAAjmJAVFDx6HUqYYeWzYE\",\"https://www.dropbox.com/scl/fi/hjonp4gt8jqjgpnqf6wgh/rphjb_Animali.mp3?rlkey=oy88fu1htok2npygddon3q5oz&dl=1\"]"""",
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
          Left("CQACAgQAAxkBAAEC15xnn5dbOnZ6NOodXkiKvxMsJrHqVAACjRgAAjmJAVFDx6HUqYYeWzYE"),
          Right(
            Uri.unsafeFromString(
              "https://www.dropbox.com/scl/fi/hjonp4gt8jqjgpnqf6wgh/rphjb_Animali.mp3?rlkey=oy88fu1htok2npygddon3q5oz&dl=1"
            )
          )
        )
      ),
      s"""[MediaSpec] Expected: List(Left("CQACAgQAAxkBAAEC15xnn5dbOnZ6NOodXkiKvxMsJrHqVAACjRgAAjmJAVFDx6HUqYYeWzYE"),Right(uri"https://www.dropbox.com/scl/fi/hjonp4gt8jqjgpnqf6wgh/rphjb_Animali.mp3?rlkey=oy88fu1htok2npygddon3q5oz&dl=1")), got: ${result
          .map(_.mediaSources)}"""
    )
    assert(
      result.map(_.mediaCount) == Right(0),
      s"""[MediaSpec] Expected: Right(0), got: ${result.map(_.mediaCount)}"""
    )
  }
}
