package com.benkio.chatcore.patterns

import cats.implicits.*
import cats.MonadThrow
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
import com.benkio.chatcore.model.Timeout
import com.benkio.chatcore.repository.db.*
import log.effect.LogWriter

import scala.concurrent.duration.*

/** Commands that read and write the per-chat timeout. */
object TimeoutCommands {

  object SetTimeoutCommand {

    private[patterns] val setTimeoutCommandDescriptionIta: String =
      s"'/settimeout': Documentazione: ${CommandDocs.chatCoreCommandDocLink("settimeout", "it")}"
    private[patterns] val setTimeoutCommandDescriptionEng: String =
      s"'/settimeout': Documentation: ${CommandDocs.chatCoreCommandDocLink("settimeout", "en")}"

    def setTimeoutLogic[F[_]: MonadThrow: LogWriter](
        msg: Message,
        dbTimeout: DBTimeout[F],
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration],
        commandSyntax: CommandSyntax = CommandSyntax.Slash
    ): F[ReplyValue] = {
      val computation: String => F[ReplyValue] = (input: String) => {
        if input.isEmpty
        then dbTimeout.removeTimeout(chatId = msg.chatId, botId = sBotInfo.botId) *>
          Text(value = "Timeout removed", timeToLive = ttl).pure[F]
        else
          Timeout(msg.chatId, sBotInfo.botId, input)
            .fold(
              error =>
                LogWriter.info(
                  s"[ERROR] While parsing the timeout input: $error"
                ) *>
                  Text(
                    value =
                      s"Timeout set failed: wrong input format for $input, the input must be in the form '/settimeout 00:00:00'",
                    timeToLive = ttl
                  )
                    .pure[F],
              timeout =>
                dbTimeout.setTimeout(
                  DBTimeoutData(timeout)
                ) *>
                  Text(
                    value = s"Timeout set successfully to ${Timeout.formatTimeout(timeout)}",
                    timeToLive = ttl
                  ).pure[F]
            )
      }
      CommandPatterns.handleCommandWithInput[F](
        commandSyntax = commandSyntax,
        msg = msg,
        command = CommandKey.SetTimeout.asString,
        sBotInfo = sBotInfo,
        computation = computation,
        allowEmptyString = true,
        ttl = ttl,
        defaultReply = """Input Required: the input must be in the form '/settimeout 00:00:00' or empty"""
      )
    }

    private[patterns] def setTimeoutReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.SetTimeout.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.SetTimeout(sBotInfo),
          replyToMessage = true
        ),
        instruction = CommandInstructionData.Instructions(
          ita = setTimeoutCommandDescriptionIta,
          eng = setTimeoutCommandDescriptionEng
        )
      )
  }

  object GetTimeoutCommand {

    private[patterns] val getTimeoutCommandDescriptionIta: String =
      s"'/gettimeout': Documentazione: ${CommandDocs.chatCoreCommandDocLink("gettimeout", "it")}"
    private[patterns] val getTimeoutCommandDescriptionEng: String =
      s"'/gettimeout': Documentation: ${CommandDocs.chatCoreCommandDocLink("gettimeout", "en")}"

    def getTimeoutLogic[F[_]: MonadThrow](
        msg: Message,
        dbTimeout: DBTimeout[F],
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration]
    ): F[ReplyValue] = {
      dbTimeout
        .getOrDefault(
          chatId = msg.chatId,
          botId = sBotInfo.botId
        )
        .flatMap((dbTimeoutData: DBTimeoutData) => MonadThrow[F].fromEither(Timeout(dbTimeoutData)))
        .map((timeout: Timeout) =>
          Text(
            value = s"The Timeout is ${Timeout.formatTimeout(timeout)}",
            timeToLive = ttl
          )
        )
        .handleError(e =>
          Text(
            value =
              s"""An error occurred when fetching the timeout. Reset the timeout with `setTimeout` and contact the bot maintainer: ${e
                  .getMessage()}""",
            timeToLive = ttl
          )
        )
    }

    private[patterns] def getTimeoutReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.GetTimeout.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.GetTimeout(sBotInfo),
          replyToMessage = true
        ),
        instruction = CommandInstructionData.Instructions(
          ita = getTimeoutCommandDescriptionIta,
          eng = getTimeoutCommandDescriptionEng
        )
      )
  }
}
