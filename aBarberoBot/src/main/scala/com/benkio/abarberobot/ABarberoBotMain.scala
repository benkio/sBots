package com.benkio.abarberobot

import cats.effect._
import scala.concurrent.ExecutionContext.Implicits.global
import com.benkio.telegrambotinfrastructure.Configurations

object ABarberoBotMain extends IOApp with Configurations {
  def run(args: List[String]): IO[cats.effect.ExitCode] =
    ABarberoBot
      .buildBot[IO, Unit](global, (cb: ABarberoBot[IO]) => cb.start())
      .as(ExitCode.Success)
}
