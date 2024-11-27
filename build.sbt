import Settings.*

// GLOBAL SETTINGS

name         := "sBots"
organization := "com.benkio"

enablePlugins(FlywayPlugin)
Global / onChangedBuildSource := ReloadOnSourceChanges

// SCoverage
coverageEnabled               := true
coverageFailOnMinimum         := true
coverageMinimumStmtTotal      := 74 // TODO: INCREASE THIS

// TASKS

lazy val runMigrate = taskKey[Unit]("Migrates the database schema.")

// COMMAND ALIASES

addCommandAlias("dbSetup", "runMigrate")
addCommandAlias("fix", ";scalafixAll; scalafmtAll; scalafmtSbt")
addCommandAlias("check", "undeclaredCompileDependenciesTest; scalafmtSbtCheck; scalafmtCheck; Test/scalafmtCheck")
addCommandAlias("generateTriggerTxt", "main/runMain com.benkio.main.GenerateTriggers")
addCommandAlias("validate", ";clean; compile; fix; generateTriggerTxt; test; coverageAggregate; integration/runIntegrationMUnitTests")
addCommandAlias("checkAllLinksTest", "integration/runIntegrationScalaTests")

// PROJECTS

lazy val sBots =
  Project("sBots", file("."))
    .settings(Settings.settings *)
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
    .settings(Settings.settings *)
    .settings(Settings.TelegramBotInfrastructureSettings *)
    .disablePlugins(AssemblyPlugin)

lazy val calandroBot =
  Project("calandroBot", file("calandroBot"))
    .settings(Settings.settings *)
    .settings(Settings.CalandroBotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val aBarberoBot =
  Project("aBarberoBot", file("aBarberoBot"))
    .settings(Settings.settings *)
    .settings(Settings.ABarberoBotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val richardPHJBensonBot =
  Project("richardPHJBensonBot", file("richardPHJBensonBot"))
    .settings(Settings.settings *)
    .settings(Settings.RichardPHJBensonBotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val xahLeeBot =
  Project("xahLeeBot", file("xahLeeBot"))
    .settings(Settings.settings *)
    .settings(Settings.XahLeeBotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val youTuboAncheI0Bot =
  Project("youTuboAncheI0Bot", file("youTuboAncheI0Bot"))
    .settings(Settings.settings *)
    .settings(Settings.YouTuboAncheI0BotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val m0sconiBot =
  Project("m0sconiBot", file("m0sconiBot"))
    .settings(Settings.settings *)
    .settings(Settings.M0sconiBotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val main = project
  .in(file("main"))
  .settings(Settings.settings *)
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
    .settings(Settings.settings *)
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
  .settings(Settings.settings *)
  .settings(Settings.IntegrationSettings)
