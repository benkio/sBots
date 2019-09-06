import Dependencies._
import Settings._

lazy val TelegramBotInfrastructure = (project in file("TelegramBotInfrastructure")).
  settings(Settings.settings: _*).
  settings(Settings.TelegramBotInfrastructureSettings: _*).
  configs(Test)
