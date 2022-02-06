package com.benkio.xahbot

import cats.effect._
import log.effect.LogWriter
import log.effect.fs2.SyncLogWriter.consoleLog

object XahBotMainPolling extends IOApp {

  implicit val log: LogWriter[IO] = consoleLog

  def run(args: List[String]): IO[cats.effect.ExitCode] =
    XahBot
      .buildPollingBot[IO, Unit]((xl: XahBotPolling[IO]) => xl.start())
      .as(ExitCode.Success)
}
