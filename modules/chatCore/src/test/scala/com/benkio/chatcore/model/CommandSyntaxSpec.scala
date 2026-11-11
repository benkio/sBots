package com.benkio.chatcore.model

import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.SBotInfo.SBotName
import munit.FunSuite

class CommandSyntaxSpec extends FunSuite {

  private val botName: SBotName = SBotName("MyBot")
  private val botId: SBotId     = SBotId("mb")
  private val irc               = CommandSyntax(prefix = "!")

  test("Telegram writes a command as /command, optionally addressed to the bot with @BotName") {
    val syntax = CommandSyntax.Telegram
    assertEquals(syntax.invocation("start"), "/start")
    assert(syntax.isInvocationOf("/start", "start", botName))
    assert(syntax.isInvocationOf("/start some input", "start", botName))
    assert(syntax.isInvocationOf("/start@MyBot", "start", botName))
    assert(syntax.isInvocationOf("/start@MyBot some input", "start", botName))
    assert(!syntax.isInvocationOf("/startnow", "start", botName))
    assert(!syntax.isInvocationOf("/other", "start", botName))
    assert(!syntax.isInvocationOf("!start", "start", botName))
  }

  test("a platform without mentions only accepts the command, alone or with an input") {
    assertEquals(irc.invocation("start"), "!start")
    assert(irc.isInvocationOf("!start", "start", botName))
    assert(irc.isInvocationOf("!start some input", "start", botName))
    assert(!irc.isInvocationOf("!start@MyBot", "start", botName))
    assert(!irc.isInvocationOf("/start", "start", botName))
  }

  test("a mention separator other than @ is used between the command and the bot name") {
    val syntax = CommandSyntax(prefix = ".", mentionSeparator = Some(":"))
    assert(syntax.isInvocationOf(".start:MyBot", "start", botName))
    assert(!syntax.isInvocationOf(".start@MyBot", "start", botName))
    assert(syntax.isCommandWord(".start:MyBot", "start", botName))
  }

  test("isCommandWord matches the typed command, addressed to the bot or not") {
    assert(CommandSyntax.Telegram.isCommandWord("/start", "start", botName))
    assert(CommandSyntax.Telegram.isCommandWord("/start@MyBot", "start", botName))
    assert(!CommandSyntax.Telegram.isCommandWord("/start@OtherBot", "start", botName))
    assert(!CommandSyntax.Telegram.isCommandWord("/start input", "start", botName))
    assert(irc.isCommandWord("!start", "start", botName))
    assert(!irc.isCommandWord("/start", "start", botName))
  }

  test("commandName gives the name of the command whatever the syntax") {
    assertEquals(CommandSyntax.Telegram.commandName("  /start@MyBot some input "), "start")
    assertEquals(CommandSyntax.Telegram.commandName("/start"), "start")
    assertEquals(irc.commandName("!start some input"), "start")
    assertEquals(irc.commandName("!start@MyBot"), "start@MyBot")
  }

  test("isCommand checks the prefix") {
    assert(CommandSyntax.Telegram.isCommand("/anything"))
    assert(!CommandSyntax.Telegram.isCommand("!anything"))
    assert(irc.isCommand("!anything"))
    assert(!irc.isCommand("/anything"))
  }

  test("a file request starts with the bot id, after the marker when the platform has one") {
    assert(CommandSyntax.Telegram.isFileRequest("mb_file.mp3", botId))
    assert(!CommandSyntax.Telegram.isFileRequest("hello mb_file.mp3", botId))
    assertEquals(CommandSyntax.Telegram.fileRequestContent("mb_file.mp3"), "mb_file.mp3")

    val withMarker = CommandSyntax(prefix = "!", fileRequestMarker = "+")
    assert(withMarker.isFileRequest("+mb_file.mp3", botId))
    assert(!withMarker.isFileRequest("mb_file.mp3", botId))
    assertEquals(withMarker.fileRequestContent("+mb_file.mp3"), "mb_file.mp3")
  }

  test("the prefix can not be empty and the file request marker can not be the prefix") {
    val _ = intercept[IllegalArgumentException](CommandSyntax(prefix = ""))
    intercept[IllegalArgumentException](CommandSyntax(prefix = "!", fileRequestMarker = "!"))
  }
}
