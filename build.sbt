import Dependencies.*
import Settings.*

// TASKS

lazy val runMigrate = taskKey[Unit]("Migrates the database schema.")

lazy val newBot = inputKey[Unit]("Create new bot from template: newBot <BotName> <id> (e.g. newBot MyNewBot mynew)")
newBot := {
  val args = sbt.complete.Parsers.spaceDelimited("<BotName> <id>").parsed
  if (args.size < 2) throw new sbt.MessageOnlyException("Usage: newBot <BotName> <id>")
  NewBotTask.createFromTemplate(baseDirectory.value, args(0), args(1))
}

// GLOBAL SETTINGS

name                     := "sBots"
organization             := "com.benkio"
ThisBuild / scalaVersion := "3.7.4"
ThisBuild / scalacOptions ++= Seq(
  "-java-output-version",
  "21",
  "-rewrite",
  "-no-indent"
)

ThisBuild / scalafixDependencies ++= Seq(
  "com.github.jatcwang" %% "scalafix-named-params" % "0.2.6",
  "org.typelevel"       %% "typelevel-scalafix"    % "0.5.0"
)

enablePlugins(FlywayPlugin)
enablePlugins(GitVersioning)
Global / onChangedBuildSource := ReloadOnSourceChanges

// SCoverage
coverageEnabled          := true
coverageFailOnMinimum    := true
coverageMinimumStmtTotal := 60 // TODO: INCREASE THIS

// COMMAND ALIASES

addCommandAlias("dbSetup", "runMigrate")
addCommandAlias("fix", ";scalafixAll; scalafmtAll; integration/scalafixAll; integration/scalafmtAll; scalafmtSbt")
addCommandAlias(
  "check",
  "undeclaredCompileDependenciesTest; scalafmtSbtCheck; scalafmtCheck; Test/scalafmtCheck"
)
addCommandAlias("generateTriggerTxt", "main/runMain com.benkio.main.GenerateTriggers")
addCommandAlias(
  "validate",
  ";clean; compile; fix; generateTriggerTxt; coverage; test; integration/mUnitTests; coverageAggregate"
)
addCommandAlias("compileAll", "compile; Test/compile; integration/Test/compile");
addCommandAlias("checkAllLinksTest", "integration/scalaTests")
addCommandAlias("integrationTests", "integration/mUnitTests")
// Data Entry Aliases
addCommandAlias("abarAddData", "ABarberoBot/runMain com.benkio.ABarberoBot.ABarberoBotMainDataEntry")
addCommandAlias("xahAddData", "XahLeeBot/runMain com.benkio.XahLeeBot.XahLeeBotMainDataEntry")
addCommandAlias("mosAddData", "M0sconiBot/runMain com.benkio.M0sconiBot.M0sconiBotMainDataEntry")
addCommandAlias("ytaiAddData", "YouTuboAncheI0Bot/runMain com.benkio.YouTuboAncheI0Bot.YouTuboAncheI0BotMainDataEntry")
addCommandAlias(
  "rphjbAddData",
  "RichardPHJBensonBot/runMain com.benkio.RichardPHJBensonBot.RichardPHJBensonBotMainDataEntry"
)

// PROJECTS

// Bot projects: when adding a bot, define the project below and add it here so aggregate and main.dependsOn include it.
lazy val botProjects: Seq[sbt.ProjectReference] = Seq(
  CalandroBot,
  ABarberoBot,
  RichardPHJBensonBot,
  XahLeeBot,
  YouTuboAncheI0Bot,
  M0sconiBot,
  VSgarbiBot
)

lazy val sBots =
  Project("sBots", file("."))
    .settings(Settings.settings *)
    .aggregate(main, botDB, telegramBotInfrastructure)
    .aggregate(botProjects *)

lazy val telegramBotInfrastructure =
  Project("telegramBotInfrastructure", file("modules/telegramBotInfrastructure"))
    .settings(Settings.settings *)
    .settings(Settings.TelegramBotInfrastructureSettings *)
    .disablePlugins(AssemblyPlugin)

lazy val CalandroBot =
  Project("CalandroBot", file("modules/bots/CalandroBot"))
    .settings(Settings.settings *)
    .settings(Settings.botProjectSettings("CalandroBot") *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val ABarberoBot =
  Project("ABarberoBot", file("modules/bots/ABarberoBot"))
    .settings(Settings.settings *)
    .settings(Settings.botProjectSettings("ABarberoBot") *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val RichardPHJBensonBot =
  Project("RichardPHJBensonBot", file("modules/bots/RichardPHJBensonBot"))
    .settings(Settings.settings *)
    .settings(Settings.botProjectSettings("RichardPHJBensonBot") *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val XahLeeBot =
  Project("XahLeeBot", file("modules/bots/XahLeeBot"))
    .settings(Settings.settings *)
    .settings(Settings.botProjectSettings("XahLeeBot") *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val YouTuboAncheI0Bot =
  Project("YouTuboAncheI0Bot", file("modules/bots/YouTuboAncheI0Bot"))
    .settings(Settings.settings *)
    .settings(Settings.botProjectSettings("YouTuboAncheI0Bot") *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val M0sconiBot =
  Project("M0sconiBot", file("modules/bots/M0sconiBot"))
    .settings(Settings.settings *)
    .settings(Settings.botProjectSettings("M0sconiBot") *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val VSgarbiBot =
Project("VSgarbiBot", file("modules/bots/VSgarbiBot"))
  .settings(Settings.settings *)
  .settings(Settings.botProjectSettings("VSgarbiBot") *)
  .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val main = project
  .in(file("modules/main"))
  .settings(Settings.settings *)
  .settings(Settings.MainSettings)
  .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")
  .dependsOn(botProjects.map(_ % "compile->compile;test->test"): _*)

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
    botDB                     % "compile->compile;test->test",
    main
  )
  .settings(Settings.settings *)
  .settings(Settings.IntegrationSettings)
