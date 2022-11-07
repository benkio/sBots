package com.benkio.telegrambotinfrastructure.resources.db

import cats.effect.Async
import cats.implicits._
import doobie.Transactor
import log.effect.LogWriter

final case class DBLayer[F[_]](dbTimeout: DBTimeout[F], dbMedia: DBMedia[F], dbSubscription: DBSubscription[F])

object DBLayer {

  def apply[F[_]: Async](
      transactor: Transactor[F]
  )(implicit log: LogWriter[F]): F[DBLayer[F]] = for {
    dbMedia <- DBMedia[F](transactor)
    dbSubscription = DBSubscription[F](transactor)
    dbTimeout = new DBTimeout.DBTimeoutImpl[F](
      transactor,
      log
    )
  } yield DBLayer[F](
    dbTimeout = dbTimeout,
    dbSubscription = dbSubscription,
    dbMedia = dbMedia
  )
}
