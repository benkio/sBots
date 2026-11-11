package com.benkio.chatcore.repository.db

import cats.effect.Async
import cats.implicits.*
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.Timeout
import doobie.*
import doobie.implicits.*
import log.effect.LogWriter

import java.time.Instant

final case class DBTimeoutData(
    platform: String,
    chat_id: String,
    bot_id: String,
    timeout_value: String,
    last_interaction: String
)

object DBTimeoutData {
  def apply(timeout: Timeout): DBTimeoutData = DBTimeoutData(
    platform = timeout.chatId.platform.id,
    chat_id = timeout.chatId.id,
    bot_id = timeout.botId.value,
    timeout_value = timeout.timeoutValue.toMillis.toString,
    last_interaction = timeout.lastInteraction.getEpochSecond.toString
  )
}

trait DBTimeout[F[_]] {
  def getOrDefault(chatId: ChatId, botId: SBotId): F[DBTimeoutData]
  def setTimeout(timeout: DBTimeoutData): F[Unit]
  def removeTimeout(chatId: ChatId, botId: SBotId): F[Unit]
  def logLastInteraction(chatId: ChatId, botId: SBotId): F[Unit]
}

object DBTimeout {

  private[db] class DBTimeoutImpl[F[_]: Async](transactor: Transactor[F], log: LogWriter[F]) extends DBTimeout[F] {

    override def getOrDefault(chatId: ChatId, botId: SBotId): F[DBTimeoutData] =
      log.info(s"DB fetching timeout for $chatId - $botId") *>
        getOrDefaultQuery(chatId, botId).option
          .transact(transactor)
          .map(_.getOrElse(DBTimeoutData(Timeout(chatId, botId))))

    override def setTimeout(timeout: DBTimeoutData): F[Unit] =
      log.info(
        s"DB setting timeout for ${timeout.platform}:${timeout.chat_id} - ${timeout.bot_id} to value ${timeout.timeout_value}"
      ) *>
        setTimeoutQuery(timeout).run.transact(transactor).void

    override def removeTimeout(chatId: ChatId, botId: SBotId): F[Unit] =
      log.info(s"DB removing timeout for $chatId - $botId") *>
        removeTimeoutQuery(chatId, botId).run
          .transact(transactor)
          .void

    override def logLastInteraction(chatId: ChatId, botId: SBotId): F[Unit] =
      log.info(s"DB logging the last interaction for chat $chatId - $botId") *>
        logLastInteractionQuery(chatId, botId).run
          .transact(transactor)
          .attemptSql
          .flatMap {
            case Left(e) =>
              log.info(s"Sql Exception on logging last interaction: $e") *>
                setTimeout(DBTimeoutData(Timeout(chatId, botId)))
            case Right(_) => ().pure[F]
          }
  }

  def getOrDefaultQuery(chatId: ChatId, botId: SBotId): Query0[DBTimeoutData] =
    sql"SELECT platform, chat_id, bot_id, timeout_value, last_interaction FROM timeout WHERE platform = ${chatId.platform.id} AND chat_id = ${chatId.id} AND bot_id = ${botId.value}"
      .query[DBTimeoutData]

  def setTimeoutQuery(timeout: DBTimeoutData): Update0 =
    sql"INSERT INTO timeout (platform, chat_id, bot_id, timeout_value, last_interaction) VALUES (${timeout.platform}, ${timeout.chat_id}, ${timeout.bot_id}, ${timeout.timeout_value}, ${timeout.last_interaction}) ON CONFLICT (platform, chat_id, bot_id) DO UPDATE SET timeout_value = EXCLUDED.timeout_value, last_interaction = EXCLUDED.last_interaction".update

  def removeTimeoutQuery(chatId: ChatId, botId: SBotId): Update0 =
    sql"DELETE FROM timeout WHERE platform = ${chatId.platform.id} AND chat_id = ${chatId.id} AND bot_id = ${botId.value}".update

  def logLastInteractionQuery(chatId: ChatId, botId: SBotId): Update0 =
    sql"UPDATE timeout SET last_interaction = ${Instant.now().getEpochSecond()} WHERE platform = ${chatId.platform.id} AND chat_id = ${chatId.id} AND bot_id = ${botId.value}".update
}
