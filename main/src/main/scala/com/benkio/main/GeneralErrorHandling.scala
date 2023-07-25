package com.benkio.main

import cats.ApplicativeError
import cats.effect.kernel.MonadCancel
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.resources.db.DBLog

object GeneralErrorHandling {

  def dbLogAndRestart[F[_], A](dbLog: DBLog[F], server: Resource[F, A])(implicit
      appErr: ApplicativeError[F, Throwable]
  ): Resource[F, A] =
    server.handleErrorWith((e: Throwable) => dbLogError(dbLog, e).flatMap(_ => dbLogAndRestart(dbLog, server)))

  def dbLogAndRestart(dbLog: DBLog[IO], app: IO[ExitCode])(implicit
      monCancel: MonadCancel[IO, Throwable]
  ): IO[ExitCode] =
    app.handleErrorWith((e: Throwable) => dbLogError[IO](dbLog, e).use_ *> dbLogAndRestart(dbLog, app))

  private def dbLogError[F[_]](dbLog: DBLog[F], e: Throwable): Resource[F, Unit] =
    Resource.eval(dbLog.writeLog(e.getMessage()))
}
