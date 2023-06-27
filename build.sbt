import Settings._

name         := "telegramBots"
organization := "com.benkio"

enablePlugins(FlywayPlugin)
Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val runMigrate = taskKey[Unit]("Migrates the database schema.")

addCommandAlias("run-db-migrations", "runMigrate")
addCommandAlias("fix", ";scalafixAll; scalafmtAll; scalafmtSbt")
addCommandAlias("validate", ";clean; compile; fix; test; integration/test")

// PROJECTS

lazy val bots =
  Project("bots", file("."))
    .settings(Settings.settings: _*)
    .aggregate(
      main,
      botDB,
      telegramBotInfrastructure,
      calandroBot,
      aBarberoBot,
      richardPHJBensonBot,
      xahBot,
      youtuboAncheIoBot,
      mosconiBot
    )

lazy val telegramBotInfrastructure =
  Project("telegramBotInfrastructure", file("telegramBotInfrastructure"))
    .settings(Settings.settings: _*)
    .settings(Settings.TelegramBotInfrastructureSettings: _*)
    .disablePlugins(AssemblyPlugin)

lazy val calandroBot =
  Project("calandroBot", file("calandroBot"))
    .settings(Settings.settings: _*)
    .settings(Settings.CalandroBotSettings: _*)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val aBarberoBot =
  Project("aBarberoBot", file("aBarberoBot"))
    .settings(Settings.settings: _*)
    .settings(Settings.ABarberoBotSettings: _*)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val richardPHJBensonBot =
  Project("richardPHJBensonBot", file("richardPHJBensonBot"))
    .settings(Settings.settings: _*)
    .settings(Settings.RichardPHJBensonBotSettings: _*)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val xahBot =
  Project("xahBot", file("xahBot"))
    .settings(Settings.settings: _*)
    .settings(Settings.XahBotSettings: _*)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val youtuboAncheIoBot =
  Project("youtuboAncheIoBot", file("youtuboAncheIoBot"))
    .settings(Settings.settings: _*)
    .settings(Settings.YoutuboAncheIoBotSettings: _*)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val mosconiBot =
  Project("mosconiBot", file("mosconiBot"))
    .settings(Settings.settings: _*)
    .settings(Settings.MosconiBotSettings: _*)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val main = project
  .in(file("main"))
  .settings(Settings.settings: _*)
  .settings(Settings.MainSettings)
  .dependsOn(
    calandroBot,
    aBarberoBot,
    richardPHJBensonBot,
    xahBot,
    youtuboAncheIoBot,
    mosconiBot,
    telegramBotInfrastructure % "compile->compile;test->test"
  )

lazy val botDB =
  Project("botDB", file("botDB"))
    .settings(Settings.settings: _*)
    .settings(Settings.BotDBSettings)
    .settings(
      fullRunTask(runMigrate, Compile, "com.benkio.botDB.Main"),
      runMigrate / fork := true
    )
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val integration = (project in file("integration"))
  .dependsOn(
    telegramBotInfrastructure % "compile->compile;test->test",
    calandroBot,
    aBarberoBot,
    richardPHJBensonBot,
    xahBot,
    youtuboAncheIoBot,
    mosconiBot,
    botDB % "compile->compile;test->test",
    main
  )
  .settings(Settings.settings: _*)
  .settings(Settings.IntegrationSettings)
