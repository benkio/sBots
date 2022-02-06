package com.benkio.abarberobot

import cats.effect._
import log.effect.LogWriter
import log.effect.fs2.SyncLogWriter.consoleLog

object ABarberoBotMainPolling extends IOApp {

  implicit val log: LogWriter[IO] = consoleLog

  def run(args: List[String]): IO[cats.effect.ExitCode] =
    ABarberoBot
      .buildPollingBot[IO, Unit]((ab: ABarberoBotPolling[IO]) => ab.start())
      .as(ExitCode.Success)
}
