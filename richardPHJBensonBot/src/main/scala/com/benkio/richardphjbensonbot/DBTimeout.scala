package com.benkio.richardphjbensonbot

import cats.effect.Async
import cats.implicits._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.{Transactor, _}
import log.effect.LogWriter
import telegramium.bots.{ChatId, ChatIntId, ChatStrId}

import java.sql.Timestamp
import java.time.Instant
import scala.concurrent.duration.FiniteDuration



sealed trait DBTimeout[F[_]] {
  def getOrDefault(chatId: ChatId): F[DBTimeout.Timeout]
  def setTimeout(chatId: ChatId, timeout: FiniteDuration): F[Unit]
}

object DBTimeout {
  final case class Timeout(chat_id: String, timeout_value: String, last_interaction: Timestamp)

  def defaultTimeout(chatId: ChatId): Timeout = Timeout(
    chat_id = chatId match {
      case ChatIntId(id) => id.toString
      case ChatStrId(id) => id
    },
    timeout_value = "0",
    last_interaction = Timestamp.from(Instant.now())
  )

  def apply[F[_]: Async](transactor: Transactor[F])(implicit log: LogWriter[F]): DBTimeout[F] =
    new DBTimeoutImpl[F](
      transactor,
      log
    )

  private class DBTimeoutImpl[F[_]: Async](transactor: Transactor[F], log: LogWriter[F]) extends DBTimeout[F] {

    private def chatIdFragment(chatId: ChatId): Fragment = chatId match {
      case ChatIntId(id) => Fragment.const(id.toString)
      case ChatStrId(id) => Fragment.const(id)
    }

    private def getOrDefaultSql(chatId: ChatId): Query0[DBTimeout.Timeout] =
      sql"SELECT chat_id, timeout_value, last_interaction FROM timeout WHERE chat_id = ${chatIdFragment(chatId)}".query[DBTimeout.Timeout]

    private def setTimeoutSql(chatId: ChatId, timeout: FiniteDuration): Update0 =
      sql"INSERT INTO timeout (chat_id, timeout_value, last_interaction) VALUES (${chatIdFragment(chatId)}, ${new Timestamp(timeout.toMillis)}, NOW()::timestamp) ON CONFLICT (chat_id) DO UPDATE SET timeout_value = EXCLUDED.timeout_value, last_interaction = EXCLUDED.last_interaction".update

    def getOrDefault(chatId: ChatId): F[DBTimeout.Timeout] =
      log.info(s"DB fetching timeout for $chatId") *>
    getOrDefaultSql(chatId).option.transact(transactor).map(_.getOrElse(defaultTimeout(chatId)))

    def setTimeout(chatId: ChatId, timeout: FiniteDuration): F[Unit] =
      log.info(s"DB setting timeout for $chatId to value $timeout") *>
    setTimeoutSql(chatId, timeout).run.transact(transactor).void
  }

}
