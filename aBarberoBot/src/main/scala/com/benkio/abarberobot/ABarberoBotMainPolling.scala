package com.benkio.abarberobot

import cats.effect._

object ABarberoBotMainPolling extends IOApp {
  def run(args: List[String]): IO[cats.effect.ExitCode] =
    ABarberoBot
      .buildPollingBot[IO, Unit]((ab: ABarberoBotPolling[IO]) => ab.start())
      .as(ExitCode.Success)
}
