package com.benkio.abarberobot

import cats.effect._
import scala.concurrent.ExecutionContext.Implicits.global

object ABarberoBotMain extends IOApp {
  def run(args: List[String]): IO[cats.effect.ExitCode] =
    ABarberoBot
      .buildBot[IO, Unit](global, (ab: ABarberoBot[IO]) => ab.start())
      .as(ExitCode.Success)
}
