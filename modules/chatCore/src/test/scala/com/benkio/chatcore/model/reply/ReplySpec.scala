package com.benkio.chatcore.model.reply

import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.SBotInfo.SBotName
import io.circe.parser.decode
import io.circe.syntax.*
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll

class ReplySpec extends ScalaCheckSuite {

  test("Mp3File case class should accept only .mp3 extension when filename created") {
    val _ = Mp3File("test.mp3") // This shouldn't throw an exception
    intercept[IllegalArgumentException] { Mp3File("test.gif") }
  }
  test("GifFile case class should not accept .gif extension when filename created") {
    val _ = GifFile("testGif.mp4")
    intercept[IllegalArgumentException] { GifFile("test.gif") }
  }
  test("PhotoFile case class should accept only .jpg and .png extension when filenames create") {
    val _ = PhotoFile("test.jpg")
    val _ = PhotoFile("test.png")
    intercept[IllegalArgumentException] { PhotoFile("test.mp3") }
  }

  test("VideoFile case class should accept only .mp4 extension when filename created") {
    val _ = VideoFile("test.mp4")
    intercept[IllegalArgumentException] { VideoFile("test.mp3") }
  }

  test(
    "MediaFile apply should create the right MediaFile when a valid filename with an allowed extension is provided"
  ) {
    assertEquals(mp3"audio.mp3", mp3"audio.mp3")
    assertEquals(pho"picture.jpg", pho"picture.jpg")
    assertEquals(pho"picture.png", pho"picture.png")
    assertEquals(gif"gif.mp4", gif"gif.mp4")
    assertEquals(vid"video.mp4", vid"video.mp4")
  }

  test("MediaFile show instance should return the expected string") {
    assertEquals(MediaFile.showInstance.show(mp3"audio.mp3"), "audio.mp3")
    assertEquals(MediaFile.showInstance.show(pho"picture.jpg"), "picture.jpg")
    assertEquals(MediaFile.showInstance.show(pho"picture.png"), "picture.png")
    assertEquals(MediaFile.showInstance.show(gif"gif.mp4"), "gif.mp4")
    assertEquals(MediaFile.showInstance.show(vid"video.mp4"), "video.mp4")
  }

  test("Reply JSON decode/encode should work as expected") {
    // Test Code
    // import com.benkio.chatcore.model.reply.TextReply
    // val test: Reply[SyncIO] = TextReply[SyncIO](List(Text("testText")))
    // println(s"[ReplySpec] test: ${test.asJson.toString}")

    val jsonInputs = List(
      """{
        |  "TextReply" : {
        |    "text" : [
        |      "testText"
        |    ],
        |    "replyToMessage" : false
        |  }
        |}""".stripMargin,
      """{
        |  "MediaReply" : {
        |    "mediaFiles" : [
        |      {
        |        "VideoFile" : {
        |          "filepath" : "testFilePath.mp4",
        |          "replyToMessage" : false
        |        }
        |      }
        |    ],
        |    "replyToMessage" : false
        |  }
        |}""".stripMargin
    )

    for inputString <- jsonInputs
    yield {
      val eitherMessageTrigger = decode[Reply](inputString)
      eitherMessageTrigger.fold(
        e => fail("failed in parsing the input string as Reply", e),
        ms => assertEquals(ms.asJson.toString, inputString)
      )
    }
  }

  property("Reply.prettyPrint should format TextReply, MediaReply and EffectfulReply") {
    val nonEmptyAlpha = Gen.alphaStr.suchThat(_.nonEmpty)
    forAll(nonEmptyAlpha, nonEmptyAlpha, nonEmptyAlpha, nonEmptyAlpha) {
      (a: String, b: String, botIdValue: String, botNameValue: String) =>
        val textReply  = TextReply.fromList(b, a)(false)
        val mediaReply = MediaReply.fromList(List(Mp3File("track.mp3"), VideoFile("video.mp4")))
        val sBotInfo   = SBotInfo(SBotId(botIdValue), SBotName(botNameValue))
        val effectfulReply = EffectfulReply(EffectfulKey.Random(sBotInfo))

        assertEquals(textReply.prettyPrint, List(a, b).distinct.sorted)
        assertEquals(mediaReply.prettyPrint, List("track.mp3", "video.mp4"))
        assertEquals(effectfulReply.prettyPrint, List(s"Reply for `${EffectfulKey.Random(sBotInfo)}`"))
    }
  }

  property("Reply.replyValues should expose values for text/media and be empty for effectful replies") {
    val nonEmptyAlpha = Gen.alphaStr.suchThat(_.nonEmpty)
    forAll(nonEmptyAlpha, nonEmptyAlpha, nonEmptyAlpha) {
      (textValue: String, botIdValue: String, botNameValue: String) =>
        val textReply      = TextReply.fromList(textValue)(false)
        val mediaReply     = MediaReply.fromList(List(Mp3File("track.mp3")))
        val sBotInfo       = SBotInfo(SBotId(botIdValue), SBotName(botNameValue))
        val effectfulReply = EffectfulReply(EffectfulKey.Random(sBotInfo))

        assertEquals(textReply.replyValues, Set[ReplyValue](Text(textValue)))
        assertEquals(mediaReply.replyValues, Set[ReplyValue](Mp3File("track.mp3")))
        assertEquals(effectfulReply.replyValues, Set.empty)
    }
  }
}
