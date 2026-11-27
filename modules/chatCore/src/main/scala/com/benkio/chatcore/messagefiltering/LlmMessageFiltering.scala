package com.benkio.chatcore.messagefiltering

import cats.syntax.all.*
import cats.MonadThrow
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageType
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.PlatformAdapter
import com.benkio.chatcore.UsageStats.Outcome

import java.util.regex.Pattern

/** Whether a message is handed to the LLM. The conditions that need no DB come first and are [[filter]]; [[guard]] adds
  * the ones that read the chat state, only for the messages that passed the first ones.
  */
object LlmMessageFiltering {

  private val privateChatType = "private"

  /** The pure conditions: the reactive part left the message unanswered, it is a plain message (not a command nor a
    * file request) that is not a forward and has some text, and it is addressed to the bot: it is a private chat, or
    * the text mentions the bot name (whole word, ignoring the case).
    */
  private def filter[F[_]](msg: Message, outcome: Outcome, botInfo: SBotInfo)(using
      platform: PlatformAdapter[F]
  ): Boolean =
    outcome == Outcome.UnReplied &&
      msg.messageType(
        botId = botInfo.botId,
        syntax = platform.capabilities.commandSyntax
      ) == MessageType.Message &&
      !msg.isForward &&
      msg.getContent.exists(_.trim.nonEmpty) &&
      (msg.chatType == privateChatType || mentionsBot(msg, botInfo))

  /** [[filter]] plus the chat state: the chat timeout of the bot has expired and the `/llm` switch of the chat is on.
    * The DB is read only when the pure conditions hold, the timeout before the switch.
    */
  def guard[F[_]: MonadThrow](dbLayer: DBLayer[F], botInfo: SBotInfo)(
      msg: Message,
      outcome: Outcome
  )(using platform: PlatformAdapter[F]): F[Boolean] =
    filter[F](msg = msg, outcome = outcome, botInfo = botInfo)
      .pure[F]
      .ifM(
        FilteringTimeout
          .chatAllowed[F](dbLayer, botInfo.botId)(msg)
          .ifM(dbLayer.llmSettings.status(msg.chatId).map(_.enabled), false.pure[F]),
        false.pure[F]
      )

  private def mentionsBot(msg: Message, botInfo: SBotInfo): Boolean = {
    val name = Pattern.compile(
      s"(?<![\\p{L}\\p{N}])${Pattern.quote(botInfo.botName.value)}(?![\\p{L}\\p{N}])",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    )
    msg.getContent.exists(content => name.matcher(content).find())
  }
}
