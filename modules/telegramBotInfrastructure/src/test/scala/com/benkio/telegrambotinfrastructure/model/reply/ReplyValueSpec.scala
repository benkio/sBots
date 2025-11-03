package com.benkio.telegrambotinfrastructure.model.reply

import com.benkio.telegrambotinfrastructure.model.media.Media
import com.benkio.telegrambotinfrastructure.model.MimeType
import com.benkio.telegrambotinfrastructure.model.SBotId
import io.circe.parser.decode
import io.circe.syntax.*
import munit.FunSuite
import org.http4s.syntax.all.uri

import java.time.Instant

class ReplyValueSpec extends FunSuite {

  test("ReplyValue JSON decode/encode should work as expected") {
    val jsonInputs = List(
      """{
        |  "Text" : "testText"
        |}""".stripMargin,
      """{
        |  "Mp3File" : {
        |    "filepath" : "testFilePath.mp3",
        |    "replyToMessage" : false
        |  }
        |}""".stripMargin,
      """{
        |  "GifFile" : {
        |    "filepath" : "testFilePathGif.mp4",
        |    "replyToMessage" : false
        |  }
        |}""".stripMargin,
      """{
        |  "PhotoFile" : {
        |    "filepath" : "testFilePath.jpg",
        |    "replyToMessage" : false
        |  }
        |}""".stripMargin,
      """{
        |  "VideoFile" : {
        |    "filepath" : "testFilePath.mp4",
        |    "replyToMessage" : false
        |  }
        |}""".stripMargin
    )

    for inputString <- jsonInputs
    yield {
      val eitherMessageTrigger = decode[ReplyValue](inputString)
      eitherMessageTrigger.fold(
        e => fail("failed in parsing the input string as replyValue", e),
        ms => assertEquals(ms.asJson.toString, inputString)
      )
    }
  }

  test("MediaFile.fromMimeType should return the expected MediaFile from Media") {
    val actual = Media(
      mediaName = "botid_mediaName.mp4",
      botId = SBotId("botid"),
      kinds = List("kind"),
      mimeType = MimeType.MP4,
      mediaSources = List(Right(uri"http://something.com")),
      mediaCount = 0,
      createdAt = Instant.now()
    )
    val expected = VideoFile("botid_mediaName.mp4", false)
    assertEquals(MediaFile.fromMimeType(actual), expected)
  }
}
