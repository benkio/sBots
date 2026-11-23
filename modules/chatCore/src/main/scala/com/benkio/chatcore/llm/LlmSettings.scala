package com.benkio.chatcore.llm

import cats.syntax.all.*
import cats.Applicative
import com.benkio.chatcore.model.ChatId

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

  /** Does nothing and reports the LLM as disabled. */
  def noop[F[_]: Applicative]: LlmSettings[F] = new LlmSettings[F] {
    override def enable(chatId: ChatId): F[Unit]   = ().pure[F]
    override def disable(chatId: ChatId): F[Unit]  = ().pure[F]
    override def status(chatId: ChatId): F[Status] = Status(enabled = false).pure[F]
    override def forget(chatId: ChatId): F[Unit]   = ().pure[F]
  }
}
