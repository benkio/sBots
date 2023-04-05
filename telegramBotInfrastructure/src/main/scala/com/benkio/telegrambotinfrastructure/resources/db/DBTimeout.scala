package com.benkio.telegrambotinfrastructure.resources.db

import cats.effect.Async
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.Timeout
import doobie.implicits._
import doobie.{Transactor, _}
import log.effect.LogWriter

import java.time.Instant

final case class DBTimeoutData(
    chat_id: Long,
    bot_name: String,
    timeout_value: String,
    last_interaction: String
)

object DBTimeoutData {
  def apply(timeout: Timeout): DBTimeoutData = DBTimeoutData(
    chat_id = timeout.chatId,
    bot_name = timeout.botName,
    timeout_value = timeout.timeoutValue.toMillis.toString,
    last_interaction = timeout.lastInteraction.getEpochSecond.toString
  )
}

trait DBTimeout[F[_]] {
  def getOrDefault(chatId: Long, botName: String): F[DBTimeoutData]
  def setTimeout(timeout: DBTimeoutData): F[Unit]
  def logLastInteraction(chatId: Long, botName: String): F[Unit]

  def getOrDefaultQuery(chatId: Long, botName: String): Query0[DBTimeoutData]
  def setTimeoutQuery(timeout: DBTimeoutData): Update0
  def logLastInteractionQuery(chatId: Long, botName: String): Update0
}

object DBTimeout {

  private[db] class DBTimeoutImpl[F[_]: Async](transactor: Transactor[F], log: LogWriter[F]) extends DBTimeout[F] {

    override def getOrDefaultQuery(chatId: Long, botName: String): Query0[DBTimeoutData] =
      sql"SELECT chat_id, bot_name, timeout_value, last_interaction FROM timeout WHERE chat_id = $chatId AND bot_name = $botName"
        .query[DBTimeoutData]

    override def setTimeoutQuery(timeout: DBTimeoutData): Update0 =
      sql"INSERT INTO timeout (chat_id, bot_name, timeout_value, last_interaction) VALUES (${timeout.chat_id}, ${timeout.bot_name}, ${timeout.timeout_value}, ${timeout.last_interaction}) ON CONFLICT (chat_id, bot_name) DO UPDATE SET timeout_value = EXCLUDED.timeout_value, last_interaction = EXCLUDED.last_interaction".update

    override def logLastInteractionQuery(chatId: Long, botName: String): Update0 =
      sql"UPDATE timeout SET last_interaction = ${Instant.now().getEpochSecond()} WHERE chat_id = $chatId AND bot_name = $botName".update

    override def getOrDefault(chatId: Long, botName: String): F[DBTimeoutData] =
      log.info(s"DB fetching timeout for $chatId - $botName") *>
        getOrDefaultQuery(chatId, botName).option.transact(transactor).map(_.getOrElse(DBTimeoutData(Timeout(chatId, botName))))

    override def setTimeout(timeout: DBTimeoutData): F[Unit] =
      log.info(s"DB setting timeout for ${timeout.chat_id} - ${timeout.bot_name} to value ${timeout.timeout_value}") *>
        setTimeoutQuery(timeout).run.transact(transactor).void

    override def logLastInteraction(chatId: Long, botName: String): F[Unit] =
      log.info(s"DB logging the last interaction for chat $chatId - $botName") *>
        logLastInteractionQuery(chatId, botName).run
          .transact(transactor)
          .attemptSql
          .flatMap {
            case Left(e) =>
              log.info(s"Sql Exception on logging last interaction: $e") *>
                setTimeout(DBTimeoutData(Timeout(chatId, botName)))
            case Right(_) => ().pure[F]
          }
  }

}
