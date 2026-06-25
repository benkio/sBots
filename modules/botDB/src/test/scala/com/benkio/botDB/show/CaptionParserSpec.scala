package com.benkio.botDB.show

import cats.effect.IO
import com.benkio.botDB.Logger.given
import munit.CatsEffectSuite

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

class CaptionParserSpec extends CatsEffectSuite {

  private val captionParser: CaptionParser[IO] = CaptionParser[IO]()

  test("CaptionParser.parsePlainCaptionSrt should parse only subtitle text from srt file") {
    val srtContent =
      """1
        |00:00:00,000 --> 00:00:01,000
        |Hello everyone
        |
        |2
        |00:00:01,500 --> 00:00:03,000
        |How are
        |you today?
        |""".stripMargin

    IO(Files.createTempFile("caption-parser-", ".srt"))
      .bracket(tempFile =>
        for {
          _      <- IO(Files.writeString(tempFile, srtContent, StandardCharsets.UTF_8))
          result <- captionParser.parsePlainCaptionSrt(tempFile)
        } yield assertEquals(result, Some("Hello everyone How are you today?"))
      )(tempFile => IO(Files.deleteIfExists(tempFile)).void)
  }

  test("CaptionParser.parsePlainCaptionSrt should return None for missing file") {
    val missingCaptionPath: Path = Path.of("target", s"caption-parser-missing-${UUID.randomUUID()}.srt")

    for {
      result <- captionParser.parsePlainCaptionSrt(missingCaptionPath)
    } yield assertEquals(result, None)
  }
}
