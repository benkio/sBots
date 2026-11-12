package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.benkio.main.runner.Runner
import com.benkio.main.Logger.given

/** Runs the bots asking Telegram for their messages. All of them, or only the ones named in the arguments (the id or
  * the name of the bot): `MainPolling rphjb XahLeeBot`.
  */
object MainPolling extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    (if args.isEmpty then Right(BotRegistry.value) else BotRegistry.value.select(args)).fold(
      error => IO.println(error).as(ExitCode.Error),
      Runner.runPolling
    )
}
