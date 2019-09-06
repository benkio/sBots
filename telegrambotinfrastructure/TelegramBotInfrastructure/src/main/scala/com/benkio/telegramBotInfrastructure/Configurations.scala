package com.benkio.telegramBotInfrastructure

import scala.io.Source

trait Configurations {

    // Configuration Stuff //////////////////////////////////////////////////////
  lazy val token = scala.util.Properties
    .envOrNone("BOT_TOKEN")
    .getOrElse(Source.fromResource("bot.token").getLines().mkString)

}
