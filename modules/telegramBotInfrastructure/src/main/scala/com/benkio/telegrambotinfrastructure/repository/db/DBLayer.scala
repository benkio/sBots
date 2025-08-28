package com.benkio.telegrambotinfrastructure.repository.db

import cats.effect.Async
import cats.implicits.*
import doobie.Transactor
import log.effect.LogWriter

final case class DBLayer[F[_]](
    dbTimeout: DBTimeout[F],
    dbMedia: DBMedia[F],
    dbSubscription: DBSubscription[F],
    dbLog: DBLog[F],
    dbShow: DBShow[F]
)

object DBLayer {

  def apply[F[_]: Async](
      transactor: Transactor[F]
  )(using log: LogWriter[F]): F[DBLayer[F]] = for {
    dbMedia <- DBMedia[F](transactor)
    dbSubscription = DBSubscription[F](transactor)
    dbTimeout      = new DBTimeout.DBTimeoutImpl[F](
      transactor,
      log
    )
    dbLog  = DBLog[F](transactor)
    dbShow = DBShow(transactor)
  } yield DBLayer[F](
    dbTimeout = dbTimeout,
    dbSubscription = dbSubscription,
    dbMedia = dbMedia,
    dbLog = dbLog,
    dbShow = dbShow
  )
}
