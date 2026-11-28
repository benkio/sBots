package com.benkio.chatcore.llm

import cats.syntax.all.*
import cats.Applicative
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.SBotInfo.SBotId
import doobie.util.transactor.Transactor

/** The port behind the `/llm` command: the per-chat switches of the LLM answers. */
trait LlmSettings[F[_]] {
  def enable(chatId: ChatId): F[Unit]
  def disable(chatId: ChatId): F[Unit]
  def status(chatId: ChatId): F[LlmSettings.Status]

  /** Drops what the LLM remembers of the chat. */
  def forget(chatId: ChatId): F[Unit]
}

object LlmSettings {

  final case class Status(enabled: Boolean)

  /** How a bot gets its settings from its own DB. chatCore owns the port and the shape of the factory; the doobie
    * implementation is supplied by chatLlm through `main`, so this module never depends on it.
    */
  type Factory[F[_]] = (Transactor[F], SBotId) => LlmSettings[F]

  /** Does nothing and reports the LLM as disabled. */
  def noop[F[_]: Applicative]: LlmSettings[F] = new LlmSettings[F] {
    override def enable(chatId: ChatId): F[Unit]   = ().pure[F]
    override def disable(chatId: ChatId): F[Unit]  = ().pure[F]
    override def status(chatId: ChatId): F[Status] = Status(enabled = false).pure[F]
    override def forget(chatId: ChatId): F[Unit]   = ().pure[F]
  }
}
