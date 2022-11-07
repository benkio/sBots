package com.benkio.telegrambotinfrastructure.resources.db

import cats.effect.Async
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.Timeout
import com.benkio.telegrambotinfrastructure.model.Timeout._
import doobie.implicits._
import doobie.Transactor
import doobie._
import log.effect.LogWriter

import java.sql.Timestamp
import java.time.Instant

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
      sql"INSERT INTO timeout (chat_id, timeout_value, last_interaction) VALUES (${timeout.chat_id}, ${timeout.timeout_value}, ${timeout.last_interaction.getTime()}) ON CONFLICT (chat_id) DO UPDATE SET timeout_value = EXCLUDED.timeout_value, last_interaction = EXCLUDED.last_interaction".update

    private def logLastInteractionSql(chatId: Long): Update0 =
      sql"UPDATE timeout SET last_interaction = ${Timestamp.from(Instant.now()).getTime()} WHERE chat_id = $chatId".update

    override def getOrDefault(chatId: Long): F[Timeout] =
      log.info(s"DB fetching timeout for $chatId") *>
        getOrDefaultSql(chatId).option.transact(transactor).map(_.getOrElse(Timeout.defaultTimeout(chatId)))

    override def setTimeout(timeout: Timeout): F[Unit] =
      log.info(s"DB setting timeout for ${timeout.chat_id} to value ${timeout.timeout_value}") *>
        setTimeoutSql(timeout).run.transact(transactor).void

    override def logLastInteraction(chatId: Long): F[Unit] =
      log.info(s"DB logging the last interaction for chat $chatId") *>
        logLastInteractionSql(chatId).run
          .transact(transactor)
          .attemptSql
          .flatMap {
            case Left(e) =>
              log.info(s"Sql Exception on logging last interaction: $e") *>
                setTimeout(Timeout.defaultTimeout(chatId))
            case Right(_) => ().pure[F]
          }
  }

}
