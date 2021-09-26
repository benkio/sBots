import Settings._

name         := "telegramBots"
organization := "com.benkio"
scalaVersion := "2.13.6"

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"
ThisBuild / scalacOptions += "-P:semanticdb:synthetics:on"

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
    .dependsOn(telegramBotInfrastructure)

lazy val aBarberoBot =
  Project("aBarberoBot", file("aBarberoBot"))
    .settings(Settings.settings: _*)
    .settings(Settings.ABarberoBotSettings: _*)
    .dependsOn(telegramBotInfrastructure)

lazy val richardPHJBensonBot =
  Project("richardPHJBensonBot", file("richardPHJBensonBot"))
    .settings(Settings.settings: _*)
    .settings(Settings.RichardPHJBensonBotSettings: _*)
    .dependsOn(telegramBotInfrastructure)

lazy val xahBot =
  Project("xahBot", file("xahBot"))
    .settings(Settings.settings: _*)
    .settings(Settings.XahBotSettings: _*)
    .dependsOn(telegramBotInfrastructure)

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
