package com.benkio.chatdiscordadapter.inbound

import com.benkio.chatcore.messagefiltering.messageType
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.MessageType
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatdiscordadapter.model.DiscordCapabilities
import munit.FunSuite

class DiscordInboundSpec extends FunSuite {

  private val syntax = DiscordCapabilities.value.commandSyntax

  private val written = DiscordMessageEvent(
    messageId = "1234567890123456789",
    channelId = "987654321098765432",
    isDirect = false,
    authorIsBot = false,
    content = "hello there",
    createdAtEpochSecond = 1700000000L
  )

  private val slash = DiscordSlashCommandEvent(
    channelId = "987654321098765432",
    isDirect = false,
    authorIsBot = false,
    name = "hello",
    arguments = None,
    createdAtEpochSecond = 1700000000L
  )

  test("a message becomes a Message of the Discord channel, with the snowflake as a string id") {
    val result = DiscordInbound.message(written).get
    assertEquals(result.chatId, ChatId(Platform.Discord, "987654321098765432"))
    assertEquals(result.messageId, Some(MessageId("1234567890123456789")))
    assertEquals(result.text, Some("hello there"))
    assertEquals(result.date, 1700000000L)
  }

  test("a DM is a private chat, a channel of a server is a group") {
    assertEquals(DiscordInbound.message(written.copy(isDirect = true)).map(_.chatType), Some("private"))
    assertEquals(DiscordInbound.message(written).map(_.chatType), Some("group"))
  }

  test("the messages of bots are ignored") {
    assertEquals(DiscordInbound.message(written.copy(authorIsBot = true)), None)
  }

  test("a message without text is ignored") {
    assertEquals(DiscordInbound.message(written.copy(content = "")), None)
    assertEquals(DiscordInbound.message(written.copy(content = "  \n")), None)
  }

  test("a message that is written as a command is left as it is: the engine decides what it is") {
    assertEquals(DiscordInbound.message(written.copy(content = "/hello now")).flatMap(_.text), Some("/hello now"))
  }

  test("a slash command becomes the text of a command, with no message id") {
    val result = DiscordInbound.slashCommand(slash, syntax).get
    assertEquals(result.text, Some("/hello"))
    assertEquals(result.messageId, None)
    assertEquals(result.chatId, ChatId(Platform.Discord, "987654321098765432"))
  }

  test("the arguments of a slash command follow it") {
    assertEquals(
      DiscordInbound.slashCommand(slash.copy(arguments = Some(" some words ")), syntax).flatMap(_.text),
      Some("/hello some words")
    )
    assertEquals(
      DiscordInbound.slashCommand(slash.copy(arguments = Some("  ")), syntax).flatMap(_.text),
      Some("/hello")
    )
  }

  test("a slash command of a bot is ignored") {
    assertEquals(DiscordInbound.slashCommand(slash.copy(authorIsBot = true), syntax), None)
  }

  test("the engine takes the slash command as a command, and the other messages as messages") {
    val botId = SBotId("bot")
    assertEquals(
      DiscordInbound.slashCommand(slash, syntax).map(_.messageType(botId, syntax)),
      Some(MessageType.Command)
    )
    assertEquals(DiscordInbound.message(written).map(_.messageType(botId, syntax)), Some(MessageType.Message))
  }
}
