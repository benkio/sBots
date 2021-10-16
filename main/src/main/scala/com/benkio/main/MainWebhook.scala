package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp

object MainWebhook extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    IO.pure(ExitCode.Success)
}
