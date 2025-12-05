package com.benkio.telegrambotinfrastructure.repository.db

import cats.effect.Async
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.ChatId
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.Timeout
import doobie.*
import doobie.implicits.*
import log.effect.LogWriter

import java.time.Instant

final case class DBTimeoutData(
    chat_id: Long,
    bot_id: String,
    timeout_value: String,
    last_interaction: String
)

object DBTimeoutData {
  def apply(timeout: Timeout): DBTimeoutData = DBTimeoutData(
    chat_id = timeout.chatId.value,
    bot_id = timeout.botId.value,
    timeout_value = timeout.timeoutValue.toMillis.toString,
    last_interaction = timeout.lastInteraction.getEpochSecond.toString
  )
}

trait DBTimeout[F[_]] {
  def getOrDefault(chatId: Long, botId: SBotId): F[DBTimeoutData]
  def setTimeout(timeout: DBTimeoutData): F[Unit]
  def removeTimeout(chatId: Long, botId: SBotId): F[Unit]
  def logLastInteraction(chatId: Long, botId: SBotId): F[Unit]
}

object DBTimeout {

  private[db] class DBTimeoutImpl[F[_]: Async](transactor: Transactor[F], log: LogWriter[F]) extends DBTimeout[F] {

    override def getOrDefault(chatId: Long, botId: SBotId): F[DBTimeoutData] =
      log.info(s"DB fetching timeout for $chatId - $botId") *>
        getOrDefaultQuery(chatId, botId).option
          .transact(transactor)
          .map(_.getOrElse(DBTimeoutData(Timeout(ChatId(chatId), botId))))

    override def setTimeout(timeout: DBTimeoutData): F[Unit] =
      log.info(s"DB setting timeout for ${timeout.chat_id} - ${timeout.bot_id} to value ${timeout.timeout_value}") *>
        setTimeoutQuery(timeout).run.transact(transactor).void

    override def removeTimeout(chatId: Long, botId: SBotId): F[Unit] =
      log.info(s"DB removing timeout for $chatId - $botId") *>
        removeTimeoutQuery(chatId, botId).run
          .transact(transactor)
          .void

    override def logLastInteraction(chatId: Long, botId: SBotId): F[Unit] =
      log.info(s"DB logging the last interaction for chat $chatId - $botId") *>
        logLastInteractionQuery(chatId, botId).run
          .transact(transactor)
          .attemptSql
          .flatMap {
            case Left(e) =>
              log.info(s"Sql Exception on logging last interaction: $e") *>
                setTimeout(DBTimeoutData(Timeout(ChatId(chatId), botId)))
            case Right(_) => ().pure[F]
          }
  }

  def getOrDefaultQuery(chatId: Long, botId: SBotId): Query0[DBTimeoutData] =
    sql"SELECT chat_id, bot_id, timeout_value, last_interaction FROM timeout WHERE chat_id = $chatId AND bot_id = ${botId.value}"
      .query[DBTimeoutData]

  def setTimeoutQuery(timeout: DBTimeoutData): Update0 =
    sql"INSERT INTO timeout (chat_id, bot_id, timeout_value, last_interaction) VALUES (${timeout.chat_id}, ${timeout.bot_id}, ${timeout.timeout_value}, ${timeout.last_interaction}) ON CONFLICT (chat_id, bot_id) DO UPDATE SET timeout_value = EXCLUDED.timeout_value, last_interaction = EXCLUDED.last_interaction".update

  def removeTimeoutQuery(chatId: Long, botId: SBotId): Update0 =
    sql"DELETE FROM timeout WHERE chat_id = $chatId AND bot_id = ${botId.value}".update

  def logLastInteractionQuery(chatId: Long, botId: SBotId): Update0 =
    sql"UPDATE timeout SET last_interaction = ${Instant.now().getEpochSecond()} WHERE chat_id = $chatId AND bot_id = ${botId.value}".update
}
