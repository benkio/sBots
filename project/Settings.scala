import sbt._

import Keys._
import sbtassembly.AssemblyPlugin.autoImport._
import Dependencies._

object Settings {

  lazy val settings = Seq(
    organization      := "com.benkio",
    version           := "1.0.0",
    scalaVersion      := "2.13.8",
    publishMavenStyle := true,
    semanticdbEnabled := true,
    semanticdbCompilerPlugin := {
      ("org.scalameta" % "semanticdb-scalac" % "4.4.32")
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
      case x                             => MergeStrategy.first
    }
  )

  lazy val TelegramBotInfrastructureSettings = Seq(
    name                := "TelegramBotInfrastructure",
    libraryDependencies := TelegramBotInfrastructureDependencies
  )

  lazy val CalandroBotSettings = Seq(
    name                     := "CalandroBot",
    libraryDependencies      := CalandroBotDependencies,
    mainClass                := Some("com.benkio.calandrobot.CalandroBotMainPolling"),
    Test / resourceDirectory := (Compile / resourceDirectory).value
  ) ++ assemblySettings

  lazy val ABarberoBotSettings = Seq(
    name                     := "ABarberoBot",
    libraryDependencies      := ABarberoBotDependencies,
    mainClass                := Some("com.benkio.abarberobot.ABarberoBotMainPolling"),
    Test / resourceDirectory := (Compile / resourceDirectory).value
  ) ++ assemblySettings

  lazy val RichardPHJBensonBotSettings = Seq(
    name                     := "RichardPHJBensonBot",
    libraryDependencies      := RichardPHJBensonBotDependencies,
    mainClass                := Some("com.benkio.richardphjbensonbot.RichardPHJBensonBotMainPolling"),
    Test / resourceDirectory := (Compile / resourceDirectory).value
  ) ++ assemblySettings

  lazy val XahBotSettings = Seq(
    name                     := "XahBot",
    libraryDependencies      := XahBotDependencies,
    mainClass                := Some("com.benkio.xahbot.XahBotMainPolling"),
    Test / resourceDirectory := (Compile / resourceDirectory).value
  ) ++ assemblySettings

  lazy val MainSettings = Seq(
    name                := "main",
    libraryDependencies := MainDependencies
  ) ++ assemblySettings

  lazy val BotDBSettings = Seq(
    name                := "botDB",
    libraryDependencies := BotDBDependencies,
    mainClass           := Some("com.benkio.botDB.Main")
  ) ++ Defaults.itSettings
}
