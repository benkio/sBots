package com.benkio.chatcore.repository.db

import cats.effect.kernel.Ref
import cats.effect.Async
import cats.implicits.*
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.SBotInfo.SBotId
import doobie.*
import doobie.implicits.*
import log.effect.LogWriter

import java.time.Instant
import scala.concurrent.duration.*

/** The incoming messages already handled by a bot, to not answer twice a message the platform delivers more than once.
  */
trait DBProcessedMessage[F[_]] {

  /** Records the message as processed, atomically: only the first call for the same platform, chat, bot and message
    * gives `true`, any other gives `false`.
    */
  def markIfNew(chatId: ChatId, botId: SBotId, messageId: MessageId): F[Boolean]

  /** Forgets the messages processed before the given time (epoch seconds), gives back how many. */
  def deleteOlderThan(epochSeconds: Long): F[Int]
}

object DBProcessedMessage {

  val defaultRetention: FiniteDuration       = 24.hours
  val defaultCleanupInterval: FiniteDuration = 1.hour

  /** The old rows are deleted while marking, at most once per `cleanupInterval`, so no background job is needed. */
  def apply[F[_]: Async](
      transactor: Transactor[F],
      retention: FiniteDuration = defaultRetention,
      cleanupInterval: FiniteDuration = defaultCleanupInterval
  )(using log: LogWriter[F]): F[DBProcessedMessage[F]] =
    Ref
      .of[F, Long](0L)
      .map(lastCleanup => DBProcessedMessageImpl[F](transactor, retention, cleanupInterval, lastCleanup))

  private[chatcore] class DBProcessedMessageImpl[F[_]: Async](
      transactor: Transactor[F],
      retention: FiniteDuration,
      cleanupInterval: FiniteDuration,
      lastCleanup: Ref[F, Long]
  )(using log: LogWriter[F])
      extends DBProcessedMessage[F] {

    override def markIfNew(chatId: ChatId, botId: SBotId, messageId: MessageId): F[Boolean] =
      for {
        now      <- Async[F].delay(Instant.now().getEpochSecond)
        inserted <- markQuery(chatId = chatId, botId = botId, messageId = messageId, processedAt = now).run
          .transact(transactor)
          .map(_ == 1)
        _ <- cleanupIfDue(now)
      } yield inserted

    override def deleteOlderThan(epochSeconds: Long): F[Int] =
      deleteOlderThanQuery(epochSeconds).run.transact(transactor)

    private def cleanupIfDue(now: Long): F[Unit] = {
      val cleanup: F[Unit] =
        deleteOlderThan(now - retention.toSeconds).attempt.flatMap {
          case Right(deleted) => log.debug(s"DB deleted $deleted processed messages older than $retention")
          case Left(e)        => log.error(s"DB failed to delete the old processed messages: $e")
        }
      lastCleanup
        .modify(last => if now - last >= cleanupInterval.toSeconds then (now, true) else (last, false))
        .flatMap(cleanup.whenA)
    }
  }

  def markQuery(chatId: ChatId, botId: SBotId, messageId: MessageId, processedAt: Long): Update0 =
    sql"INSERT INTO processed_message (platform, bot_id, chat_id, message_id, processed_at) VALUES (${chatId.platform.id}, ${botId.value}, ${chatId.id}, ${messageId.value}, $processedAt) ON CONFLICT (platform, bot_id, chat_id, message_id) DO NOTHING".update

  def deleteOlderThanQuery(epochSeconds: Long): Update0 =
    sql"DELETE FROM processed_message WHERE processed_at < $epochSeconds".update
}
