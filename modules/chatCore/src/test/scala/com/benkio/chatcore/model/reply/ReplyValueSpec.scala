package com.benkio.chatcore.model.reply

import com.benkio.chatcore.model.media.Media
import com.benkio.chatcore.model.MimeType
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.SBotInfo.SBotName
import com.benkio.chatcore.Arbitraries.given
import io.circe.parser.decode
import io.circe.syntax.*
import munit.ScalaCheckSuite
import org.http4s.syntax.all.uri
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll

import java.time.Instant

class ReplyValueSpec extends ScalaCheckSuite {

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
        |}""".stripMargin,
      """{
        |  "MarkdownFile" : {
        |    "filepath" : "testFilePath.md",
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

  property("MediaFile.fromMimeType should return the expected MediaFile from Media") {
    val mediaNameGen = Gen.alphaNumStr.suchThat(_.nonEmpty)
    forAll(mediaNameGen) { (mediaNamePrefix: String) =>
      val mediaName = s"${mediaNamePrefix}.mp4"
      val actual = Media(
        mediaName = mediaName,
        botId = SBotId("botid"),
        kinds = List("kind"),
        mimeType = MimeType.MP4,
        mediaSources = List(Right(uri"http://something.com")),
        mediaCount = 0,
        createdAt = Instant.now()
      )
      val expected = VideoFile(mediaName, false)
      assertEquals(MediaFile.fromMimeType(actual), expected)
    }
  }

  property("EffectfulKey.overridePage should update page on supported keys") {
    val pageGen = Gen.choose(0, 1000)
    forAll(pageGen, pageGen) { (initialPage: Int, newPage: Int) =>
      val sBotInfo      = SBotInfo(SBotId("botid"), SBotName("bot"))
      val searchShow    = EffectfulKey.SearchShow(sBotInfo, page = initialPage)
      val triggerSearch = EffectfulKey.TriggerSearch(
        sBotInfo = sBotInfo,
        replyBundleMessage = List.empty,
        ignoreMessagePrefix = None,
        page = initialPage
      )
      val topTwenty = EffectfulKey.TopTwenty(sBotInfo, page = initialPage)

      assertEquals(searchShow.overridePage(Some(newPage)), EffectfulKey.SearchShow(sBotInfo, page = newPage))
      assertEquals(
        triggerSearch.overridePage(Some(newPage)),
        EffectfulKey.TriggerSearch(
          sBotInfo = sBotInfo,
          replyBundleMessage = List.empty,
          ignoreMessagePrefix = None,
          page = newPage
        )
      )
      assertEquals(topTwenty.overridePage(Some(newPage)), EffectfulKey.TopTwenty(sBotInfo, page = newPage))
    }
  }

  property("EffectfulKey.overridePage should keep page unchanged on unsupported keys") {
    forAll(Gen.choose(0, 1000)) { (newPage: Int) =>
      val sBotInfo = SBotInfo(SBotId("botid"), SBotName("bot"))
      val random   = EffectfulKey.Random(sBotInfo)
      assertEquals(random.overridePage(Some(newPage)), random)
    }
  }

  property("EffectfulKey.overridePage should keep page unchanged when no new page is provided") {
    forAll(Gen.choose(0, 1000)) { (page: Int) =>
      val sBotInfo = SBotInfo(SBotId("botid"), SBotName("bot"))

      val searchShow    = EffectfulKey.SearchShow(sBotInfo, page = page)
      val topTwenty     = EffectfulKey.TopTwenty(sBotInfo, page = page)
      val triggerSearch = EffectfulKey.TriggerSearch(
        sBotInfo = sBotInfo,
        replyBundleMessage = List.empty,
        ignoreMessagePrefix = None,
        page = page
      )

      assertEquals(searchShow.overridePage(None), searchShow)
      assertEquals(topTwenty.overridePage(None), topTwenty)
      assertEquals(triggerSearch.overridePage(None), triggerSearch)
    }
  }

  property("EffectfulKey decoder should decode JSON with page field") {
    forAll(Gen.choose(0, 1000)) { (page: Int) =>
      val searchShowJson =
        s"""{
           |  "SearchShow": {
           |    "sBotInfo": {
           |      "botId": "botid",
           |      "botName": "bot"
           |    },
           |    "page": $page
           |  }
           |}""".stripMargin
      val topTwentyJson =
        s"""{
           |  "TopTwenty": {
           |    "sBotInfo": {
           |      "botId": "botid",
           |      "botName": "bot"
           |    },
           |    "page": $page
           |  }
           |}""".stripMargin

      val expectedSBotInfo = SBotInfo(SBotId("botid"), SBotName("bot"))

      assertEquals(
        decode[EffectfulKey](searchShowJson),
        Right(EffectfulKey.SearchShow(expectedSBotInfo, page = page))
      )
      assertEquals(
        decode[EffectfulKey](topTwentyJson),
        Right(EffectfulKey.TopTwenty(expectedSBotInfo, page = page))
      )
    }
  }

  test("EffectfulKey decoder should round-trip all variants with minimal payloads") {
    val sBotInfo                    = SBotInfo(SBotId("botid"), SBotName("bot"))
    val allKeys: List[EffectfulKey] = List(
      EffectfulKey.Random(sBotInfo),
      EffectfulKey.SearchShow(sBotInfo, page = 2),
      EffectfulKey.TriggerSearch(sBotInfo, replyBundleMessage = List.empty, ignoreMessagePrefix = None, page = 4),
      EffectfulKey.Instructions(sBotInfo, ignoreMessagePrefix = Some("!"), commands = List.empty),
      EffectfulKey.Subscribe(sBotInfo),
      EffectfulKey.Unsubscribe(sBotInfo),
      EffectfulKey.Subscriptions(sBotInfo),
      EffectfulKey.TopTwenty(sBotInfo, page = 6),
      EffectfulKey.SetTimeout(sBotInfo),
      EffectfulKey.GetTimeout(sBotInfo),
      EffectfulKey.MediaByKind("metal", sBotInfo),
      EffectfulKey.Callback("bensonify", sBotInfo)
    )

    allKeys.foreach { key =>
      val encoded = key.asJson.noSpaces
      assertEquals(decode[EffectfulKey](encoded), Right(key))
    }
  }

  test("EffectfulKey decoder should round-trip callback and mediaByKind keys with ScalaCheck samples") {
    val sBotInfo    = SBotInfo(SBotId("botid"), SBotName("bot"))
    val nonEmptyKey = Gen.alphaNumStr.suchThat(_.nonEmpty)
    forAll(nonEmptyKey) { (value: String) =>
      val mediaByKind = EffectfulKey.MediaByKind(value, sBotInfo)
      val callback    = EffectfulKey.Callback(value, sBotInfo)

      assertEquals(decode[EffectfulKey](mediaByKind.asJson.noSpaces), Right(mediaByKind))
      assertEquals(decode[EffectfulKey](callback.asJson.noSpaces), Right(callback))
    }
  }

  property("EffectfulKey decoder should fail when JSON does not contain a supported key") {
    forAll(Gen.alphaNumStr.suchThat(_.nonEmpty)) { (unknownKey: String) =>
      val invalidJson =
        s"""{
           |  "$unknownKey": {
           |    "value": "x"
           |  }
           |}""".stripMargin

      val isKnown = Set(
        "Random",
        "SearchShow",
        "TriggerSearch",
        "Instructions",
        "Subscribe",
        "Unsubscribe",
        "Subscriptions",
        "TopTwenty",
        "SetTimeout",
        "GetTimeout",
        "MediaByKind",
        "Callback"
      ).contains(unknownKey)

      if !isKnown then assert(decode[EffectfulKey](invalidJson).isLeft)
    }
  }

  test("MediaFile.fromString should infer media type from filename") {
    assertEquals(MediaFile.fromString("song.mp3"), Mp3File("song.mp3"))
    assertEquals(MediaFile.fromString("photo.jpg"), PhotoFile("photo.jpg"))
    assertEquals(MediaFile.fromString("image.png"), PhotoFile("image.png"))
    assertEquals(MediaFile.fromString("animation.Gif.mp4"), GifFile("animation.Gif.mp4"))
    assertEquals(MediaFile.fromString("video.mp4"), VideoFile("video.mp4"))
    assertEquals(MediaFile.fromString("readme.md"), MarkdownFile("readme.md"))
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

    assertEquals(
      MediaFile.fromMimeType(baseMedia.copy(mediaName = "x.mp3", mimeType = MimeType.MPEG)),
      Mp3File("x.mp3", false)
    )
    assertEquals(
      MediaFile.fromMimeType(baseMedia.copy(mediaName = "x.jpg", mimeType = MimeType.JPEG)),
      PhotoFile("x.jpg", false)
    )
    assertEquals(
      MediaFile.fromMimeType(baseMedia.copy(mediaName = "x.png", mimeType = MimeType.PNG)),
      PhotoFile("x.png", false)
    )
    assertEquals(
      MediaFile.fromMimeType(baseMedia.copy(mediaName = "x.gif.mp4", mimeType = MimeType.GIF)),
      GifFile("x.gif.mp4", false)
    )
    assertEquals(
      MediaFile.fromMimeType(baseMedia.copy(mediaName = "x.sticker", mimeType = MimeType.STICKER)),
      Sticker("x.sticker", false)
    )
    assertEquals(
      MediaFile.fromMimeType(baseMedia.copy(mediaName = "x.mp4", mimeType = MimeType.MP4)),
      VideoFile("x.mp4", false)
    )
    assertEquals(
      MediaFile.fromMimeType(baseMedia.copy(mediaName = "x.md", mimeType = MimeType.MD)),
      MarkdownFile("x.md", false)
    )
    assertEquals(
      MediaFile.fromMimeType(baseMedia.copy(mediaName = "x.doc", mimeType = MimeType.DOC)),
      Document("x.doc", false)
    )

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
    forAll { (replyValueCore: ReplyValueCore) =>
      val replyValue: ReplyValue = replyValueCore
      replyValueCore match {
        case text: Text                 => assertEquals(ReplyValue.from[Text](replyValue), Some(text))
        case markdown: Markdown         => assertEquals(ReplyValue.from[Markdown](replyValue), Some(markdown))
        case mp3File: Mp3File           => assertEquals(ReplyValue.from[Mp3File](replyValue), Some(mp3File))
        case gifFile: GifFile           => assertEquals(ReplyValue.from[GifFile](replyValue), Some(gifFile))
        case photoFile: PhotoFile       => assertEquals(ReplyValue.from[PhotoFile](replyValue), Some(photoFile))
        case videoFile: VideoFile       => assertEquals(ReplyValue.from[VideoFile](replyValue), Some(videoFile))
        case markdownFile: MarkdownFile =>
          assertEquals(ReplyValue.from[MarkdownFile](replyValue), Some(markdownFile))
        case document: Document => assertEquals(ReplyValue.from[Document](replyValue), Some(document))
        case sticker: Sticker   => assertEquals(ReplyValue.from[Sticker](replyValue), Some(sticker))
      }
    }
  }

  property("MediaFile.fromString maps known extensions to the expected ADT") {
    forAll(Gen.alphaNumStr) { (prefix: String) =>
      assert(MediaFile.fromString(s"$prefix.mp3").isInstanceOf[Mp3File])
      assert(MediaFile.fromString(s"$prefix.jpg").isInstanceOf[PhotoFile])
      assert(MediaFile.fromString(s"$prefix.png").isInstanceOf[PhotoFile])
      assert(MediaFile.fromString(s"${prefix}Gif.mp4").isInstanceOf[GifFile])
      assert(MediaFile.fromString(s"$prefix.md").isInstanceOf[MarkdownFile])
      assert(MediaFile.fromString(s"$prefix.sticker").isInstanceOf[Sticker])
      val videoName = if prefix.toLowerCase.endsWith("gif") then s"x$prefix.mp4" else s"$prefix.mp4"
      assert(MediaFile.fromString(videoName).isInstanceOf[VideoFile])
      assert(MediaFile.fromString(s"$prefix.bin").isInstanceOf[Document])
    }
  }

  property("Markdown codec should round-trip string payloads with ScalaCheck samples") {
    forAll(Gen.alphaStr) { (value: String) =>
      val encoded = Markdown(value).asJson.noSpaces
      assertEquals(decode[Markdown](encoded), Right(Markdown(value)))
    }
  }

  property("ReplyValue encoder should fail on non-core adapter values") {
    final case class AdapterReplyValue(label: String) extends ReplyValue

    forAll(Gen.alphaNumStr.suchThat(_.nonEmpty)) { (label: String) =>
      val replyValue: ReplyValue = AdapterReplyValue(label)
      val _ = intercept[UnsupportedOperationException] {
        replyValue.asJson.noSpaces
      }
      assert(true)
    }
  }

  property("ReplyValue show should fallback to toString for non-core adapter values") {
    final case class AdapterReplyValue(label: String) extends ReplyValue

    forAll(Gen.alphaNumStr.suchThat(_.nonEmpty)) { (label: String) =>
      val replyValue: ReplyValue = AdapterReplyValue(label)
      assertEquals(summon[cats.Show[ReplyValue]].show(replyValue), replyValue.toString)
    }
  }
}
