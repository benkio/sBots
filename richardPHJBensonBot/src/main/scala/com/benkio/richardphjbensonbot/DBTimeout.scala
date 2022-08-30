package com.benkio.richardphjbensonbot

import cats.effect.Async
import cats.implicits._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.Transactor
import doobie._
import log.effect.LogWriter
import telegramium.bots.Message

import java.sql.Timestamp
import java.time.Instant
import scala.concurrent.duration._
import scala.util.Try

final case class Timeout(chat_id: Long, timeout_value: String, last_interaction: Timestamp)

object Timeout {
  def isExpired(timeout: Timeout): Boolean = {
    val now = Timestamp.from(Instant.now())
    now.after(
      /*last interaction + timeout value*/
      new Timestamp(timeout.last_interaction.getTime() + timeout.timeout_value.toInt)
    )
  }

  def defaultTimeout(chatId: Long): Timeout = Timeout(
    chat_id = chatId,
    timeout_value = "0",
    last_interaction = Timestamp.from(Instant.now())
  )

  def apply(m: Message, timeout: String): Option[Timeout] =
    Try(
      timeout
        .split(":")
        .map(_.toLong)
        .zip(List(HOURS, MINUTES, SECONDS))
        .map { case (value, timeUnit) =>
          Duration(value, timeUnit)
        }
        .reduce(_ + _)
    )
      .map(duration => defaultTimeout(m.chat.id).copy(timeout_value = duration.toMillis.toString))
      .toOption

}

sealed trait DBTimeout[F[_]] {
  def getOrDefault(chatId: Long): F[Timeout]
  def setTimeout(timeout: Timeout): F[Unit]
  def logLastInteraction(chatId: Long): F[Unit]
}

object DBTimeout {

  def apply[F[_]: Async](transactor: Transactor[F])(implicit log: LogWriter[F]): DBTimeout[F] =
    new DBTimeoutImpl[F](
      transactor,
      log
    )

  private class DBTimeoutImpl[F[_]: Async](transactor: Transactor[F], log: LogWriter[F]) extends DBTimeout[F] {

    private def getOrDefaultSql(chatId: Long): Query0[Timeout] =
      sql"SELECT chat_id, timeout_value, last_interaction FROM timeout WHERE chat_id = $chatId"
        .query[Timeout]

    private def setTimeoutSql(timeout: Timeout): Update0 =
      sql"INSERT INTO timeout (chat_id, timeout_value, last_interaction) VALUES (${timeout.chat_id}, ${timeout.timeout_value}, datetime('now')) ON CONFLICT (chat_id) DO UPDATE SET timeout_value = EXCLUDED.timeout_value, last_interaction = EXCLUDED.last_interaction".update

    private def logLastInteractionSql(chatId: Long): Update0 =
      sql"UPDATE timeout SET last_interaction = datetime('now') WHERE chat_id = $chatId".update

    def getOrDefault(chatId: Long): F[Timeout] =
      log.info(s"DB fetching timeout for $chatId") *>
        getOrDefaultSql(chatId).option.transact(transactor).map(_.getOrElse(Timeout.defaultTimeout(chatId)))

    def setTimeout(timeout: Timeout): F[Unit] =
      log.info(s"DB setting timeout for ${timeout.chat_id} to value ${timeout.timeout_value}") *>
        setTimeoutSql(timeout).run.transact(transactor).void

    def logLastInteraction(chatId: Long): F[Unit] =
      log.info(s"DB logging the last interaction for chat $chatId") *>
        logLastInteractionSql(chatId).run
          .transact(transactor)
          .onSqlException(setTimeout(Timeout.defaultTimeout(chatId)))
          .void
  }

}
