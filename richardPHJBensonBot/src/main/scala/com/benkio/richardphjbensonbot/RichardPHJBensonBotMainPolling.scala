package com.benkio.richardphjbensonbot

import cats.effect._

import scala.concurrent.ExecutionContext.Implicits.global

object RichardPHJBensonBotMainPolling extends IOApp {
  def run(args: List[String]): cats.effect.IO[cats.effect.ExitCode] =
    RichardPHJBensonBot
      .buildPollingBot(global, (rb: RichardPHJBensonBotPolling[IO]) => rb.start())
      .as(ExitCode.Success)
}
