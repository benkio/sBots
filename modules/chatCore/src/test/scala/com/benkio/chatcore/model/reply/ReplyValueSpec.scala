package com.benkio.chatcore.model.reply

import com.benkio.chatcore.model.media.Media
import com.benkio.chatcore.model.MimeType
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.SBotInfo.{SBotId, SBotName}
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

  test("EffectfulKey.overridePage should update page on supported keys") {
    val sBotInfo = SBotInfo(SBotId("botid"), SBotName("bot"))
    val triggerSearch = EffectfulKey.TriggerSearch(
      sBotInfo = sBotInfo,
      replyBundleMessage = List.empty,
      ignoreMessagePrefix = None,
      page = 1
    )
    assertEquals(triggerSearch.overridePage(Some(3)), triggerSearch.copy(page = 3))

    val topTwenty = EffectfulKey.TopTwenty(sBotInfo, page = 2)
    assertEquals(topTwenty.overridePage(Some(6)), topTwenty.copy(page = 6))
  }

  test("EffectfulKey.overridePage should keep page unchanged on unsupported keys") {
    val sBotInfo = SBotInfo(SBotId("botid"), SBotName("bot"))
    val random = EffectfulKey.Random(sBotInfo)
    assertEquals(random.overridePage(Some(10)), random)
  }

  test("MediaFile.fromString should infer media type from filename") {
    assertEquals(MediaFile.fromString("song.mp3"), Mp3File("song.mp3"))
    assertEquals(MediaFile.fromString("photo.jpg"), PhotoFile("photo.jpg"))
    assertEquals(MediaFile.fromString("image.png"), PhotoFile("image.png"))
    assertEquals(MediaFile.fromString("animation.Gif.mp4"), GifFile("animation.Gif.mp4"))
    assertEquals(MediaFile.fromString("video.mp4"), VideoFile("video.mp4"))
    assertEquals(MediaFile.fromString("note.sticker"), Sticker("note.sticker"))
    assertEquals(MediaFile.fromString("document.txt"), Document("document.txt"))
  }

  test("MediaFile.fromMimeType should map every MIME type") {
    val baseMedia = Media(
      mediaName = "botid_mediaName",
      botId = SBotId("botid"),
      kinds = List("kind"),
      mimeType = MimeType.DOC,
      mediaSources = List(Right(uri"http://something.com")),
      mediaCount = 0,
      createdAt = Instant.now()
    )

    assertEquals(MediaFile.fromMimeType(baseMedia.copy(mediaName = "x.mp3", mimeType = MimeType.MPEG), Mp3File("x.mp3", false))
    assertEquals(MediaFile.fromMimeType(baseMedia.copy(mediaName = "x.jpg", mimeType = MimeType.JPEG), PhotoFile("x.jpg", false))
    assertEquals(MediaFile.fromMimeType(baseMedia.copy(mediaName = "x.png", mimeType = MimeType.PNG), PhotoFile("x.png", false))
    assertEquals(MediaFile.fromMimeType(baseMedia.copy(mediaName = "x.gif.mp4", mimeType = MimeType.GIF), GifFile("x.gif.mp4", false))
    assertEquals(MediaFile.fromMimeType(baseMedia.copy(mediaName = "x.sticker", mimeType = MimeType.STICKER), Sticker("x.sticker", false))
    assertEquals(MediaFile.fromMimeType(baseMedia.copy(mediaName = "x.mp4", mimeType = MimeType.MP4), VideoFile("x.mp4", false))
    assertEquals(MediaFile.fromMimeType(baseMedia.copy(mediaName = "x.doc", mimeType = MimeType.DOC), Document("x.doc", false))

    assertEquals(
      MediaFile.fromMimeType(baseMedia.copy(mediaName = "x.mp3", mimeType = MimeType.MPEG), true),
      Mp3File("x.mp3", true)
    )
  }

  test("StringContext extensions should build the expected reply types") {
    assertEquals(mp3"song.mp3", Mp3File("song.mp3"))
    assertEquals(gif"anim.Gif.mp4", GifFile("anim.Gif.mp4"))
    assertEquals(vid"video.mp4", VideoFile("video.mp4"))
    assertEquals(pho"photo.jpg", PhotoFile("photo.jpg"))
    assertEquals(txt"hello world", Text("hello world"))
    assertEquals(doc"file.bin", Document("file.bin"))
    assertEquals(sticker"emoji.sticker", Sticker("emoji.sticker"))
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
