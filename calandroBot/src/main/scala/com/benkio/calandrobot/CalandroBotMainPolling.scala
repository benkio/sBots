package com.benkio.calandrobot

import cats.effect._
import log.effect.LogWriter
import log.effect.fs2.SyncLogWriter.consoleLog

object CalandroBotMainPolling extends IOApp {

  implicit val log: LogWriter[IO] = consoleLog

  def run(args: List[String]): cats.effect.IO[cats.effect.ExitCode] =
    CalandroBot
      .buildPollingBot((cb: CalandroBotPolling[IO]) => cb.start())
      .as(ExitCode.Success)
}
