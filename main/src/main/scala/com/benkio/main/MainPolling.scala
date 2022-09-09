package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.benkio.abarberobot.ABarberoBotMainPolling
import com.benkio.calandrobot.CalandroBotMainPolling
import com.benkio.richardphjbensonbot.RichardPHJBensonBotMainPolling
import com.benkio.xahbot.XahBotMainPolling
import com.benkio.youtuboancheiobot.YoutuboAncheIoBotMainPolling

object MainPolling extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    ABarberoBotMainPolling.run(args) &> CalandroBotMainPolling.run(args) &> RichardPHJBensonBotMainPolling.run(
      args
    ) &> XahBotMainPolling.run(args) &> YoutuboAncheIoBotMainPolling.run(args)
}
