package com.benkio.richardphjbensonbot

import cats.effect.Async
import cats.implicits._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.Transactor
import doobie._
import log.effect.LogWriter

import java.sql.Timestamp
import java.time.Instant
import scala.concurrent.duration._

final case class Timeout(chat_id: Long, timeout_value: String, last_interaction: Timestamp)

object Timeout {
  def isExpired(timeout: Timeout): Boolean = {
    val now = Timestamp.from(Instant.now())
    now.after(
      /*last interaction + timeout value*/
      new Timestamp(timeout.last_interaction.getTime() + timeout.timeout_value.toInt)
    )
  }

}

sealed trait DBTimeout[F[_]] {
  def getOrDefault(chatId: Long): F[Timeout]
  def setTimeout(chatId: Long, timeout: FiniteDuration): F[Unit]
  def logLastInteraction(chatId: Long): F[Unit]
}

object DBTimeout {

  def defaultTimeout(chatId: Long): Timeout = Timeout(
    chat_id = chatId,
    timeout_value = "0",
    last_interaction = Timestamp.from(Instant.now())
  )

  def apply[F[_]: Async](transactor: Transactor[F])(implicit log: LogWriter[F]): DBTimeout[F] =
    new DBTimeoutImpl[F](
      transactor,
      log
    )

  private class DBTimeoutImpl[F[_]: Async](transactor: Transactor[F], log: LogWriter[F]) extends DBTimeout[F] {

    private def getOrDefaultSql(chatId: Long): Query0[Timeout] =
      sql"SELECT chat_id, timeout_value, last_interaction FROM timeout WHERE chat_id = $chatId"
        .query[Timeout]

    private def setTimeoutSql(chatId: Long, timeout: FiniteDuration): Update0 =
      sql"INSERT INTO timeout (chat_id, timeout_value, last_interaction) VALUES ($chatId, ${timeout.toMillis}, NOW()::timestamp) ON CONFLICT (chat_id) DO UPDATE SET timeout_value = EXCLUDED.timeout_value, last_interaction = EXCLUDED.last_interaction".update

    private def logLastInteractionSql(chatId: Long): Update0 =
      sql"UPDATE timeout SET last_interaction = NOW()::timestamp WHERE chat_id = $chatId".update

    def getOrDefault(chatId: Long): F[Timeout] =
      log.info(s"DB fetching timeout for $chatId") *>
        getOrDefaultSql(chatId).option.transact(transactor).map(_.getOrElse(defaultTimeout(chatId)))

    def setTimeout(chatId: Long, timeout: FiniteDuration): F[Unit] =
      log.info(s"DB setting timeout for $chatId to value $timeout") *>
        setTimeoutSql(chatId, timeout).run.transact(transactor).void

    def logLastInteraction(chatId: Long): F[Unit] =
      log.info(s"DB logging the last interaction for chat $chatId") *>
        logLastInteractionSql(chatId).run
          .transact(transactor)
          .onSqlException(setTimeout(chatId, 0.seconds))
          .void
  }

}
