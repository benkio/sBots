package com.benkio.calandrobot

import cats.effect._
import scala.concurrent.ExecutionContext.Implicits.global

object CalandroBotMain extends IOApp {
  def run(args: List[String]): cats.effect.IO[cats.effect.ExitCode] =
    CalandroBot
      .buildBot(global, (cb: CalandroBot[IO]) => cb.start())
      .as(ExitCode.Success)
}
