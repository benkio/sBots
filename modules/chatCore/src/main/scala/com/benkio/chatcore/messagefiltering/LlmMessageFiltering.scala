package com.benkio.chatcore.messagefiltering

import cats.syntax.all.*
import cats.MonadThrow
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageType
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.PlatformAdapter
import com.benkio.chatcore.UsageStats.Outcome

/** Whether a message is handed to the LLM. The conditions that need no DB come first and are [[filter]]; [[guard]] adds
  * the ones that read the chat state, only for the messages that passed the first ones.
  */
object LlmMessageFiltering {

  private val privateChatType = "private"

  /** The pure conditions: the reactive part left the message unanswered, it is a plain message (not a command nor a
    * file request) that is not a forward and has some text, and it is addressed to the bot: it is a private chat, or
    * the bot is mentioned, replied to, or called by name at the start of the text (see [[Message.isDirectedAt]]).
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
      (msg.chatType == privateChatType || msg.isDirectedAt(botInfo.botName))

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
}
