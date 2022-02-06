package com.benkio.richardphjbensonbot

import cats.effect._
import log.effect.LogWriter
import log.effect.fs2.SyncLogWriter.consoleLog

object RichardPHJBensonBotMainPolling extends IOApp {

  implicit val log: LogWriter[IO] = consoleLog

  def run(args: List[String]): cats.effect.IO[cats.effect.ExitCode] =
    RichardPHJBensonBot
      .buildPollingBot((rb: RichardPHJBensonBotPolling[IO]) => rb.start())
      .as(ExitCode.Success)
}
