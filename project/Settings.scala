import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*
import sbt.*
import sbtassembly.AssemblyPlugin.autoImport.*
import scoverage.ScoverageSbtPlugin.autoImport.*

import Dependencies.*
import Keys.*

object Settings {

  // TASKS
  lazy val mUnitTests = taskKey[Unit]("Run MUnit tests")
  lazy val runMigrate = taskKey[Unit]("Migrates the database schema.")
  lazy val scalaTests = taskKey[Unit]("Run ScalaTest tests")

  lazy val settings = Seq(
    organization             := "com.benkio",
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

  lazy val ChatCoreSettings = Seq(
    name                := "ChatCore",
    libraryDependencies := ChatCoreDependencies
  )

  lazy val ChatTelegramAdapterSettings = Seq(
    name                := "chatTelegramAdapter",
    libraryDependencies := ChatTelegramAdapterDependencies
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
    mainClass                := Some(s"com.benkio.$projectName.${projectName}MainPolling"),
    Test / resourceDirectory := (Compile / resourceDirectory).value
  ) ++ assemblySettings

  lazy val MainSettings = Seq(
    name                := "main",
    libraryDependencies := MainDependencies
  ) ++ assemblySettings

  lazy val BotDBSettings = Seq(
    name                := "botDB",
    libraryDependencies := BotDBDependencies,
    Test / javaOptions += s"-Dconfig.file=${sourceDirectory.value}/test/resources/application.test.conf",
    Test / fork       := true,
    runMigrate / fork := true
  ) ++ fullRunTask(runMigrate, Compile, "com.benkio.botDB.Main")

  def RepliesEditorServer(repliesEditorUI: Project) = Seq(
    name                := "repliesEditorServer",
    libraryDependencies := RepliesEditorServerDependencies,
    run / javaOptions += s"-Dsbots.repoRoot=${(ThisBuild / baseDirectory).value.getAbsolutePath}",
    Compile / resourceGenerators += Def.task {
      val _        = (repliesEditorUI / Compile / fastLinkJS).value // ensure UI is linked before copying
      val uiOutDir = (repliesEditorUI / Compile / fastLinkJS / scalaJSLinkerOutputDirectory).value
      val jsFiles  = (uiOutDir ** "*.js").get
      val uiJs     =
        jsFiles
          .find(_.getName == "main.js")
          .orElse(jsFiles.headOption)
          .getOrElse(sys.error(s"No linked JS file found under: $uiOutDir"))
      val targetDir = (Compile / resourceManaged).value / "public"
      val destJs    = targetDir / "app.js"
      IO.createDirectory(targetDir)
      IO.copyFile(uiJs, destJs)
      Seq(destJs)
    }.taskValue
  )

  lazy val RepliesEditorUI = Seq(
    name := "repliesEditorUI",
    libraryDependencies ++= RepliesEditorUiDependencies.value,
    scalaJSUseMainModuleInitializer := true,
    Test / fork                     := false,
    // scoverage + Scala.js currently crashes the Scala 3.7.2 JS backend (genSJSIR)
    coverageEnabled := false
  )
}
