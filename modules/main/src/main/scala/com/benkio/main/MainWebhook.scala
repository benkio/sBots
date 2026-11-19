package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.benkio.chatcore.Logger.given
import com.benkio.main.runner.Runner

object MainWebhook extends IOApp {

  def run(args: List[String]): IO[ExitCode] = Runner.runWebhook(BotRegistry.value)
}
