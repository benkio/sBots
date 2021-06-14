package com.benkio.richardphjbensonbot

import cats.effect._
import scala.concurrent.ExecutionContext.Implicits.global
import com.benkio.telegrambotinfrastructure.Configurations

object RichardPHJBensonBotMain extends IOApp with Configurations {
  def run(args: List[String]): cats.effect.IO[cats.effect.ExitCode] =
    RichardPHJBensonBot
      .buildBot(global, (cb: RichardPHJBensonBot[IO]) => cb.start())
      .as(ExitCode.Success)
}
