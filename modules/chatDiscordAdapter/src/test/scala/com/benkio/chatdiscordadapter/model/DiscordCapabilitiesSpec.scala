package com.benkio.chatdiscordadapter.model

import com.benkio.chatcore.model.reply.*
import com.benkio.chatcore.model.MediaKind
import com.benkio.chatcore.model.Platform
import munit.FunSuite
import org.http4s.syntax.literals.*

import scala.concurrent.duration.*

class DiscordCapabilitiesSpec extends FunSuite {

  private val capabilities = DiscordCapabilities.value
  private val link         = uri"https://example.com/a.mp3"

  test("Discord is the Discord platform, without buttons and file references") {
    assertEquals(capabilities.platform, Platform.Discord)
    assert(!capabilities.supportsButtons)
    assert(!capabilities.acceptsFileIds)
  }

  test("the messages are limited to 2000 characters") {
    assertEquals(capabilities.maxTextLength, 2000)
  }

  test("the uploads are files, the stickers and the markdown files are not") {
    assert(capabilities.supports(Mp3File("a.mp3")))
    assert(capabilities.supports(GifFile("a.mp4")))
    assert(capabilities.supports(PhotoFile("a.jpg")))
    assert(capabilities.supports(VideoFile("a.mp4")))
    assert(capabilities.supports(Document("a.pdf")))
    assert(!capabilities.supports(Sticker("a.sticker")))
    assert(!capabilities.supports(MarkdownFile("a.md")))
    assertEquals(capabilities.supportedMedia, MediaKind.values.toSet - MediaKind.Sticker - MediaKind.MarkdownFile)
  }

  test("the commands are written as !command, without a mention") {
    assertEquals(capabilities.commandSyntax.invocation("hello"), "!hello")
    assertEquals(capabilities.commandSyntax.mentionSeparator, None)
  }

  test("a text over the limit is split and keeps its time to live, markdown stays markdown") {
    val result = ReplyTransformer.transform(capabilities)(Text("a" * 2500, Some(1.minute)))
    assertEquals(result.map { case Text(value, ttl) => (value.length, ttl); case other => fail(s"$other") }, List((2000, Some(1.minute)), (500, Some(1.minute))))
    assertEquals(ReplyTransformer.transform(capabilities)(Markdown("**hi**")), List(Markdown("**hi**")))
  }

  test("a media that Discord cannot upload becomes its link") {
    assertEquals(
      ReplyTransformer.transform(capabilities, _ => Some(link))(Sticker("a.sticker")),
      List(Text(link.renderString))
    )
  }
}
