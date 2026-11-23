package com.benkio.chatcore.patterns

import cats.implicits.*
import cats.ApplicativeThrow
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo

import scala.concurrent.duration.*

/** Entry point for the command patterns, grouped by bounded context in the sibling files.
  *
  * The aliases below keep the historical `CommandPatterns.<Command>` call sites working while the implementations live
  * in the dedicated modules: [[SearchCommands]], [[MediaCommands]], [[SubscriptionCommands]], [[TimeoutCommands]] and
  * [[InstructionCommands]] and [[LlmCommands]].
  */
object CommandPatterns {

  val CalendarCommands: SubscriptionCommands.CalendarCommands.type = SubscriptionCommands.CalendarCommands
  val SubscribeUnsubscribeCommand: SubscriptionCommands.SubscribeUnsubscribeCommand.type =
    SubscriptionCommands.SubscribeUnsubscribeCommand

  val MediaByKindCommand: MediaCommands.MediaByKindCommand.type = MediaCommands.MediaByKindCommand
  val RandomDataCommand: MediaCommands.RandomDataCommand.type   = MediaCommands.RandomDataCommand
  val StatisticsCommands: MediaCommands.StatisticsCommands.type = MediaCommands.StatisticsCommands

  val SearchShowCommand: SearchCommands.SearchShowCommand.type       = SearchCommands.SearchShowCommand
  val TriggerSearchCommand: SearchCommands.TriggerSearchCommand.type = SearchCommands.TriggerSearchCommand

  val SetTimeoutCommand: TimeoutCommands.SetTimeoutCommand.type = TimeoutCommands.SetTimeoutCommand
  val GetTimeoutCommand: TimeoutCommands.GetTimeoutCommand.type = TimeoutCommands.GetTimeoutCommand

  val InstructionsCommand: InstructionCommands.InstructionsCommand.type = InstructionCommands.InstructionsCommand
  val TriggerListCommand: InstructionCommands.TriggerListCommand.type   = InstructionCommands.TriggerListCommand

  val LlmCommand: LlmCommands.LlmCommand.type = LlmCommands.LlmCommand

  def handleCommandWithInput[F[_]: ApplicativeThrow](
      msg: Message,
      command: String,
      sBotInfo: SBotInfo,
      computation: String => F[ReplyValue],
      defaultReply: String,
      allowEmptyString: Boolean = false,
      ttl: Option[FiniteDuration],
      commandSyntax: CommandSyntax = CommandSyntax.Slash
  ): F[ReplyValue] =
    msg.text
      .filter(t => {
        val (inputCommand, rest) = t.trim.span(_ != ' ')
        val restCheck            = allowEmptyString || (rest.trim.nonEmpty && !allowEmptyString)
        val commandCheck         = commandSyntax.isCommandWord(inputCommand, command, sBotInfo.botName)
        commandCheck && restCheck
      })
      .map(t => computation(t.dropWhile(_ != ' ').drop(1).trim))
      .getOrElse(Text(value = defaultReply, timeToLive = ttl).pure[F])
      .handleErrorWith(e =>
        Text(
          value = s"""An error occurred processing the command: $command
                     | message text: ${msg.text.orElse(msg.caption).getOrElse("")}
                     | bot: ${sBotInfo.botName}
                     | error: ${e.getMessage}""".stripMargin,
          timeToLive = ttl
        ).pure[F]
      )
}
