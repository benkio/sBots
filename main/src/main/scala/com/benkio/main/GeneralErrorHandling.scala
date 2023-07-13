package com.benkio.main

import cats.ApplicativeError
import cats.effect.kernel.MonadCancel
import cats.effect.{ExitCode, IO, Resource}

object GeneralErrorHandling {

  def dbLogAndRestart[F[_], A](server: Resource[F, A])(implicit appErr: ApplicativeError[F,Throwable]): Resource[F, A] =
    server.handleErrorWith((e : Throwable) => dbLogError(e).flatMap(_ => dbLogAndRestart(server)))

  def dbLogAndRestart(app: IO[ExitCode])(implicit monCancel: MonadCancel[IO,Throwable]): IO[ExitCode] =
    app.handleErrorWith((e: Throwable) => dbLogError[IO](e).use_ *> app)

  private def dbLogError[F[_]](e: Throwable) : Resource[F, Unit] = ???
}
