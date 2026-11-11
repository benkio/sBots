package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.reply.*
import com.benkio.chatcore.model.MediaKind
import com.benkio.chatcore.model.Platform
import munit.ScalaCheckSuite
import org.http4s.syntax.literals.*
import org.http4s.Uri
import org.scalacheck.Gen
import org.scalacheck.Prop.*

import scala.concurrent.duration.*

class TelegramCapabilitiesSpec extends ScalaCheckSuite {

  private val mediaFiles: List[MediaFile] = List(
    Mp3File("a.mp3"),
    GifFile("aGif.mp4"),
    PhotoFile("a.jpg"),
    VideoFile("a.mp4"),
    Document("a.pdf"),
    Sticker("a.sticker"),
    MarkdownFile("a.md")
  )

  private val replyValueGen: Gen[ReplyValue] = Gen.oneOf(
    Gen.stringOfN(100, Gen.oneOf(Gen.alphaNumChar, Gen.const(' '), Gen.const('\n'))).map(Text(_, Some(1.minute))),
    Gen.stringOfN(100, Gen.alphaNumChar).map(Markdown(_)),
    Gen.oneOf(mediaFiles)
  )

  test("Telegram should be the Telegram platform that sends every kind of media") {
    assertEquals(TelegramCapabilities.value.platform, Platform.Telegram)
    assertEquals(TelegramCapabilities.value.supportedMedia, MediaKind.values.toSet)
  }

  test("Telegram should keep the limit of the text messages") {
    assertEquals(TelegramCapabilities.value.maxTextLength, TelegramLimits.maxTextMessageLength)
  }

  property("the ReplyTransformer leaves the replies of Telegram as they are") {
    forAll(Gen.listOf(replyValueGen)) { values =>
      assertEquals(ReplyTransformer.transformAll(TelegramCapabilities.value)(values), values)
    }
  }

  test("a text at the Telegram limit is left as it is") {
    val text = Text("a" * TelegramLimits.maxTextMessageLength, Some(1.minute))
    assertEquals(ReplyTransformer.transform(TelegramCapabilities.value)(text), List(text))
  }

  test("Telegram keeps the sources that are file ids") {
    val sources = List(Left("telegramFileId"), Right(uri"https://example.com/a.mp3"))
    assertEquals(ReplyTransformer.usableSources(TelegramCapabilities.value)(sources), sources)
  }
}
