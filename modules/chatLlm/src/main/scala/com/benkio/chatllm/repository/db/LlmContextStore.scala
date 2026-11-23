package com.benkio.chatllm.repository.db

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.SBotInfo.SBotId
import doobie.*
import doobie.implicits.*
import doobie.util.transactor.Transactor

import java.time.Instant

/** Who wrote a context message. Only the role is persisted, never the sender's name or id (#1489). */
enum LlmContextRole(val asString: String) {
  case User      extends LlmContextRole("user")
  case Assistant extends LlmContextRole("assistant")
}

object LlmContextRole {

  def fromString(value: String): Option[LlmContextRole] = values.find(_.asString == value)
}

/** One message retained for a chat, in the form the prompt builder will consume it. */
final case class LlmContextMessage(role: LlmContextRole, content: String, createdAt: Long)

/** The doobie repository of the `llm_context` table. The rolling window, TTL cleanup and prompt ordering are added with
  * #1489; this store only persists and drops the messages of a chat.
  */
trait LlmContextStore[F[_]] {

  /** Appends a message with the current epoch second as its timestamp. */
  def append(chatId: ChatId, botId: SBotId, role: LlmContextRole, content: String): F[Unit]

  /** All the retained messages of a chat, oldest first. */
  def all(chatId: ChatId, botId: SBotId): F[List[LlmContextMessage]]

  /** Drops every message of a chat, giving back how many were removed. */
  def delete(chatId: ChatId, botId: SBotId): F[Int]
}

object LlmContextStore {

  def apply[F[_]: Async](transactor: Transactor[F]): LlmContextStore[F] = new LlmContextStoreImpl[F](transactor)

  private class LlmContextStoreImpl[F[_]: Async](transactor: Transactor[F]) extends LlmContextStore[F] {

    override def append(chatId: ChatId, botId: SBotId, role: LlmContextRole, content: String): F[Unit] =
      Async[F]
        .delay(Instant.now().getEpochSecond)
        .flatMap(now => appendQuery(chatId, botId, role, content, now).run.transact(transactor).void)

    override def all(chatId: ChatId, botId: SBotId): F[List[LlmContextMessage]] =
      allQuery(chatId, botId)
        .to[List]
        .transact(transactor)
        .flatMap(_.traverse { case (role, content, createdAt) =>
          Async[F].fromOption(
            LlmContextRole.fromString(role).map(LlmContextMessage(_, content, createdAt)),
            new IllegalArgumentException(s"unknown llm_context role: $role")
          )
        })

    override def delete(chatId: ChatId, botId: SBotId): F[Int] =
      deleteQuery(chatId, botId).run.transact(transactor)
  }

  def appendQuery(
      chatId: ChatId,
      botId: SBotId,
      role: LlmContextRole,
      content: String,
      createdAt: Long
  ): Update0 =
    sql"""INSERT INTO llm_context (platform, chat_id, bot_id, role, content, created_at)
          VALUES (${chatId.platform.id}, ${chatId.id}, ${botId.value}, ${role.asString}, $content, $createdAt)""".update

  def allQuery(chatId: ChatId, botId: SBotId): Query0[(String, String, Long)] =
    sql"""SELECT role, content, created_at FROM llm_context
          WHERE platform = ${chatId.platform.id} AND chat_id = ${chatId.id} AND bot_id = ${botId.value}
          ORDER BY created_at, rowid""".query[(String, String, Long)]

  def deleteQuery(chatId: ChatId, botId: SBotId): Update0 =
    sql"""DELETE FROM llm_context
          WHERE platform = ${chatId.platform.id} AND chat_id = ${chatId.id} AND bot_id = ${botId.value}""".update
}
