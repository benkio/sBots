package com.benkio.chatircadapter.inbound

import com.benkio.chatcore.messagefiltering.messageType
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.MessageType
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatircadapter.model.IrcCapabilities
import munit.FunSuite

class IrcInboundSpec extends FunSuite {

  private val ownNick = "TestBot"

  private val inChannel = IrcMessageEvent("alice", Some("#SBots"), "hello there", 1700000000L)
  private val privately = IrcMessageEvent("Alice", None, "hello there", 1700000000L)

  test("a message to a channel is a message of the channel, a group chat, without id") {
    val result = IrcInbound.message(inChannel, ownNick).get
    assertEquals(result.chatId, ChatId(Platform.Irc, "#sbots"))
    assertEquals(result.chatType, "group")
    assertEquals(result.messageId, None)
    assertEquals(result.text, Some("hello there"))
    assertEquals(result.date, 1700000000L)
  }

  test("a private message is a message of the sender, a private chat") {
    val result = IrcInbound.message(privately, ownNick).get
    assertEquals(result.chatId, ChatId(Platform.Irc, "alice"))
    assertEquals(result.chatType, "private")
  }

  test("the same channel or nick is the same chat whatever the case") {
    assertEquals(
      IrcInbound.message(inChannel.copy(channel = Some("#SBOTS")), ownNick).map(_.chatId),
      IrcInbound.message(inChannel.copy(channel = Some("#sbots")), ownNick).map(_.chatId)
    )
  }

  test("the messages of the bot itself are ignored, whatever the case of its nick") {
    assertEquals(IrcInbound.message(inChannel.copy(sender = "TestBot"), ownNick), None)
    assertEquals(IrcInbound.message(inChannel.copy(sender = "testbot"), ownNick), None)
  }

  test("a message without text is ignored, also when it was only formatting") {
    assertEquals(IrcInbound.message(inChannel.copy(text = "   "), ownNick), None)
    assertEquals(IrcInbound.message(inChannel.copy(text = "\u0002\u000f"), ownNick), None)
  }

  test("the formatting codes are removed from the text") {
    assertEquals(
      IrcInbound.stripFormatting("\u0002bold\u000f and \u000304,12colored\u0003 \u001fplain\u001f"),
      "bold and colored plain"
    )
    assertEquals(IrcInbound.stripFormatting("\u0004FF0000red\u0004 text"), "red text")
    assertEquals(IrcInbound.stripFormatting("1,2,3 stays"), "1,2,3 stays")
  }

  test("the engine takes !command as a command, and the other messages as messages") {
    val botId  = SBotId("bot")
    val syntax = IrcCapabilities.value.commandSyntax
    assertEquals(
      IrcInbound.message(inChannel.copy(text = "!hello now"), ownNick).map(_.messageType(botId, syntax)),
      Some(MessageType.Command)
    )
    assertEquals(IrcInbound.message(inChannel, ownNick).map(_.messageType(botId, syntax)), Some(MessageType.Message))
  }
}
