package com.benkio.chatcore.repository.db

import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.model.*
import com.benkio.chatcore.SubscriptionDraftPutResult
import com.benkio.chatcore.SubscriptionDraftStartResult
import doobie.*
import doobie.implicits.*
import log.effect.LogWriter

import scala.concurrent.duration.*

final case class DBSubscriptionDraftData(
    platform: String,
    chat_id: String,
    bot_id: String,
    requester: String,
    step: String,
    subscription_type: Option[String],
    schedule: Option[String],
    target: Option[String],
    saved_at_millis: Long
)

object DBSubscriptionDraftData {
  def apply(draft: SubscriptionDraft): DBSubscriptionDraftData =
    DBSubscriptionDraftData(
      platform = draft.key.chatId.platform.id,
      chat_id = draft.key.chatId.id,
      bot_id = draft.key.botId.value,
      requester = draft.key.requester,
      step = draft.step.asString,
      subscription_type = draft.subscriptionType.map(_.asString),
      schedule = draft.schedule,
      target = draft.target,
      saved_at_millis = draft.savedAt.toMillis
    )

  def toDraft(data: DBSubscriptionDraftData): Option[SubscriptionDraft] =
    for {
      platform <- Platform.fromId(data.platform).toOption
      step     <- SubscriptionDraftStep.values.find(_.asString == data.step)
      subType  <- data.subscription_type.traverse(SubscriptionType.fromString(_).toOption)
    } yield SubscriptionDraft(
      key = SubscriptionFlowKey(ChatId(platform, data.chat_id), SBotInfo.SBotId(data.bot_id), data.requester),
      step = step,
      subscriptionType = subType,
      schedule = data.schedule,
      target = data.target,
      savedAt = data.saved_at_millis.millis
    )
}

trait DBSubscriptionDraft[F[_]] {
  def start(draft: SubscriptionDraft): F[SubscriptionDraftStartResult]
  def get(key: SubscriptionFlowKey): F[Option[SubscriptionDraft]]
  def put(draft: SubscriptionDraft): F[SubscriptionDraftPutResult]
  def remove(key: SubscriptionFlowKey): F[Unit]
  def size: F[Int]
}

object DBSubscriptionDraft {

  def apply[F[_]: Async](
      transactor: Transactor[F],
      botId: SBotInfo.SBotId,
      expiry: FiniteDuration,
      maxDrafts: Int
  )(using log: LogWriter[F]): DBSubscriptionDraft[F] =
    new DBSubscriptionDraftImpl[F](transactor, botId, expiry, maxDrafts, log)

