package com.benkio

import cats.effect._
import scala.concurrent.ExecutionContext.Implicits.global
import com.benkio.telegramBotInfrastructure.Configurations

object main extends IOApp with Configurations {
  def run(args: List[String]): cats.effect.IO[cats.effect.ExitCode] =
    ABarberoBot
      .buildBot(global, (cb: ABarberoBot[IO]) => cb.start())
      .as(ExitCode.Success)
}
