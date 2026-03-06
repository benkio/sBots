// -----------------------------------------------------------------------------
// Modifications to this file must be done carefully. The new-bot process
// (CompleteBotRegistration.sc) adds a project def, appends to botProjects, and
// adds data-entry aliases. When editing, keep the same structure so adding
// future bots continues to work. See docs/adding-a-bot.md.
// -----------------------------------------------------------------------------

import Dependencies.*
import Settings.*

lazy val newBot = inputKey[Unit]("Create new bot from template: newBot <BotName> <id> (e.g. newBot MyNewBot mynew)")
newBot := {
  val args = sbt.complete.Parsers.spaceDelimited("<BotName> <id>").parsed
  if (args.size < 2) throw new sbt.MessageOnlyException("Usage: newBot <BotName> <id>")
  NewBotTask.createFromTemplate(baseDirectory.value, args(0), args(1))
}

// GLOBAL SETTINGS

name                     := "sBots"
organization             := "com.benkio"
ThisBuild / scalaVersion := "3.3.7"
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
coverageMinimumStmtTotal := 65 // TODO: INCREASE THIS

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
  ";clean; compile; fix; generateTriggerTxt; coverage; test; integration/mUnitTests; coverageAggregate; assembly"
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
addCommandAlias("pinoAddData", "PinoScottoBot/runMain com.benkio.PinoScottoBot.PinoScottoBotMainDataEntry")

// PROJECTS

// Bot projects: when adding a bot, define the project below and add it here so aggregate and main.dependsOn include it.
lazy val botProjects: Seq[sbt.ProjectReference] = Seq(
  CalandroBot,
  ABarberoBot,
  RichardPHJBensonBot,
  XahLeeBot,
  YouTuboAncheI0Bot,
  M0sconiBot,
  PinoScottoBot
)

lazy val sBots =
  Project("sBots", file("."))
    .settings(Settings.settings *)
    .aggregate(main, botDB, chatCore, chatTelegramAdapter, repliesEditorServer, repliesEditorUI)
    .aggregate(botProjects *)

lazy val chatCore =
  Project("chatCore", file("modules/chatCore"))
    .settings(Settings.settings *)
    .settings(Settings.ChatCoreSettings *)
    .disablePlugins(AssemblyPlugin)

lazy val chatTelegramAdapter =
  Project("chatTelegramAdapter", file("modules/chatTelegramAdapter"))
    .settings(Settings.settings *)
    .settings(Settings.ChatTelegramAdapterSettings *)
    .dependsOn(chatCore % "compile->compile;test->test")
    .disablePlugins(AssemblyPlugin)

lazy val CalandroBot =
  Project("CalandroBot", file("modules/bots/CalandroBot"))
    .settings(Settings.settings *)
    .settings(Settings.botProjectSettings("CalandroBot") *)
    .dependsOn(chatCore % "compile->compile;test->test", chatTelegramAdapter % "compile->compile;test->test")

lazy val ABarberoBot =
  Project("ABarberoBot", file("modules/bots/ABarberoBot"))
    .settings(Settings.settings *)
    .settings(Settings.botProjectSettings("ABarberoBot") *)
    .dependsOn(chatCore % "compile->compile;test->test", chatTelegramAdapter % "compile->compile;test->test")

lazy val RichardPHJBensonBot =
  Project("RichardPHJBensonBot", file("modules/bots/RichardPHJBensonBot"))
    .settings(Settings.settings *)
    .settings(Settings.botProjectSettings("RichardPHJBensonBot") *)
    .dependsOn(chatCore % "compile->compile;test->test", chatTelegramAdapter % "compile->compile;test->test")

lazy val XahLeeBot =
  Project("XahLeeBot", file("modules/bots/XahLeeBot"))
    .settings(Settings.settings *)
    .settings(Settings.botProjectSettings("XahLeeBot") *)
    .dependsOn(chatCore % "compile->compile;test->test", chatTelegramAdapter % "compile->compile;test->test")

lazy val YouTuboAncheI0Bot =
  Project("YouTuboAncheI0Bot", file("modules/bots/YouTuboAncheI0Bot"))
    .settings(Settings.settings *)
    .settings(Settings.botProjectSettings("YouTuboAncheI0Bot") *)
    .dependsOn(chatCore % "compile->compile;test->test", chatTelegramAdapter % "compile->compile;test->test")

lazy val M0sconiBot =
  Project("M0sconiBot", file("modules/bots/M0sconiBot"))
    .settings(Settings.settings *)
    .settings(Settings.botProjectSettings("M0sconiBot") *)
    .dependsOn(chatCore % "compile->compile;test->test", chatTelegramAdapter % "compile->compile;test->test")

lazy val PinoScottoBot =
  Project("PinoScottoBot", file("modules/bots/PinoScottoBot"))
    .settings(Settings.settings *)
    .settings(Settings.botProjectSettings("PinoScottoBot") *)
    .dependsOn(chatCore % "compile->compile;test->test", chatTelegramAdapter % "compile->compile;test->test")

lazy val main = project
  .in(file("modules/main"))
  .settings(Settings.settings *)
  .settings(Settings.MainSettings)
  .dependsOn(chatCore % "compile->compile;test->test", chatTelegramAdapter % "compile->compile;test->test")
  .dependsOn(botProjects.map(_ % "compile->compile;test->test"): _*)

lazy val botDB =
  Project("botDB", file("modules/botDB"))
    .disablePlugins(sbtassembly.AssemblyPlugin)
    .settings(Settings.settings *)
    .settings(Settings.BotDBSettings)
    .dependsOn(chatCore % "compile->compile;test->test")

lazy val repliesEditorUI =
  Project("repliesEditorUI", file("modules/repliesEditorUI"))
    .disablePlugins(sbtassembly.AssemblyPlugin)
    .enablePlugins(ScalaJSPlugin)
    .settings(Settings.settings *)
    .settings(
      Settings.RepliesEditorUI
    )

lazy val repliesEditorServer =
  Project("repliesEditorServer", file("modules/repliesEditorServer"))
    .disablePlugins(sbtassembly.AssemblyPlugin)
    .settings(Settings.settings *)
    .settings(
      Settings.RepliesEditorServer(repliesEditorUI)
    )
    .dependsOn(chatCore % "compile->compile;test->test")

lazy val integration = (project in file("modules/integration-tests"))
  .dependsOn(
    chatCore            % "compile->compile;test->test",
    chatTelegramAdapter % "compile->compile;test->test",
    botDB               % "compile->compile;test->test",
    main
  )
  .settings(Settings.settings *)
  .settings(Settings.IntegrationSettings)
