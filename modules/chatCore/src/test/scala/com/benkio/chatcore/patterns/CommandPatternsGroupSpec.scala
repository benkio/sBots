package com.benkio.chatcore.patterns

import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.SBotInfo
import munit.FunSuite
import org.http4s.syntax.literals.*
import org.http4s.Uri

class CommandPatternsGroupSpec extends FunSuite {

  private val botInfo = SBotInfo(SBotInfo.SBotId("bot"), SBotInfo.SBotName("BotName"))

  test("ShowGroup builds the show-related commands") {
    val commands = CommandPatternsGroup.ShowGroup.group(botInfo)

    assertEquals(
      commands.map(_.trigger.command),
      List(
        CommandKey.SearchShow.asString,
        CommandKey.Subscribe.asString,
        CommandKey.Unsubscribe.asString,
        CommandKey.Subscriptions.asString,
        CommandKey.LoadCalendar.asString,
        CommandKey.ShowCalendar.asString
      )
    )
  }

  test("TriggerGroup builds the trigger and settings commands") {
    val commands = CommandPatternsGroup.TriggerGroup.group(
      triggerFileUri = uri"https://example.com/triggers.md",
      sBotInfo = botInfo,
      messageRepliesData = List.empty[ReplyBundleMessage],
      ignoreMessagePrefix = None
    )

    assertEquals(
      commands.map(_.trigger.command),
      List(
        CommandKey.TriggerList.asString,
        CommandKey.TriggerSearch.asString,
        CommandKey.TopTwenty.asString,
        CommandKey.SetTimeout.asString,
        CommandKey.GetTimeout.asString,
        CommandKey.Llm.asString
      )
    )
  }
}
