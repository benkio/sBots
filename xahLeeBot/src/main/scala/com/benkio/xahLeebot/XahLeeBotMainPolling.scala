package com.benkio.xahleebot

import cats.effect._
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevel
import log.effect.LogLevels
import log.effect.LogWriter

object XahLeeBotMainPolling extends IOApp {

  private def internalRun(logLevel: LogLevel): IO[ExitCode] = {
    implicit val log: LogWriter[IO] = consoleLogUpToLevel(logLevel)
    XahLeeBot
      .buildPollingBot[IO, Unit]((xl: XahLeeBotPolling[IO]) => xl.start())
      .as(ExitCode.Success)
  }

  def run(args: List[String]): IO[ExitCode] =
    internalRun(LogLevels.Info)
}
