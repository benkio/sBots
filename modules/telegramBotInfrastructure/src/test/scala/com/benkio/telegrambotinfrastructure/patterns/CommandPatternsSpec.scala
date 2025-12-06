package com.benkio.telegrambotinfrastructure.patterns

import com.benkio.telegrambotinfrastructure.model.SBotInfo
import cats.effect.*
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotName
import munit.*
import telegramium.bots.Chat
import telegramium.bots.Message
import com.benkio.telegrambotinfrastructure.mocks.SampleWebhookBot

class CommandPatternsSpec extends CatsEffectSuite {

  val msg: Message = Message(
    messageId = 0,
    date = 0,
    chat = Chat(id = 123, `type` = "private")
  )
  val command: String      = "command"
  val defaultReply: String = "defaultReply"
  val sBotInfo: SBotInfo = SampleWebhookBot.sBotInfo

  def resultByInput(input: String, allowEmptyString: Boolean): IO[List[Text]] = CommandPatterns.handleCommandWithInput(
    msg = msg.copy(text = Some(input)),
    command = command,
    sBotInfo = sBotInfo,
    defaultReply = defaultReply,
    allowEmptyString = allowEmptyString,
    computation = input => (if input.isEmpty then List("success") else List("failed")).pure[IO]
  )

  test(
    "handleCommandWithInput should serve an empty string to computation when the `allowEmptyString` is true and the command matches"
  ) {
    val result =
      List(
        """/command""",
        """/command@botName""",
        """/command   """,
        """/command@botName   """
      ).traverse(resultByInput(_, true))
    assertIO(result, List.fill(4)(List(Text("success"))))
  }

  test(
    "handleCommandWithInput should return the default reply when the `allowEmptyString` is false and the command matches, but No Input"
  ) {
    val result: IO[List[List[Text]]] =
      List(
        """/command""",
        """/command@botName""",
        """/command   """,
        """/command@botName   """
      ).traverse(resultByInput(_, false))
    assertIO(result, List.fill(4)(List(Text(defaultReply))))
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
        )
    )
    val expectedError =
      """|An error occurred processing the command: command
         | message text: /command
         | bot: botName
         | error: [CommandPatternsSpec] this should trigger the error handling of the handleCommandWithInput""".stripMargin
    assertIO(result, List(Text(expectedError)))
  }
}
