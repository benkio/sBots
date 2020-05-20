import Settings._

lazy val root = (project in file("."))
  .settings(Settings.settings: _*)
  .settings(Settings.TelegramBotInfrastructureSettings: _*)
  .configs(Test)
