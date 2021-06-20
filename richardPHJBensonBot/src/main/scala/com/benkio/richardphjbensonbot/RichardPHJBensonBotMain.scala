package com.benkio.richardphjbensonbot

import cats.effect._
import scala.concurrent.ExecutionContext.Implicits.global

object RichardPHJBensonBotMain extends IOApp {
  def run(args: List[String]): cats.effect.IO[cats.effect.ExitCode] =
    RichardPHJBensonBot
      .buildBot(global, (rb: RichardPHJBensonBot[IO]) => rb.start())
      .as(ExitCode.Success)
}
