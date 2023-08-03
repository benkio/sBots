package com.benkio.m0sconibot

import cats.effect._
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevel
import log.effect.LogLevels
import log.effect.LogWriter

object M0sconiBotMainPolling extends IOApp {

  private def internalRun(logLevel: LogLevel): IO[ExitCode] = {
    implicit val log: LogWriter[IO] = consoleLogUpToLevel(logLevel)
    M0sconiBot
      .buildPollingBot[IO, Unit]((ab: M0sconiBotPolling[IO]) => ab.start())
      .as(ExitCode.Success)
  }

  def run(args: List[String]): IO[ExitCode] =
    internalRun(LogLevels.Info)
}
