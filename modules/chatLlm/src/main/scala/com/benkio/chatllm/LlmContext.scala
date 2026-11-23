package com.benkio.chatllm

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatllm.llm.LlmContextConfig
import com.benkio.chatllm.repository.db.LlmContextMessage
import com.benkio.chatllm.repository.db.LlmContextRole
import com.benkio.chatllm.repository.db.LlmContextStore
import com.benkio.chatllm.repository.db.LlmSettingsDB
import doobie.util.transactor.Transactor
import log.effect.LogWriter

/** #1489: the per-chat conversation memory. Nothing is stored while the chat's LLM switch is off; what is stored uses
  * stable anonymous labels instead of the platform's sender identity; a prompt only uses a bounded, most-recent window.
  *
  * The engine does not call it yet: the routing is the still open #1492.
  */
trait LlmContext[F[_]] {

  /** Stores a user message for an enabled chat. Returns whether it was stored. */
  def remember(msg: Message, botId: SBotId): F[Boolean]

  /** Stores the assistant's answer of an enabled chat. Returns whether it was stored. */
  def rememberAssistant(chatId: ChatId, botId: SBotId, content: String): F[Boolean]

  /** The retained messages of a chat, oldest first, bounded by count and token budget. */
  def window(chatId: ChatId, botId: SBotId): F[List[LlmContextMessage]]

  /** The prompt built from the window, with no platform sender identity in it. */
  def prompt(chatId: ChatId, botId: SBotId): F[String]

  /** Drops the context older than the configured TTL, for every chat and bot. */
  def deleteExpired(nowEpochSecond: Long): F[Int]
}

object LlmContext {

  /** The production instance: one settings port per bot, one store for the whole table. */
  def apply[F[_]: Async](transactor: Transactor[F], botId: SBotId, config: LlmContextConfig)(using
      log: LogWriter[F]
  ): LlmContext[F] =
    apply(settings = LlmSettingsDB[F](transactor, botId), store = LlmContextStore[F](transactor), config = config)

  /** The pure instance, so a test can supply its own settings and store. */
  def apply[F[_]: Async](settings: LlmSettings[F], store: LlmContextStore[F], config: LlmContextConfig)(using
      log: LogWriter[F]
  ): LlmContext[F] =
    new LlmContextImpl[F](settings, store, config)

  /** The same estimate the input limit uses in `LimitedLlmClient`: four characters per token. */
  private[chatllm] def estimateTokens(content: String): Int =
    math.ceil(content.length.toDouble / 4).toInt

  private final class LlmContextImpl[F[_]: Async](
      settings: LlmSettings[F],
      store: LlmContextStore[F],
      config: LlmContextConfig
  )(using log: LogWriter[F])
      extends LlmContext[F] {

    override def remember(msg: Message, botId: SBotId): F[Boolean] =
      enabled(msg.chatId).ifM(
        store
          .append(msg.chatId, botId, LlmContextRole.User, msg.anonymisedContent(config.authorSalt.value))
          .as(true),
        log.debug(s"[LlmContext] ${msg.chatId} is disabled: message not remembered").as(false)
      )

    override def rememberAssistant(chatId: ChatId, botId: SBotId, content: String): F[Boolean] =
      enabled(chatId).ifM(
        store
          .append(chatId, botId, LlmContextRole.Assistant, s"assistant: ${Message.singleLine(content)}")
          .as(true),
        log.debug(s"[LlmContext] $chatId is disabled: answer not remembered").as(false)
      )

    override def window(chatId: ChatId, botId: SBotId): F[List[LlmContextMessage]] =
      store.recent(chatId, botId, config.maxMessages).map { chronological =>
        val newestFirst = chronological.reverse
        // The prompt joins the window with newlines: estimate each candidate on that rendered prefix, so the
        // separators count towards the budget. Selection stays newest-first.
        val cumulative = newestFirst.indices.map { i =>
          estimateTokens(newestFirst.take(i + 1).map(_.content).mkString("\n"))
        }.toList
        newestFirst
          .zip(cumulative)
          .takeWhile { case (_, tokens) => tokens <= config.maxTokens }
          .map(_._1)
          .reverse
      }

    override def prompt(chatId: ChatId, botId: SBotId): F[String] =
      window(chatId, botId).map(_.map(_.content).mkString("\n"))

    override def deleteExpired(nowEpochSecond: Long): F[Int] =
      store.deleteOlderThan(nowEpochSecond - config.ttl.toSeconds)

    private def enabled(chatId: ChatId): F[Boolean] =
      settings.status(chatId).map(_.enabled)
  }
}