  private[db] class DBSubscriptionDraftImpl[F[_]: Async](
      transactor: Transactor[F],
      botId: SBotInfo.SBotId,
      expiry: FiniteDuration,
      maxDrafts: Int,
      log: LogWriter[F]
  ) extends DBSubscriptionDraft[F] {

    private def nowMillis: F[Long] = Clock[F].realTime.map(_.toMillis)

    private def expiryCutoff(now: Long): Long = now - expiry.toMillis

    private def pruneExpired(now: Long): ConnectionIO[Int] =
      sql"DELETE FROM subscription_draft WHERE bot_id = ${botId.value} AND saved_at_millis <= ${expiryCutoff(now)}".update.run

    private def countLive(now: Long): ConnectionIO[Int] =
      sql"SELECT COUNT(*) FROM subscription_draft WHERE bot_id = ${botId.value} AND saved_at_millis > ${expiryCutoff(now)}"
        .query[Int]
        .unique

    private def existsLive(key: SubscriptionFlowKey, now: Long): ConnectionIO[Boolean] =
      sql"""SELECT 1 FROM subscription_draft
            WHERE platform = ${key.chatId.platform.id}
              AND chat_id = ${key.chatId.id}
              AND bot_id = ${key.botId.value}
              AND requester = ${key.requester}
              AND saved_at_millis > ${expiryCutoff(now)}"""
        .query[Int]
        .option
        .map(_.isDefined)

    override def start(draft: SubscriptionDraft): F[SubscriptionDraftStartResult] =
      for {
        now    <- nowMillis
        result <- (pruneExpired(now) *> startTx(draft, now)).transact(transactor)
        _      <- log.debug(s"[DBSubscriptionDraft] start key=${draft.key} result=$result")
      } yield result

    private def startTx(draft: SubscriptionDraft, now: Long): ConnectionIO[SubscriptionDraftStartResult] = {
      val d      = DBSubscriptionDraftData(draft)
      val cutoff = expiryCutoff(now)
      for {
        _ <-
          sql"""INSERT INTO subscription_draft (platform, chat_id, bot_id, requester, step, subscription_type, schedule, target, saved_at_millis)
                        SELECT ${d.platform}, ${d.chat_id}, ${d.bot_id}, ${d.requester}, ${d.step}, ${d.subscription_type}, ${d.schedule}, ${d.target}, ${d.saved_at_millis}
                        WHERE EXISTS (
                          SELECT 1 FROM subscription_draft
                          WHERE platform = ${d.platform} AND chat_id = ${d.chat_id} AND bot_id = ${d.bot_id} AND requester = ${d.requester}
                            AND saved_at_millis > $cutoff
                        ) OR (
                          SELECT COUNT(*) FROM subscription_draft WHERE bot_id = ${d.bot_id} AND saved_at_millis > $cutoff
                        ) < $maxDrafts
                        ON CONFLICT(platform, chat_id, bot_id, requester) DO UPDATE SET
                          step = EXCLUDED.step,
                          subscription_type = EXCLUDED.subscription_type,
                          schedule = EXCLUDED.schedule,
                          target = EXCLUDED.target,
                          saved_at_millis = EXCLUDED.saved_at_millis""".update.run
        exists <- existsLive(draft.key, now)
      } yield if exists then SubscriptionDraftStartResult.Started else SubscriptionDraftStartResult.Full
    }

    override def get(key: SubscriptionFlowKey): F[Option[SubscriptionDraft]] =
      for {
        now  <- nowMillis
        data <- (pruneExpired(
          now
        ) *> sql"""SELECT platform, chat_id, bot_id, requester, step, subscription_type, schedule, target, saved_at_millis
                   FROM subscription_draft
                   WHERE platform = ${key.chatId.platform.id}
                     AND chat_id = ${key.chatId.id}
                     AND bot_id = ${key.botId.value}
                     AND requester = ${key.requester}
                     AND saved_at_millis > ${expiryCutoff(now)}"""
          .query[DBSubscriptionDraftData]
          .option).transact(transactor)
        draft <- data
          .traverse { d =>
            DBSubscriptionDraftData.toDraft(d) match {
              case Some(v) => v.some.pure[F]
              case None    =>
                log.warn(s"[DBSubscriptionDraft] failed to decode draft for key=$key").as(none[SubscriptionDraft])
            }
          }
          .map(_.flatten)
      } yield draft

    override def put(draft: SubscriptionDraft): F[SubscriptionDraftPutResult] =
      start(draft).map {
        case SubscriptionDraftStartResult.Started => SubscriptionDraftPutResult.Saved
        case SubscriptionDraftStartResult.Full    => SubscriptionDraftPutResult.Full
      }

    override def remove(key: SubscriptionFlowKey): F[Unit] =
      for {
        now <- nowMillis
        _   <- (pruneExpired(now) *> sql"""DELETE FROM subscription_draft
              WHERE platform = ${key.chatId.platform.id}
                AND chat_id = ${key.chatId.id}
                AND bot_id = ${key.botId.value}
                AND requester = ${key.requester}""".update.run).transact(transactor)
      } yield ()

    override def size: F[Int] =
      for {
        now    <- nowMillis
        result <- (pruneExpired(now) *> countLive(now)).transact(transactor)
      } yield result
  }
}
