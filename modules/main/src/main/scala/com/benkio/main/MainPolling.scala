package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.benkio.main.runner.Runner
import com.benkio.main.Logger.given

object MainPolling extends IOApp {

  def run(args: List[String]): IO[ExitCode] = Runner.runPolling(BotRegistry.value)
}
