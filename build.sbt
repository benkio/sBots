import Settings._

// import Configs._

name         := "telegramBots"
organization := "com.benkio"

enablePlugins(FlywayPlugin)
Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

lazy val runMigrate = taskKey[Unit]("Migrates the database schema.")

addCommandAlias("run-db-migrations", "runMigrate")
addCommandAlias("fix", ";scalafixAll; scalafmtAll; scalafmtSbt")
addCommandAlias("validate", ";clean; compile; fix; test; IntegrationTest/test")

// PROJECTS

lazy val bots =
  Project("bots", file("."))
    .settings(Settings.settings: _*)
    .configs(IntegrationTest.extend(Test))
    .aggregate(
      main,
      botDB,
      telegramBotInfrastructure,
      calandroBot,
      aBarberoBot,
      richardPHJBensonBot,
      xahBot,
      youtuboAncheIoBot
    )

lazy val telegramBotInfrastructure =
  Project("telegramBotInfrastructure", file("telegramBotInfrastructure"))
    .settings(Settings.settings: _*)
    .settings(Settings.TelegramBotInfrastructureSettings: _*)
    .configs(IntegrationTest.extend(Test))
    .disablePlugins(AssemblyPlugin)

lazy val calandroBot =
  Project("calandroBot", file("calandroBot"))
    .settings(Settings.settings: _*)
    .settings(Settings.CalandroBotSettings: _*)
    .configs(IntegrationTest.extend(Test))
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val aBarberoBot =
  Project("aBarberoBot", file("aBarberoBot"))
    .settings(Settings.settings: _*)
    .settings(Settings.ABarberoBotSettings: _*)
    .configs(IntegrationTest.extend(Test))
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val richardPHJBensonBot =
  Project("richardPHJBensonBot", file("richardPHJBensonBot"))
    .settings(Settings.settings: _*)
    .settings(Settings.RichardPHJBensonBotSettings: _*)
    .configs(IntegrationTest.extend(Test))
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val xahBot =
  Project("xahBot", file("xahBot"))
    .settings(Settings.settings: _*)
    .settings(Settings.XahBotSettings: _*)
    .configs(IntegrationTest.extend(Test))
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val youtuboAncheIoBot =
  Project("youtuboAncheIoBot", file("youtuboAncheIoBot"))
    .settings(Settings.settings: _*)
    .settings(Settings.YoutuboAncheIoBotSettings: _*)
    .configs(IntegrationTest.extend(Test))
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val main = project
  .in(file("main"))
  .settings(Settings.settings: _*)
  .settings(Settings.MainSettings)
  .configs(IntegrationTest.extend(Test))
  .dependsOn(
    calandroBot,
    aBarberoBot,
    richardPHJBensonBot,
    xahBot,
    youtuboAncheIoBot,
    telegramBotInfrastructure % "compile->compile;test->test"
  )

lazy val botDB =
  Project("botDB", file("botDB"))
    .settings(Settings.settings: _*)
    .settings(Settings.BotDBSettings)
    .configs(IntegrationTest.extend(Test))
    .settings(
      fullRunTask(runMigrate, Compile, "com.benkio.botDB.Main"),
      runMigrate / fork := true
    )
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")
