package com.benkio.richardphjbensonbot.db

import cats.effect.Async
import cats.implicits._
import com.benkio.richardphjbensonbot.model.Timeout
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.Transactor
import doobie._
import log.effect.LogWriter

sealed trait DBTimeout[F[_]] {
  def getOrDefault(chatId: Long): F[Timeout]
  def setTimeout(timeout: Timeout): F[Unit]
  def logLastInteraction(chatId: Long): F[Unit]
}

object DBTimeout {

  private[db] class DBTimeoutImpl[F[_]: Async](transactor: Transactor[F], log: LogWriter[F]) extends DBTimeout[F] {

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
