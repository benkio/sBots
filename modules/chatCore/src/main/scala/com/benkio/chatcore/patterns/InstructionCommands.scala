package com.benkio.chatcore.patterns

import cats.implicits.*
import cats.ApplicativeThrow
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.toEng
import com.benkio.chatcore.model.toIta
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import org.http4s.Uri

import scala.concurrent.duration.*

/** Commands that describe the bot: instructions and trigger list. */
object InstructionCommands {

  object InstructionsCommand {

    def instructionsUriDefault(sBotInfo: SBotInfo): Uri =
      Uri.unsafeFromString(
        s"https://github.com/benkio/sBots/blob/main/modules/bots/${sBotInfo.botName}/${sBotInfo.botId}_instructions.md"
      )

    def instructionsLinkReply(instructionsUri: Uri): Text =
      Text(s"Puoi trovare le istruzioni del bot al seguente URL: $instructionsUri")

    private def formatCommandDescription(raw: String): String =
      raw.replaceFirst("^'?", "").replaceFirst("':\\s*", " - ")

    def describableCommands(
        sBotInfo: SBotInfo,
        triggerListUri: Uri,
        ignoreMessagePrefix: Option[String],
        messageRepliesData: List[ReplyBundleMessage],
        commandRepliesData: List[ReplyBundleCommand]
    ): List[ReplyBundleCommand] =
      commandRepliesData ++ CommandPatternsGroup.TriggerGroup.group(
        triggerFileUri = triggerListUri,
        sBotInfo = sBotInfo,
        messageRepliesData = messageRepliesData,
        ignoreMessagePrefix = ignoreMessagePrefix
      )

    def instructionsFileContent(
        sBotInfo: SBotInfo,
        ignoreMessagePrefix: Option[String],
        commands: List[ReplyBundleCommand]
    ): String = {
      val (commandDescriptionsIta, commandDescriptionsEng) =
        commands.unzip(using cmd => (cmd.instruction.toIta.toList, cmd.instruction.toEng.toList))
      val itaCommands = commandDescriptionsIta.flatten.map(d => s"- ${formatCommandDescription(d)}").mkString("\n")
      val engCommands = commandDescriptionsEng.flatten.map(d => s"- ${formatCommandDescription(d)}").mkString("\n")
      val itaIgnore   = ignoreMessagePrefix
        .map(s =>
          s"\nSe vuoi disabilitare il bot per un messaggio specifico, puoi iniziare il messaggio con il carattere: `$s`\n\n`$s Messaggio`\n"
        )
        .getOrElse("")
      val engIgnore = ignoreMessagePrefix
        .map(s =>
          s"\nIf you want to disable the bot for a specific message, you can start the message with the character: `$s`\n\n`$s Message`\n"
        )
        .getOrElse("")
      s"""# Istruzioni per ${sBotInfo.botName}
         |
         |Per segnalare problemi, scrivere a: https://t.me/Benkio
         |
         |## Comandi
         |
         |$itaCommands
         |$itaIgnore
         |# Instructions for ${sBotInfo.botName}
         |
         |To report issues, write to: https://t.me/Benkio
         |
         |## Commands
         |
         |$engCommands
         |$engIgnore""".stripMargin
    }

    def instructionCommandLogic[F[_]: ApplicativeThrow](
        msg: Message,
        sBotInfo: SBotInfo,
        instructionsUri: Uri,
        ttl: Option[FiniteDuration],
        commandSyntax: CommandSyntax = CommandSyntax.Slash
    ): F[ReplyValue] =
      CommandPatterns.handleCommandWithInput[F](
        commandSyntax = commandSyntax,
        msg = msg,
        command = CommandKey.Instructions.asString,
        sBotInfo = sBotInfo,
        computation = _ => instructionsLinkReply(instructionsUri).pure[F],
        defaultReply = "",
        ttl = ttl,
        allowEmptyString = true
      )

    def instructionsReplyBundleCommand(
        sBotInfo: SBotInfo,
        instructionsUri: Uri
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.Instructions.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.Instructions(sBotInfo, instructionsUri)
        ),
        instruction = CommandInstructionData.NoInstructions
      )
  }

  object TriggerListCommand {

    private[patterns] val triggerListCommandDescriptionIta: String =
      s"'/triggerlist': Documentazione: ${CommandDocs.chatCoreCommandDocLink("triggerlist", "it")}"
    private[patterns] val triggerListCommandDescriptionEng: String =
      s"'/triggerlist': Documentation: ${CommandDocs.chatCoreCommandDocLink("triggerlist", "en")}"

    def triggerListLogic(triggerFileUri: Uri): Text =
      Text(s"Puoi trovare la lista dei trigger al seguente URL: $triggerFileUri")

    private[patterns] def triggerListReplyBundleCommand[F[_]](
        triggerFileUri: Uri
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.TriggerList.trigger,
        reply = TextReply(Set(triggerListLogic(triggerFileUri)), replyToMessage = true),
        instruction = CommandInstructionData.Instructions(
          ita = triggerListCommandDescriptionIta,
          eng = triggerListCommandDescriptionEng
        )
      )
  }
}
