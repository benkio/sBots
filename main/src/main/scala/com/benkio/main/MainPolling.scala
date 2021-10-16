package com.benkio.main

import cats.effect.IO
import cats.effect.IOApp
import com.benkio.abarberobot.ABarberoBotMain
import com.benkio.calandrobot.CalandroBotMain
import com.benkio.richardphjbensonbot.RichardPHJBensonBotMain
import com.benkio.xahbot.XahBotMain
import cats.effect.ExitCode

object MainPolling extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    ABarberoBotMain.run(args) &> CalandroBotMain.run(args) &> RichardPHJBensonBotMain.run(args) &> XahBotMain.run(args)
}
