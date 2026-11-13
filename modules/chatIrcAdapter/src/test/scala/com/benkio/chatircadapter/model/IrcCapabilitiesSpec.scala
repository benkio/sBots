package com.benkio.chatircadapter.model

import com.benkio.chatcore.messagefiltering.messageType
import com.benkio.chatcore.model.reply.*
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageType
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import munit.FunSuite
import org.http4s.syntax.literals.*

import scala.concurrent.duration.*

class IrcCapabilitiesSpec extends FunSuite {

  private val capabilities = IrcCapabilities.value
  private val link         = uri"https://example.com/a.mp3"

  test("IRC is the IRC platform: text only, without buttons, delete, edit and file references") {
    assertEquals(capabilities.platform, Platform.Irc)
    assertEquals(capabilities.supportedMedia, Set.empty)
    assert(!capabilities.supportsButtons)
    assert(!capabilities.supportsDelete)
    assert(!capabilities.supportsEdit)
    assert(!capabilities.acceptsFileIds)
  }

  test("a line is at most 400 characters") {
    assertEquals(capabilities.maxTextLength, 400)
  }

  test("the commands are written as !command, without a mention") {
    assertEquals(capabilities.commandSyntax.invocation("hello"), "!hello")
    assertEquals(capabilities.commandSyntax.mentionSeparator, None)
    val message = Message(None, 0L, ChatId(Platform.Irc, "#chan"), "group", text = Some("!hello"))
    assertEquals(message.messageType(SBotId("bot"), capabilities.commandSyntax), MessageType.Command)
  }

  test("a text over the limit is split and loses its time to live") {
    val result = ReplyTransformer.transform(capabilities)(Text("a" * 900, Some(1.minute)))
    assertEquals(result.map { case Text(value, ttl) => (value.length, ttl); case other => fail(s"$other") },
                 List((400, None), (400, None), (100, None)))
  }

  test("a markdown becomes plain text") {
    assertEquals(ReplyTransformer.transform(capabilities)(Markdown("**hi** \\. there")), List(Text("hi . there")))
  }

  test("every media becomes its link, none if it has none") {
    assertEquals(
      ReplyTransformer.transform(capabilities, _ => Some(link))(Mp3File("a.mp3")),
      List(Text(link.renderString))
    )
    assertEquals(ReplyTransformer.transform(capabilities)(Mp3File("a.mp3")), List.empty)
  }
}
