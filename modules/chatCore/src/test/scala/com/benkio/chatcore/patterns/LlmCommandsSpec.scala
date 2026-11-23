package com.benkio.chatcore.patterns

import cats.effect.IO
import cats.effect.Ref
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.TestIds
import munit.CatsEffectSuite

class LlmCommandsSpec extends CatsEffectSuite {

  private val sBotInfo = SBotInfo(
    botName = SBotInfo.SBotName("SampleBot"),
    botId = SBotInfo.SBotId("sbot")
  )
  private val chatId = TestIds.chatId(123L)

  private def msg(text: String): Message =
    Message(messageId = None, date = 0L, chatId = chatId, chatType = "private", text = Some(text))

  /** Records the calls of the port so the command is verified to delegate to the right operation. */
  private def settings(calls: Ref[IO, List[String]], enabled: Boolean = false): LlmSettings[IO] =
    new LlmSettings[IO] {
      private def record(operation: String, chat: ChatId): IO[Unit] =
        calls.update(_ :+ s"$operation:${chat.id}")
      override def enable(chat: ChatId): IO[Unit]               = record("enable", chat)
      override def disable(chat: ChatId): IO[Unit]              = record("disable", chat)
      override def forget(chat: ChatId): IO[Unit]               = record("forget", chat)
      override def status(chat: ChatId): IO[LlmSettings.Status] =
        record("status", chat).as(LlmSettings.Status(enabled = enabled))
    }

  private def run(text: String, port: LlmSettings[IO], syntax: CommandSyntax = CommandSyntax.Slash) =
    CommandPatterns.LlmCommand.llmCommandLogic[IO](
      msg = msg(text),
      llmSettings = port,
      sBotInfo = sBotInfo,
      ttl = None,
      commandSyntax = syntax
    )

  test("/llm on enables the chat and confirms") {
    for {
      calls    <- Ref[IO].of(List.empty[String])
      result   <- run("/llm on", settings(calls))
      recorded <- calls.get
    } yield {
      assertEquals(result, Text("LLM replies enabled for this chat"))
      assertEquals(recorded, List(s"enable:${chatId.id}"))
    }
  }

  test("/llm off disables the chat and confirms") {
    for {
      calls    <- Ref[IO].of(List.empty[String])
      result   <- run("/llm off", settings(calls))
      recorded <- calls.get
    } yield {
      assertEquals(result, Text("LLM replies disabled for this chat"))
      assertEquals(recorded, List(s"disable:${chatId.id}"))
    }
  }

  test("/llm status reports the stored state without changing it") {
    for {
      calls    <- Ref[IO].of(List.empty[String])
      result   <- run("/llm status", settings(calls, enabled = true))
      recorded <- calls.get
    } yield {
      assertEquals(result, Text("LLM replies are enabled for this chat"))
      assertEquals(recorded, List(s"status:${chatId.id}"))
    }
  }

  test("/llm status reports disabled when no setting is stored") {
    for {
      calls    <- Ref[IO].of(List.empty[String])
      result   <- run("/llm status", settings(calls))
      recorded <- calls.get
    } yield {
      assertEquals(result, Text("LLM replies are disabled for this chat"))
      assertEquals(recorded, List(s"status:${chatId.id}"))
    }
  }

  test("/llm forget purges the context without changing the switch") {
    for {
      calls    <- Ref[IO].of(List.empty[String])
      result   <- run("/llm forget", settings(calls))
      recorded <- calls.get
    } yield {
      assertEquals(result, Text("LLM context forgotten for this chat"))
      assertEquals(recorded, List(s"forget:${chatId.id}"))
    }
  }

  test("missing or unknown arguments show the usage and change nothing") {
    for {
      calls    <- Ref[IO].of(List.empty[String])
      noArg    <- run("/llm", settings(calls))
      wrongArg <- run("/llm maybe", settings(calls))
      ircNoArg <- run("!llm", settings(calls), CommandSyntax(prefix = "!"))
      recorded <- calls.get
    } yield {
      assertEquals(noArg, Text("Usage: /llm on|off|status|forget"))
      assertEquals(wrongArg, noArg)
      assertEquals(ircNoArg, Text("Usage: !llm on|off|status|forget"))
      assertEquals(recorded, Nil)
    }
  }

  test("the command works with the IRC syntax too") {
    for {
      calls    <- Ref[IO].of(List.empty[String])
      result   <- run("!llm ON", settings(calls), CommandSyntax(prefix = "!"))
      recorded <- calls.get
    } yield {
      assertEquals(result, Text("LLM replies enabled for this chat"))
      assertEquals(recorded, List(s"enable:${chatId.id}"))
    }
  }
}
