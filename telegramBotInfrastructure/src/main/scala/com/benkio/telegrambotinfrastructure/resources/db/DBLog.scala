package com.benkio.telegrambotinfrastructure.resources.db

import java.time.Instant
import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._

trait DBLog[F[_]] {
  def writeLog(logMessage: String): F[Unit]
}

object DBLog {

  def apply[F[_]: Async](
    transactor: Transactor[F],
  ): DBLog[F] =
    new DBLogImpl[F](
      transactor = transactor,
    )

  private[telegrambotinfrastructure] class DBLogImpl[F[_]: Async](
    transactor: Transactor[F]
  ) extends DBlog[F] {

    override def writeLog(logMessage: String): F[Unit] = for {
      // get current time
      currentTime <- Async[F].delay(Instant.now().toEpochMilli)
      // truncate message to the max TEXT of SqlLite
      m <- Async[F].pure(logMessage.substring(0, 2147483647))
      // Write the log
      _ <- writeLogQuery(currentTime, m).run.transact(transactor).void
    } yield ()

    def writeLogQuery(logTime: Long, logMessage: String): Update0 =
      sql"INSERT INTO log (log_time, message) VALUES (${logTime}, ${logMessage})".update
  }
}
