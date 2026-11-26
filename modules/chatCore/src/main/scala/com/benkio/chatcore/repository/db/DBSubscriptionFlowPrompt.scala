package com.benkio.chatcore.repository.db

import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.model.*
import com.benkio.chatcore.SubscriptionFlowPromptHandle
import doobie.*
import doobie.implicits.*
import log.effect.LogWriter

import scala.concurrent.duration.*

final case class DBSubscriptionFlowPromptData(
    platform: String,
    chat_id: String,
    bot_id: String,
    requester: String,
    message_id: String,
    owner: String,
    saved_at_millis: Long
)

object DBSubscriptionFlowPromptData {
  def toHandle(data: DBSubscriptionFlowPromptData): Option[SubscriptionFlowPromptHandle] =
    Platform
      .fromId(data.platform)
      .toOption
      .map(platform =>
        SubscriptionFlowPromptHandle(
          key = SubscriptionFlowKey(ChatId(platform, data.chat_id), SBotInfo.SBotId(data.bot_id), data.requester),
          messageId = data.message_id,
          owner = data.owner,
          savedAt = data.saved_at_millis.millis
        )
      )
}

trait DBSubscriptionFlowPrompt[F[_]] {
  def put(key: SubscriptionFlowKey, messageId: String, owner: String): F[Unit]
  def touch(key: SubscriptionFlowKey): F[Unit]
  def byKey(key: SubscriptionFlowKey): F[Option[SubscriptionFlowPromptHandle]]
  def byMessageId(chatId: ChatId, messageId: String): F[Option[SubscriptionFlowPromptHandle]]
  def remove(key: SubscriptionFlowKey): F[Unit]
}

object DBSubscriptionFlowPrompt {

  def apply[F[_]: Async](
      transactor: Transactor[F],
      botId: SBotInfo.SBotId,
      expiry: FiniteDuration,
      maxPrompts: Int
  )(using log: LogWriter[F]): DBSubscriptionFlowPrompt[F] =
    new DBSubscriptionFlowPromptImpl[F](transactor, botId, expiry, maxPrompts, log)

