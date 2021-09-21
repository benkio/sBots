package com.benkio.xahbot

import cats.effect._

import scala.concurrent.ExecutionContext.Implicits.global

object XahBotMain extends IOApp {
  def run(args: List[String]): IO[cats.effect.ExitCode] =
    XahBot
      .buildBot[IO, Unit](global, (xl: XahBot[IO]) => xl.start())
      .as(ExitCode.Success)
}
