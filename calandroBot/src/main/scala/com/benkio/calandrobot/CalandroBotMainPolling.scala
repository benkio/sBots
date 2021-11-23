package com.benkio.calandrobot

import cats.effect._

object CalandroBotMainPolling extends IOApp {
  def run(args: List[String]): cats.effect.IO[cats.effect.ExitCode] =
    CalandroBot
      .buildPollingBot((cb: CalandroBotPolling[IO]) => cb.start())
      .as(ExitCode.Success)
}
