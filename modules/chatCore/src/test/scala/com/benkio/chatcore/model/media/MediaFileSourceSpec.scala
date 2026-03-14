package com.benkio.chatcore.model.media

import cats.effect.IO
import com.benkio.chatcore.model.MimeType
import munit.CatsEffectSuite
import org.http4s.syntax.all.uri

class MediaFileSourceSpec extends CatsEffectSuite {
  test("MediaFileSource.fromUriString should parse a valid URI") {
    val input    = "https://example.com/test.bot_mediaFile.mp3"
    val expected = MediaFileSource(
      filename = "test.bot_mediaFile.mp3",
      kinds = List.empty,
      mime = MimeType.MPEG,
      sources = List(Right(uri"https://example.com/test.bot_mediaFile.mp3"))
    )

    val actual: IO[MediaFileSource] = MediaFileSource.fromUriString[IO](input)
    assertIO(actual, expected)
  }

  test("MediaFileSource.fromUriString should fail for invalid URI") {
    val input = "https://exa mple.com"
    assertIO(
      MediaFileSource.fromUriString[IO](input).attempt.map(_.isLeft),
      true
    )
  }

  test("MediaFileSource.fromUriString should fail if filename does not contain `_`") {
    val input = "https://example.com/mediaFile.mp3"
    assertIO(
      MediaFileSource.fromUriString[IO](input).attempt.map(_.isLeft),
      true
    )
  }
}