  private[db] class DBSubscriptionFlowPromptImpl[F[_]: Async](
      transactor: Transactor[F],
      botId: SBotInfo.SBotId,
      expiry: FiniteDuration,
      maxPrompts: Int,
      log: LogWriter[F]
  ) extends DBSubscriptionFlowPrompt[F] {

    private def nowMillis: F[Long]      = Clock[F].realTime.map(_.toMillis)
    private def cutoff(now: Long): Long = now - expiry.toMillis

    private def pruneExpired(now: Long): ConnectionIO[Int] =
      sql"DELETE FROM subscription_flow_prompt WHERE bot_id = ${botId.value} AND saved_at_millis <= ${cutoff(now)}".update.run

    private def countLive(now: Long): ConnectionIO[Int] =
      sql"SELECT COUNT(*) FROM subscription_flow_prompt WHERE bot_id = ${botId.value} AND saved_at_millis > ${cutoff(now)}"
        .query[Int]
        .unique

    private def evictOldest(now: Long): ConnectionIO[Unit] =
      countLive(now).flatMap { count =>
        if count <= maxPrompts then ().pure[ConnectionIO]
        else
          sql"""DELETE FROM subscription_flow_prompt
                WHERE bot_id = ${botId.value} AND (platform, chat_id, bot_id, requester) IN (
                  SELECT platform, chat_id, bot_id, requester FROM subscription_flow_prompt
                  WHERE bot_id = ${botId.value} AND saved_at_millis > ${cutoff(now)}
                  ORDER BY saved_at_millis ASC
                  LIMIT ${count - maxPrompts}
                )""".update.run.void
      }

    override def put(key: SubscriptionFlowKey, messageId: String, owner: String): F[Unit] =
      for {
        now <- nowMillis
        _   <- (for {
          _ <- pruneExpired(now)
          _ <- sql"""DELETE FROM subscription_flow_prompt
                            WHERE platform = ${key.chatId.platform.id}
                              AND chat_id = ${key.chatId.id}
                              AND message_id = $messageId
                              AND NOT (bot_id = ${key.botId.value} AND requester = ${key.requester})""".update.run
          _ <-
            sql"""INSERT INTO subscription_flow_prompt (platform, chat_id, bot_id, requester, message_id, owner, saved_at_millis)
                            VALUES (${key.chatId.platform.id}, ${key.chatId.id}, ${key.botId.value}, ${key.requester}, $messageId, $owner, $now)
                            ON CONFLICT(platform, chat_id, bot_id, requester) DO UPDATE SET
                              message_id = EXCLUDED.message_id,
                              owner = EXCLUDED.owner,
                              saved_at_millis = EXCLUDED.saved_at_millis""".update.run
          _ <- evictOldest(now)
        } yield ()).transact(transactor)
        _ <- log.debug(s"[DBSubscriptionFlowPrompt] put key=$key messageId=$messageId")
      } yield ()

    override def touch(key: SubscriptionFlowKey): F[Unit] =
      for {
        now <- nowMillis
        _   <- (pruneExpired(now) *> sql"""UPDATE subscription_flow_prompt SET saved_at_millis = $now
                WHERE platform = ${key.chatId.platform.id}
                  AND chat_id = ${key.chatId.id}
                  AND bot_id = ${key.botId.value}
                  AND requester = ${key.requester}
                  AND saved_at_millis > ${cutoff(now)}""".update.run).transact(transactor).void
      } yield ()

    override def byKey(key: SubscriptionFlowKey): F[Option[SubscriptionFlowPromptHandle]] =
      for {
        now    <- nowMillis
        result <- (pruneExpired(
          now
        ) *> sql"""SELECT platform, chat_id, bot_id, requester, message_id, owner, saved_at_millis
                      FROM subscription_flow_prompt
                      WHERE platform = ${key.chatId.platform.id}
                        AND chat_id = ${key.chatId.id}
                        AND bot_id = ${key.botId.value}
                        AND requester = ${key.requester}
                        AND saved_at_millis > ${cutoff(now)}"""
          .query[DBSubscriptionFlowPromptData]
          .option).transact(transactor)
        handle <- result
          .traverse { d =>
            DBSubscriptionFlowPromptData
              .toHandle(d)
              .fold(
                log
                  .warn(s"[DBSubscriptionFlowPrompt] failed to decode prompt for key=$key")
                  .as(none[SubscriptionFlowPromptHandle])
              )(_.some.pure[F])
          }
          .map(_.flatten)
      } yield handle

    override def byMessageId(chatId: ChatId, messageId: String): F[Option[SubscriptionFlowPromptHandle]] =
      for {
        now    <- nowMillis
        result <- (pruneExpired(
          now
        ) *> sql"""SELECT platform, chat_id, bot_id, requester, message_id, owner, saved_at_millis
                      FROM subscription_flow_prompt
                      WHERE platform = ${chatId.platform.id}
                        AND chat_id = ${chatId.id}
                        AND bot_id = ${botId.value}
                        AND message_id = $messageId
                        AND saved_at_millis > ${cutoff(now)}
                      ORDER BY saved_at_millis DESC
                      LIMIT 1"""
          .query[DBSubscriptionFlowPromptData]
          .option).transact(transactor)
        handle <- result
          .traverse { d =>
            DBSubscriptionFlowPromptData
              .toHandle(d)
              .fold(
                log
                  .warn(s"[DBSubscriptionFlowPrompt] failed to decode prompt for chatId=$chatId messageId=$messageId")
                  .as(none[SubscriptionFlowPromptHandle])
              )(_.some.pure[F])
          }
          .map(_.flatten)
      } yield handle

    override def remove(key: SubscriptionFlowKey): F[Unit] =
      for {
        now <- nowMillis
        _   <- (pruneExpired(now) *> sql"""DELETE FROM subscription_flow_prompt
                    WHERE platform = ${key.chatId.platform.id}
                      AND chat_id = ${key.chatId.id}
                      AND bot_id = ${key.botId.value}
                      AND requester = ${key.requester}""".update.run).transact(transactor).void
      } yield ()
  }
}
