package com.benkio.chatcore.patterns

import cats.syntax.all.*
import cats.MonadThrow
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo

import java.util.Locale
import scala.concurrent.duration.FiniteDuration

/** The `/llm` command: a per-chat switch of the LLM replies, delegating to the [[LlmSettings]] port. */
object LlmCommands {

  object LlmCommand {

    private[patterns] val llmCommandDescriptionIta: String =
      s"'/llm': Abilita/disabilita le risposte LLM della chat: ${CommandDocs.chatCoreCommandDocLink("llm", "it")}"
    private[patterns] val llmCommandDescriptionEng: String =
      s"'/llm': Enable/disable the LLM replies of this chat: ${CommandDocs.chatCoreCommandDocLink("llm", "en")}"

    def llmCommandLogic[F[_]: MonadThrow](
        msg: Message,
        llmSettings: LlmSettings[F],
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration],
        commandSyntax: CommandSyntax = CommandSyntax.Slash
    ): F[ReplyValue] = {
      val usage = s"Usage: ${commandSyntax.invocation(CommandKey.Llm.asString)} on|off|status"

      def reply(value: String): F[ReplyValue] = Text(value = value, timeToLive = ttl).pure[F]

      val computation: String => F[ReplyValue] = (input: String) =>
        input.trim.toLowerCase(Locale.ROOT) match {
          case "on" =>
            llmSettings.enable(msg.chatId) *> reply("LLM replies enabled for this chat")
          case "off" =>
            llmSettings.disable(msg.chatId) *> reply("LLM replies disabled for this chat")
          case "status" =>
            llmSettings
              .status(msg.chatId)
              .map(status =>
                if status.enabled then "LLM replies are enabled for this chat"
                else "LLM replies are disabled for this chat"
              )
              .flatMap(reply)
          case _ => reply(usage)
        }

      CommandPatterns.handleCommandWithInput[F](
        commandSyntax = commandSyntax,
        msg = msg,
        command = CommandKey.Llm.asString,
        sBotInfo = sBotInfo,
        computation = computation,
        allowEmptyString = false,
        ttl = ttl,
        defaultReply = usage
      )
    }

    private[patterns] def llmReplyBundleCommand(sBotInfo: SBotInfo): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.Llm.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.Llm(sBotInfo),
          replyToMessage = true
        ),
        instruction = CommandInstructionData.Instructions(
          ita = llmCommandDescriptionIta,
          eng = llmCommandDescriptionEng
        )
      )
  }
}
