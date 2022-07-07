import Settings._

name         := "telegramBots"
organization := "com.benkio"
scalaVersion := "2.13.8"

enablePlugins(FlywayPlugin)
Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

lazy val runMigrate = taskKey[Unit]("Migrates the database schema.")

addCommandAlias("run-db-migrations", "runMigrate")
addCommandAlias("fix", ";scalafixAll; scalafmtAll; scalafmtSbt")

// PROJECTS

lazy val global = project
  .in(file("."))
  .settings(Settings.settings: _*)
  .aggregate(
    main,
    telegramBotInfrastructure,
    calandroBot,
    aBarberoBot,
    richardPHJBensonBot,
    xahBot
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

lazy val main = project
  .in(file("main"))
  .settings(Settings.settings: _*)
  .settings(Settings.MainSettings)
  .dependsOn(
    calandroBot,
    aBarberoBot,
    richardPHJBensonBot,
    xahBot
  )

lazy val BotDB =
  Project("botDB", file("botDB"))
    .settings(Settings.settings: _*)
    .settings(Settings.BotDBSettings)
    .settings(
      fullRunTask(runMigrate, Compile, "com.benkio.botDB.Main"),
      runMigrate / fork := true
    )
