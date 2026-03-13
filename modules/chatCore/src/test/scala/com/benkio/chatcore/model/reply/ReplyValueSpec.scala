package com.benkio.chatcore.model.reply

import com.benkio.chatcore.model.media.Media
import com.benkio.chatcore.model.MimeType
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.Arbitraries.given
import io.circe.parser.decode
import io.circe.syntax.*
import munit.FunSuite
import munit.ScalaCheckEffectSuite
import org.http4s.syntax.all.uri
import org.scalacheck.Prop.forAll

import java.time.Instant

class ReplyValueSpec extends FunSuite with ScalaCheckEffectSuite {

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

  test("ReplyValue.from should downcast to the requested ReplyValue subtype") {
    forAll { (replyValue: ReplyValue) =>
      replyValue match {
        case text: Text           => assertEquals(ReplyValue.from[Text](replyValue), Some(text))
        case mp3File: Mp3File     => assertEquals(ReplyValue.from[Mp3File](replyValue), Some(mp3File))
        case gifFile: GifFile     => assertEquals(ReplyValue.from[GifFile](replyValue), Some(gifFile))
        case photoFile: PhotoFile => assertEquals(ReplyValue.from[PhotoFile](replyValue), Some(photoFile))
        case videoFile: VideoFile => assertEquals(ReplyValue.from[VideoFile](replyValue), Some(videoFile))
        case document: Document   => assertEquals(ReplyValue.from[Document](replyValue), Some(document))
        case sticker: Sticker     => assertEquals(ReplyValue.from[Sticker](replyValue), Some(sticker))
      }
    }
  }
}
