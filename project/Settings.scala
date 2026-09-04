import com.github.sbt.jacoco.report.JacocoReportSettings
import com.github.sbt.jacoco.report.JacocoThresholds
import com.github.sbt.jacoco.JacocoPlugin.autoImport.*
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.*
import sbt.*
import sbtassembly.AssemblyPlugin.autoImport.*

import Dependencies.*
import Keys.*

object Settings {

  // TASKS
  lazy val mUnitTests = taskKey[Unit]("Run MUnit tests")
  lazy val runMigrate = taskKey[Unit]("Migrates the database schema.")
  lazy val scalaTests = taskKey[Unit]("Run ScalaTest tests")

  /** Line-coverage threshold, rounded down from each module's current measured coverage (with a small safety margin) so
    * `jacoco`/`jacocoAggregate` reflect reality instead of an arbitrary one-size-fits-all number. Raise per-module as
    * coverage improves. Sets both `jacocoReportSettings` (used by `jacoco`/`jacocoReport`) and
    * `jacocoAggregateReportSettings` (used by `jacocoAggregate`'s per-project checks) — the latter otherwise silently
    * falls back to the root project's threshold.
    */
  def lineCoverageThreshold(pct: Double): Seq[Def.SettingsDefinition] = Seq(
    jacocoReportSettings          := JacocoReportSettings().withThresholds(JacocoThresholds(line = pct)),
    jacocoAggregateReportSettings := JacocoReportSettings().withThresholds(JacocoThresholds(line = pct))
  )

  lazy val settings = Seq(
    organization             := "com.benkio",
    publishMavenStyle        := true,
    semanticdbEnabled        := true,
    semanticdbCompilerPlugin := {
      ("org.scalameta" % "semanticdb-scalac" % "4.7.8")
        .cross(CrossVersion.full)
    },
    fork                   := true,
    Test / publishArtifact := false,
    jacocoExcludes         := Seq("com/benkio/chatcore/mocks/**")
  ) ++ lineCoverageThreshold(60)

  lazy val assemblySettings = Seq(
    assembly / assemblyJarName       := name.value + ".jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "services", _*)                     => MergeStrategy.concat
      case PathList("META-INF", "versions", _, "module-info.class") => MergeStrategy.discard
      case PathList("META-INF", "MANIFEST.MF")                      => MergeStrategy.discard
      case PathList("META-INF", xs @ _*)
          if xs.exists(_.endsWith(".SF")) || xs.exists(_.endsWith(".DSA")) || xs.exists(_.endsWith(".RSA")) =>
        MergeStrategy.discard
      case PathList("META-INF", xs @ _*) => MergeStrategy.first
      case "application.conf"            => MergeStrategy.concat
      case x                             => MergeStrategy.first
    }
  )

  lazy val ChatCoreSettings = Seq(
    name                := "ChatCore",
    libraryDependencies := ChatCoreDependencies
  ) ++ lineCoverageThreshold(55)

  lazy val ChatTelegramAdapterSettings = Seq(
    name                := "chatTelegramAdapter",
    libraryDependencies := ChatTelegramAdapterDependencies
  ) ++ lineCoverageThreshold(55)

  lazy val IntegrationSettings = Seq(
    name                := "Integration",
    libraryDependencies := IntegrationDependencies,
    publish / skip      := true,
    // These tests share a single botDB.sqlite3 file and in-memory caches; running them
    // concurrently races on that shared state and fails nondeterministically.
    Test / parallelExecution := false,
    // Constants.scala needs these paths (previously found via the now-unreliable
    // `getClass.getResource("/")`, see Constants.scala for why).
    Test / javaOptions += s"-Dsbots.repoRoot=${(ThisBuild / baseDirectory).value.getAbsolutePath}",
    Test / javaOptions += s"-Dsbots.integrationTestResourcesDirectory=${(Test / resourceDirectory).value.getAbsolutePath}/",
    mUnitTests := Def.uncached {
      (Test / testOnly).toTask(" com.benkio.integration.integrationmunit.*").value
    },
    scalaTests := Def.uncached {
      (Test / testOnly).toTask(" com.benkio.integration.integrationscalatest.*").value
    }
  )

  /** Shared settings for all bot projects. Pass the project name (e.g. "CalandroBot") and its current line-coverage
    * threshold (see `lineCoverageThreshold`).
    */
  def botProjectSettings(projectName: String, lineCoverage: Double = 10): Seq[Def.SettingsDefinition] = Seq(
    name                     := projectName,
    libraryDependencies      := BotDependencies,
    mainClass                := Some(s"com.benkio.$projectName.${projectName}MainPolling"),
    Test / resourceDirectory := (Compile / resourceDirectory).value
  ) ++ lineCoverageThreshold(lineCoverage) ++ assemblySettings

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

  def RepliesEditorServer(repliesEditorUI: ProjectReference) = Seq(
    name                := "repliesEditorServer",
    libraryDependencies := RepliesEditorServerDependencies,
    run / javaOptions += s"-Dsbots.repoRoot=${(ThisBuild / baseDirectory).value.getAbsolutePath}",
    Compile / resourceGenerators += Def.task {
      val _        = (repliesEditorUI / Compile / fastLinkJS).value // ensure UI is linked before copying
      val uiOutDir = (repliesEditorUI / Compile / fastLinkJS / scalaJSLinkerOutputDirectory).value
      val jsFiles  = (uiOutDir ** "*.js").get()
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
    Test / fork                     := false
    // Jacoco instruments JVM bytecode; Scala.js's linked JS output has none to measure.
  ) ++ lineCoverageThreshold(0)
}
