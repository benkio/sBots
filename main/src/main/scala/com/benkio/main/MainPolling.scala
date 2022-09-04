package com.benkio.main

import cats.effect.{ExitCode, IO, IOApp}
import com.benkio.abarberobot.ABarberoBotMainPolling
import com.benkio.calandrobot.CalandroBotMainPolling
import com.benkio.richardphjbensonbot.RichardPHJBensonBotMainPolling
import com.benkio.xahbot.XahBotMainPolling

object MainPolling extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    ABarberoBotMainPolling.run(args) &> CalandroBotMainPolling.run(args) &> RichardPHJBensonBotMainPolling.run(
      args
    ) &> XahBotMainPolling.run(args)
}
