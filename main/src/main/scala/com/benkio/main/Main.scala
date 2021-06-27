package com.benkio.main

import cats.effect._
import com.benkio.abarberobot.ABarberoBotMain
import com.benkio.calandrobot.CalandroBotMain
import com.benkio.richardphjbensonbot.RichardPHJBensonBotMain

object Main extends IOApp {
  def run(args: List[String]): IO[cats.effect.ExitCode] =
    ABarberoBotMain.run(args) &> CalandroBotMain.run(args) &> RichardPHJBensonBotMain.run(args)
}
