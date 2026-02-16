import Dependencies.*
import JsonCheck.*
import Settings.*

// TASKS

lazy val runMigrate     = taskKey[Unit]("Migrates the database schema.")
lazy val checkJsonFiles = taskKey[Unit]("Checks if bot's JSON files are valid")
checkJsonFiles := JsonCheck.checkJsonFilesImpl.value

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
  "checkJsonFiles; undeclaredCompileDependenciesTest; scalafmtSbtCheck; scalafmtCheck; Test/scalafmtCheck"
)
addCommandAlias("generateTriggerTxt", "main/runMain com.benkio.main.GenerateTriggers")
addCommandAlias(
  "validate",
  ";clean; compile; checkJsonFiles; fix; generateTriggerTxt; coverage; test; integration/mUnitTests; coverageAggregate"
)
addCommandAlias("compileAll", "compile; Test/compile; integration/Test/compile");
addCommandAlias("checkAllLinksTest", "integration/scalaTests")
addCommandAlias("integrationTests", "integration/mUnitTests")
// Data Entry Aliases
addCommandAlias("abarAddData", "ABarberoBot/runMain com.benkio.ABarberoBot.ABarberoBotMainDataEntry")
addCommandAlias("xahAddData", "XahLeeBot/runMain com.benkio.XahLeeBot.XahLeeBotMainDataEntry")
addCommandAlias("mosAddData", "M0sconiBot/runMain com.benkio.M0sconiBot.M0sconiBotMainDataEntry")
addCommandAlias("ytaiAddData", "youTuboAncheI0Bot/runMain com.benkio.youtuboanchei0bot.YouTuboAncheI0BotMainDataEntry")
addCommandAlias(
  "rphjbAddData",
  "richardPHJBensonBot/runMain com.benkio.richardphjbensonbot.RichardPHJBensonBotMainDataEntry"
)

// PROJECTS

lazy val sBots =
  Project("sBots", file("."))
    .settings(Settings.settings *)
    .aggregate(
      main,
      botDB,
      telegramBotInfrastructure,
      CalandroBot,
      ABarberoBot,
      richardPHJBensonBot,
      XahLeeBot,
      youTuboAncheI0Bot,
      M0sconiBot
    )

lazy val telegramBotInfrastructure =
  Project("telegramBotInfrastructure", file("modules/telegramBotInfrastructure"))
    .settings(Settings.settings *)
    .settings(Settings.TelegramBotInfrastructureSettings *)
    .disablePlugins(AssemblyPlugin)

lazy val CalandroBot =
  Project("CalandroBot", file("modules/bots/CalandroBot"))
    .settings(Settings.settings *)
    .settings(Settings.CalandroBotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val ABarberoBot =
  Project("ABarberoBot", file("modules/bots/ABarberoBot"))
    .settings(Settings.settings *)
    .settings(Settings.ABarberoBotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val richardPHJBensonBot =
  Project("richardPHJBensonBot", file("modules/bots/richardPHJBensonBot"))
    .settings(Settings.settings *)
    .settings(Settings.RichardPHJBensonBotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val XahLeeBot =
  Project("XahLeeBot", file("modules/bots/XahLeeBot"))
    .settings(Settings.settings *)
    .settings(Settings.XahLeeBotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val youTuboAncheI0Bot =
  Project("youTuboAncheI0Bot", file("modules/bots/youTuboAncheI0Bot"))
    .settings(Settings.settings *)
    .settings(Settings.YouTuboAncheI0BotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val M0sconiBot =
  Project("M0sconiBot", file("modules/bots/M0sconiBot"))
    .settings(Settings.settings *)
    .settings(Settings.M0sconiBotSettings *)
    .dependsOn(telegramBotInfrastructure % "compile->compile;test->test")

lazy val main = project
  .in(file("modules/main"))
  .settings(Settings.settings *)
  .settings(Settings.MainSettings)
  .dependsOn(
    CalandroBot,
    ABarberoBot,
    richardPHJBensonBot,
    XahLeeBot,
    youTuboAncheI0Bot,
    M0sconiBot,
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
    CalandroBot,
    ABarberoBot,
    richardPHJBensonBot,
    XahLeeBot,
    youTuboAncheI0Bot,
    M0sconiBot,
    botDB % "compile->compile;test->test",
    main
  )
  .settings(Settings.settings *)
  .settings(Settings.IntegrationSettings)
