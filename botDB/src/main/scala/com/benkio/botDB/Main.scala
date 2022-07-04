package com.benkio.botDB

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {

  val run = Config.loadConfig.flatMap(config => IO(println(config)))

}
