import Settings.*

// GLOBAL SETTINGS

name         := "sBots"
organization := "com.benkio"

enablePlugins(FlywayPlugin)
Global / onChangedBuildSource := ReloadOnSourceChanges

// SCoverage
coverageEnabled          := true
coverageFailOnMinimum    := true
coverageMinimumStmtTotal := 84 // TODO: INCREASE THIS

// TASKS

lazy val runMigrate = taskKey[Unit]("Migrates the database schema.")

// COMMAND ALIASES

addCommandAlias("dbSetup", "runMigrate")
addCommandAlias("fix", ";scalafixAll; scalafmtAll; integration/scalafixAll; integration/scalafmtAll; scalafmtSbt")
addCommandAlias("check", "undeclaredCompileDependenciesTest; scalafmtSbtCheck; scalafmtCheck; Test/scalafmtCheck")
addCommandAlias("generateTriggerTxt", "main/runMain com.benkio.main.GenerateTriggers")
addCommandAlias(
  "validate",
  ";clean; compile; fix; generateTriggerTxt; coverage; test; integration/runIntegrationMUnitTests; coverageAggregate"
)
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
  Project("telegramBotInfrastructure", file("modules/telegramBotInfrastructure"))
    .settings(Settings.settings *)
    .settings(Settings.TelegramBotInfrastructureSettings *)
    .disablePlugins(AssemblyPlugin)

lazy val calandroBot =
  Project("calandroBot", file("modules/bots/calandroBot"))
    .settings(Settings.settings *)
    .settings(Settings.CalandroBotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val aBarberoBot =
  Project("aBarberoBot", file("modules/bots/aBarberoBot"))
    .settings(Settings.settings *)
    .settings(Settings.ABarberoBotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val richardPHJBensonBot =
  Project("richardPHJBensonBot", file("modules/bots/richardPHJBensonBot"))
    .settings(Settings.settings *)
    .settings(Settings.RichardPHJBensonBotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val xahLeeBot =
  Project("xahLeeBot", file("modules/bots/xahLeeBot"))
    .settings(Settings.settings *)
    .settings(Settings.XahLeeBotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val youTuboAncheI0Bot =
  Project("youTuboAncheI0Bot", file("modules/bots/youTuboAncheI0Bot"))
    .settings(Settings.settings *)
    .settings(Settings.YouTuboAncheI0BotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val m0sconiBot =
  Project("m0sconiBot", file("modules/bots/m0sconiBot"))
    .settings(Settings.settings *)
    .settings(Settings.M0sconiBotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val main = project
  .in(file("modules/main"))
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
  Project("botDB", file("modules/botDB"))
    .settings(Settings.settings *)
    .settings(Settings.BotDBSettings)
    .settings(
      fullRunTask(runMigrate, Compile, "com.benkio.botDB.Main"),
      runMigrate / fork := true
    )
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val integration = (project in file("modules/integration-tests"))
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
