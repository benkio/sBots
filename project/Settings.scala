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
    scalaVersion             := "3.7.2",
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

  /** Shared settings for all bot projects. Pass the project name (e.g. "CalandroBot"). */
  def botProjectSettings(projectName: String): Seq[Def.SettingsDefinition] = Seq(
    name                     := projectName,
    libraryDependencies      := BotDependencies,
    dependencyOverrides      := BotDependencies,
    mainClass                := Some(s"com.benkio.$projectName.${projectName}MainPolling"),
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
