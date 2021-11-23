package com.benkio.xahbot

import cats.effect._

object XahBotMainPolling extends IOApp {
  def run(args: List[String]): IO[cats.effect.ExitCode] =
    XahBot
      .buildPollingBot[IO, Unit]((xl: XahBotPolling[IO]) => xl.start())
      .as(ExitCode.Success)
}
