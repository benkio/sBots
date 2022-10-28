package com.benkio.richardphjbensonbot.db

import cats.effect.Async
import cats.implicits._
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBResourceAccess
import com.benkio.telegrambotinfrastructure.web.UrlFetcher
import doobie.Transactor
import log.effect.LogWriter

final case class DBLayer[F[_]](dbResourceAccess: ResourceAccess[F], dbTimeout: DBTimeout[F])

object DBLayer {

  // FIX: Remove URL Fetcher dependency from here
  def apply[F[_]: Async](
      transactor: Transactor[F],
      urlFetcher: UrlFetcher[F]
  )(implicit log: LogWriter[F]): F[DBLayer[F]] = for {
    dbResourceAccess <- DBResourceAccess[F](transactor, urlFetcher)
    dbTimeout = new DBTimeout.DBTimeoutImpl[F](
      transactor,
      log
    )
  } yield DBLayer[F](
    dbResourceAccess = dbResourceAccess,
    dbTimeout = dbTimeout
  )
}
