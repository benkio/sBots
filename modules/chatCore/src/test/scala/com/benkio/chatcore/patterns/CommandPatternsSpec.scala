package com.benkio.chatcore.patterns

import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import munit.*

class CommandPatternsSpec extends CatsEffectSuite {

  val msg: Message         = Message(messageId = 0, date = 0L, chatId = ChatId(123L), chatType = "private")
  val command: String      = "command"
  val defaultReply: String = "defaultReply"
  val sBotInfo: SBotInfo   = SBotInfo(
    botName = SBotInfo.SBotName("SampleWebhookBot"),
    botId = SBotInfo.SBotId("sbot")
  )

  def resultByInput(input: String, allowEmptyString: Boolean): IO[ReplyValue] = CommandPatterns.handleCommandWithInput(
    msg = msg.copy(text = Some(input)),
    command = command,
    sBotInfo = sBotInfo,
    defaultReply = defaultReply,
    allowEmptyString = allowEmptyString,
    computation = input => Text(if input.isEmpty then "success" else "failed").pure[IO],
    ttl = None
  )

  test(
    "handleCommandWithInput should serve an empty string to computation when the `allowEmptyString` is true and the command matches"
  ) {
    val result =
      List(
        """/command""",
        """/command@SampleWebhookBot""",
        """/command   """,
        """/command@SampleWebhookBot   """
      ).traverse(resultByInput(_, true))
    assertIO(result, List.fill(4)(Text("success")))
  }

  test(
    "handleCommandWithInput should return the default reply when the `allowEmptyString` is false and the command matches, but No Input"
  ) {
    val result: IO[List[ReplyValue]] =
      List(
        """/command""",
        """/command@SampleWebhookBot""",
        """/command   """,
        """/command@SampleWebhookBot   """
      ).traverse(resultByInput(_, false))
    assertIO(result, List.fill(4)(Text(value = defaultReply)))
  }

  test("handleCommandWithInput should return the error if the command's input causes an error ") {
    val result = CommandPatterns.handleCommandWithInput(
      msg = msg.copy(text = Some("""/command""")),
      command = command,
      sBotInfo = sBotInfo,
      defaultReply = defaultReply,
      allowEmptyString = true,
      computation = _ =>
        IO.raiseError(
          Throwable("[CommandPatternsSpec] this should trigger the error handling of the handleCommandWithInput")
        ),
      ttl = None
    )
    val expectedError =
      """|An error occurred processing the command: command
         | message text: /command
         | bot: SampleWebhookBot
         | error: [CommandPatternsSpec] this should trigger the error handling of the handleCommandWithInput""".stripMargin
    assertIO(result, Text(expectedError))
  }
}
