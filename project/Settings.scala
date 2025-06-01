import sbt.*
import sbtassembly.AssemblyPlugin.autoImport.*

import Dependencies.*
import Keys.*

object Settings {

  lazy val mUnitTests = taskKey[Unit]("Run MUnit tests")

  lazy val scalaTests = taskKey[Unit]("Run ScalaTest tests")

  lazy val settings = Seq(
    organization             := "com.benkio",
    version                  := "2.3.0",
    scalaVersion             := "3.7.0",
    publishMavenStyle        := true,
    semanticdbEnabled        := true,
    semanticdbCompilerPlugin := {
      ("org.scalameta" % "semanticdb-scalac" % "4.7.8")
        .cross(CrossVersion.full)
    },
    fork                   := true,
    Test / publishArtifact := false
  )

  lazy val assemblySettings = Seq(
    assembly / assemblyJarName       := name.value + ".jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case "application.conf"            => MergeStrategy.concat
      case x                             => MergeStrategy.first
    }
  )

  lazy val TelegramBotInfrastructureSettings = Seq(
    name                := "TelegramBotInfrastructure",
    libraryDependencies := TelegramBotInfrastructureDependencies,
    dependencyOverrides := TelegramBotInfrastructureDependencies
  )

  lazy val IntegrationSettings = Seq(
    name                := "Integration",
    libraryDependencies := IntegrationDependencies,
    publish / skip      := true,
    mUnitTests          := {
      (Test / testOnly).toTask(" com.benkio.integration.integrationmunit.*").value
    },
    scalaTests := {
      (Test / testOnly).toTask(" com.benkio.integration.integrationscalatest.*").value
    }
  )

  lazy val CalandroBotSettings = Seq(
    name                     := "CalandroBot",
    libraryDependencies      := CalandroBotDependencies,
    dependencyOverrides      := CalandroBotDependencies,
    mainClass                := Some("com.benkio.calandrobot.CalandroBotMainPolling"),
    Test / resourceDirectory := (Compile / resourceDirectory).value
  ) ++ assemblySettings

  lazy val ABarberoBotSettings = Seq(
    name                     := "ABarberoBot",
    libraryDependencies      := ABarberoBotDependencies,
    dependencyOverrides      := ABarberoBotDependencies,
    mainClass                := Some("com.benkio.abarberobot.ABarberoBotMainPolling"),
    Test / resourceDirectory := (Compile / resourceDirectory).value
  ) ++ assemblySettings

  lazy val RichardPHJBensonBotSettings = Seq(
    name                     := "RichardPHJBensonBot",
    libraryDependencies      := RichardPHJBensonBotDependencies,
    dependencyOverrides      := RichardPHJBensonBotDependencies,
    mainClass                := Some("com.benkio.richardphjbensonbot.RichardPHJBensonBotMainPolling"),
    Test / resourceDirectory := (Compile / resourceDirectory).value
  ) ++ assemblySettings

  lazy val XahLeeBotSettings = Seq(
    name                     := "XahLeeBot",
    libraryDependencies      := XahLeeBotDependencies,
    dependencyOverrides      := XahLeeBotDependencies,
    mainClass                := Some("com.benkio.xahleebot.XahLeeBotMainPolling"),
    Test / resourceDirectory := (Compile / resourceDirectory).value
  ) ++ assemblySettings

  lazy val YouTuboAncheI0BotSettings = Seq(
    name                     := "YouTuboAncheI0Bot",
    libraryDependencies      := YouTuboAncheI0BotDependencies,
    dependencyOverrides      := YouTuboAncheI0BotDependencies,
    mainClass                := Some("com.benkio.youtuboanchei0bot.YouTuboAncheI0BotMainPolling"),
    Test / resourceDirectory := (Compile / resourceDirectory).value
  ) ++ assemblySettings

  lazy val M0sconiBotSettings = Seq(
    name                     := "M0sconiBot",
    libraryDependencies      := M0sconiBotDependencies,
    dependencyOverrides      := M0sconiBotDependencies,
    mainClass                := Some("com.benkio.M0sconibot.M0sconiBotMainPolling"),
    Test / resourceDirectory := (Compile / resourceDirectory).value
  ) ++ assemblySettings

  lazy val MainSettings = Seq(
    name                := "main",
    libraryDependencies := MainDependencies,
    dependencyOverrides := MainDependencies
  ) ++ assemblySettings

  lazy val BotDBSettings = Seq(
    name                := "botDB",
    libraryDependencies := BotDBDependencies,
    dependencyOverrides := BotDBDependencies,
    mainClass           := Some("com.benkio.botDB.Main"),
    Test / javaOptions += s"-Dconfig.file=${sourceDirectory.value}/test/resources/application.test.conf",
    Test / fork := true
  ) ++ assemblySettings
}
