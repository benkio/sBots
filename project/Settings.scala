import sbt._

import Keys._
import sbtassembly.AssemblyPlugin.autoImport._
import Dependencies._

object Settings {

  lazy val settings = Seq(
    organization      := "com.benkio",
    version           := "1.0.0",
    scalaVersion      := "2.13.11",
    publishMavenStyle := true,
    semanticdbEnabled := true,
    semanticdbCompilerPlugin := {
      ("org.scalameta" % "semanticdb-scalac" % "4.7.8")
        .cross(CrossVersion.full)
    },
    fork := true,
    run / javaOptions += "-Xmx256m",
    Test / publishArtifact := false
  )

  lazy val assemblySettings = Seq(
    assembly / assemblyJarName := name.value + ".jar",
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

  lazy val XahBotSettings = Seq(
    name                     := "XahBot",
    libraryDependencies      := XahBotDependencies,
    dependencyOverrides      := XahBotDependencies,
    mainClass                := Some("com.benkio.xahbot.XahBotMainPolling"),
    Test / resourceDirectory := (Compile / resourceDirectory).value
  ) ++ assemblySettings

  lazy val YoutuboAncheIoBotSettings = Seq(
    name                     := "YoutuboAncheIoBot",
    libraryDependencies      := YoutuboAncheIoBotDependencies,
    dependencyOverrides      := YoutuboAncheIoBotDependencies,
    mainClass                := Some("com.benkio.youtuboAncheIobot.YoutuboAncheIoBotMainPolling"),
    Test / resourceDirectory := (Compile / resourceDirectory).value
  ) ++ assemblySettings

  lazy val MosconiBotSettings = Seq(
    name                     := "MosconiBot",
    libraryDependencies      := MosconiBotDependencies,
    dependencyOverrides      := MosconiBotDependencies,
    mainClass                := Some("com.benkio.Mosconibot.MosconiBotMainPolling"),
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
