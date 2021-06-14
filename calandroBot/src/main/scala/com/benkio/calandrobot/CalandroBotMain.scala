package com.benkio.calandrobot

import cats.effect._
import scala.concurrent.ExecutionContext.Implicits.global
import com.benkio.telegrambotinfrastructure.Configurations

object CalandroBotMain extends IOApp with Configurations {
  def run(args: List[String]): cats.effect.IO[cats.effect.ExitCode] =
    CalandroBot
      .buildBot(global, (cb: CalandroBot[IO]) => cb.start())
      .as(ExitCode.Success)
}
