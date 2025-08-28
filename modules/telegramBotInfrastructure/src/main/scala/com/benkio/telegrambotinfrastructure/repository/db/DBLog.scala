package com.benkio.telegrambotinfrastructure.repository.db

import cats.effect.*
import cats.implicits.*
import doobie.*
import doobie.implicits.*

import java.time.Instant

final case class DBLogData(log_time: Long, message: String)

trait DBLog[F[_]] {
  def writeLog(logMessage: String): F[Unit]
  def getLastLog(): F[Option[DBLogData]]
}

object DBLog {

  def apply[F[_]: Async](
      transactor: Transactor[F]
  ): DBLog[F] =
    new DBLogImpl[F](
      transactor = transactor
    )

  private[telegrambotinfrastructure] class DBLogImpl[F[_]: Async](
      transactor: Transactor[F]
  ) extends DBLog[F] {

    override def writeLog(logMessage: String): F[Unit] = for {
      // get current time
      currentTime <- Async[F].delay(Instant.now().toEpochMilli)
      // truncate message to the max TEXT of SqlLite
      m <- Async[F].pure(logMessage.take(2147483647))
      // Write the log
      _ <- writeLogQuery(currentTime, m).run.transact(transactor).void
    } yield ()

    def getLastLog(): F[Option[DBLogData]] =
      getLastLogQuery().option.transact(transactor)

    def writeLogQuery(logTime: Long, logMessage: String): Update0 =
      sql"INSERT INTO log (log_time, message) VALUES (${logTime}, ${logMessage})".update

    def getLastLogQuery(): Query0[DBLogData] =
      sql"SELECT log_time, message FROM log ORDER BY log_time DESC LIMIT 1"
        .query[DBLogData]

  }
}
