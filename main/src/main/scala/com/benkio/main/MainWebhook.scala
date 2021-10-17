package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp

object MainWebhook extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    IO.pure(ExitCode.Success)

// WebhookBot.compose[IO](
//     List(bot1, bot2),
//     8080,
//     ExecutionContext.global, //optional, global as default
//     "127.0.0.1" //optional, localhost as default
//   ).useForever
}
