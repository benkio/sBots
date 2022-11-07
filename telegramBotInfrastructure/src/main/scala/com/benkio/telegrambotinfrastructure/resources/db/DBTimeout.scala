package com.benkio.telegrambotinfrastructure.resources.db

import cats.effect.Async
import cats.implicits._
import doobie.implicits._
import doobie.Transactor
import doobie._
import log.effect.LogWriter

import java.sql.Timestamp
import java.time.Instant
import doobie.util.Get
import doobie.util.Put

sealed trait DBTimeout[F[_]] {
  def getOrDefault(chatId: Long): F[DBTimeoutData]
  def setTimeout(timeout: DBTimeoutData): F[Unit]
  def logLastInteraction(chatId: Long): F[Unit]
}

object DBTimeout {

    implicit val getTimestamp: Get[Timestamp] = Get[String].map(s => new Timestamp(s.toLong))
  implicit val putTimestamp: Put[Timestamp] = Put[String].contramap(_.getTime.toString)

  final case class DBTimeoutData(
    chat_id: Long,
    timeout_value: String,
    last_interaction: Timestamp)

  object DBTimeoutData {
    def apply(timeout: Timeout): DBTimeoutData = DBTimeoutData(
      chat_id = timeout.chatId,
      timeout_value = timeout.timeoutValue.toMillis.toString,
      last_interaction = timeout.lastInteraction
    )
  }

  private[db] class DBTimeoutImpl[F[_]: Async](transactor: Transactor[F], log: LogWriter[F]) extends DBTimeout[F] {

    private def getOrDefaultSql(chatId: Long): Query0[DBTimeoutData] =
      sql"SELECT chat_id, timeout_value, last_interaction FROM timeout WHERE chat_id = $chatId"
        .query[DBTimeoutData]

    private def setTimeoutSql(timeout: DBTimeoutData): Update0 =
      sql"INSERT INTO timeout (chat_id, timeout_value, last_interaction) VALUES (${timeout.chat_id}, ${timeout.timeout_value}, ${timeout.last_interaction.getTime()}) ON CONFLICT (chat_id) DO UPDATE SET timeout_value = EXCLUDED.timeout_value, last_interaction = EXCLUDED.last_interaction".update

    private def logLastInteractionSql(chatId: Long): Update0 =
      sql"UPDATE timeout SET last_interaction = ${Timestamp.from(Instant.now()).getTime()} WHERE chat_id = $chatId".update

    override def getOrDefault(chatId: Long): F[DBTimeoutData] =
      log.info(s"DB fetching timeout for $chatId") *>
        getOrDefaultSql(chatId).option.transact(transactor).map(_.getOrElse(Timeout.defaultTimeout(chatId)))

    override def setTimeout(timeout: DBTimeoutData): F[Unit] =
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
