package com.benkio.calandrobot

import cats.effect._

import scala.concurrent.ExecutionContext.Implicits.global

object CalandroBotMainPolling extends IOApp {
  def run(args: List[String]): cats.effect.IO[cats.effect.ExitCode] =
    CalandroBot
      .buildPollingBot(global, (cb: CalandroBotPolling[IO]) => cb.start())
      .as(ExitCode.Success)
}
