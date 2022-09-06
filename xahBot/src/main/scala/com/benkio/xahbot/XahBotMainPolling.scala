package com.benkio.xahbot

import cats.effect._
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.{LogLevel, LogLevels, LogWriter}

object XahBotMainPolling extends IOApp {

  private def internalRun(logLevel: LogLevel): IO[ExitCode] = {
    implicit val log: LogWriter[IO] = consoleLogUpToLevel(logLevel)
    XahBot
      .buildPollingBot[IO, Unit]((xl: XahBotPolling[IO]) => xl.start())
      .as(ExitCode.Success)
  }

  def run(args: List[String]): IO[ExitCode] =
    internalRun(LogLevels.Info)
}
