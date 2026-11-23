package com.benkio.chatllm.repository.db

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.llm.LlmSettings
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.SBotInfo.SBotId
import doobie.*
import doobie.implicits.*
import doobie.util.transactor.Transactor
import log.effect.LogWriter

import java.time.Instant

/** The doobie-backed implementation of the `/llm` settings port. The port is keyed by `ChatId`, which already carries
  * the platform; the bot id is fixed when the implementation is built, the same way the bot-scoped repositories of
  * chatCore are built with their bot id.
  *
  * A chat with no row is disabled: /llm turns it on explicitly (#1488). `forget` drops the chat's context but leaves
  * the switch untouched.
  */
object LlmSettingsDB {

  def apply[F[_]: Async](transactor: Transactor[F], botId: SBotId)(using log: LogWriter[F]): LlmSettings[F] =
    apply(transactor, botId, Async[F].delay(Instant.now().getEpochSecond))

  /** The clock is injectable so the `updated_at = now` rule of the spec can be tested without depending on sub-second
    * resolution; production uses epoch seconds.
    */
  def apply[F[_]: Async](transactor: Transactor[F], botId: SBotId, now: F[Long])(using
      log: LogWriter[F]
  ): LlmSettings[F] =
    new LlmSettingsDBImpl[F](transactor, botId, now)

  private class LlmSettingsDBImpl[F[_]: Async](transactor: Transactor[F], botId: SBotId, now: F[Long])(using
      log: LogWriter[F]
  ) extends LlmSettings[F] {

    override def enable(chatId: ChatId): F[Unit] = set(chatId, enabled = true)

    override def disable(chatId: ChatId): F[Unit] = set(chatId, enabled = false)

    override def status(chatId: ChatId): F[LlmSettings.Status] =
      statusQuery(chatId, botId).option
        .transact(transactor)
        .map(enabled => LlmSettings.Status(enabled = enabled.getOrElse(false)))

    override def forget(chatId: ChatId): F[Unit] =
      LlmContextStore
        .deleteQuery(chatId, botId)
        .run
        .transact(transactor)
        .flatMap(deleted => log.info(s"LLM forgot $deleted context messages of $chatId for $botId"))
        .void

    private def set(chatId: ChatId, enabled: Boolean): F[Unit] =
      now
        .flatMap(timestamp => upsertQuery(chatId, botId, enabled, timestamp).run.transact(transactor))
        .flatMap(_ => log.debug(s"LLM settings for $chatId / $botId: enabled = $enabled"))
        .void
  }

  def upsertQuery(chatId: ChatId, botId: SBotId, enabled: Boolean, updatedAt: Long): Update0 =
    sql"""INSERT INTO llm_chat_settings (platform, chat_id, bot_id, enabled, updated_at)
          VALUES (${chatId.platform.id}, ${chatId.id}, ${botId.value}, $enabled, $updatedAt)
          ON CONFLICT (platform, chat_id, bot_id) DO UPDATE SET
            enabled = EXCLUDED.enabled, updated_at = EXCLUDED.updated_at""".update

  def statusQuery(chatId: ChatId, botId: SBotId): Query0[Boolean] =
    sql"""SELECT enabled FROM llm_chat_settings
          WHERE platform = ${chatId.platform.id} AND chat_id = ${chatId.id} AND bot_id = ${botId.value}"""
      .query[Boolean]

  def updatedAtQuery(chatId: ChatId, botId: SBotId): Query0[Long] =
    sql"""SELECT updated_at FROM llm_chat_settings
          WHERE platform = ${chatId.platform.id} AND chat_id = ${chatId.id} AND bot_id = ${botId.value}"""
      .query[Long]
}
