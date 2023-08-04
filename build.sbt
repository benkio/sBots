import Settings._

name         := "sBots"
organization := "com.benkio"

enablePlugins(FlywayPlugin)
Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val runMigrate = taskKey[Unit]("Migrates the database schema.")

addCommandAlias("run-db-migrations", "runMigrate")
addCommandAlias("fix", ";scalafixAll; scalafmtAll; scalafmtSbt")
addCommandAlias("validate", ";clean; compile; fix; test; integration/test")

// PROJECTS

lazy val sBots =
  Project("sBots", file("."))
    .settings(Settings.settings: _*)
    .aggregate(
      main,
      botDB,
      telegramBotInfrastructure,
      calandroBot,
      aBarberoBot,
      richardPHJBensonBot,
      xahLeeBot,
      youTuboAncheI0Bot,
      m0sconiBot
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

lazy val xahLeeBot =
  Project("xahLeeBot", file("xahLeeBot"))
    .settings(Settings.settings: _*)
    .settings(Settings.XahLeeBotSettings: _*)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val youTuboAncheI0Bot =
  Project("youTuboAncheI0Bot", file("youTuboAncheI0Bot"))
    .settings(Settings.settings: _*)
    .settings(Settings.YouTuboAncheI0BotSettings: _*)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val m0sconiBot =
  Project("m0sconiBot", file("m0sconiBot"))
    .settings(Settings.settings: _*)
    .settings(Settings.M0sconiBotSettings: _*)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val main = project
  .in(file("main"))
  .settings(Settings.settings: _*)
  .settings(Settings.MainSettings)
  .dependsOn(
    calandroBot,
    aBarberoBot,
    richardPHJBensonBot,
    xahLeeBot,
    youTuboAncheI0Bot,
    m0sconiBot,
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
    xahLeeBot,
    youTuboAncheI0Bot,
    m0sconiBot,
    botDB % "compile->compile;test->test",
    main
  )
  .settings(Settings.settings: _*)
  .settings(Settings.IntegrationSettings)
