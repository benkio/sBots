package com.benkio.chatcore.paths

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/** Explicit path configuration shared by runtime and tests.
  *
  * sbt injects `sbots.repoRoot` and `sbots.projectRoot` into every forked run and test (see project/Settings.scala), so
  * normal builds never depend on the launch directory. The parent-walk fallback exists only for direct JVM launches
  * where no property was injected. `sbots.logFile` is deliberately not defaulted: nothing is written to the working
  * directory unless the caller configures a file explicitly.
  */
object SBotPaths {

  val RepoRootProperty    = "sbots.repoRoot"
  val ProjectRootProperty = "sbots.projectRoot"
  val LogFileProperty     = "sbots.logFile"

  /** The JVM launch directory, used only as the starting point of the fallback detection. */
  def workingDirectory: Path = Paths.get(System.getProperty("user.dir")).toAbsolutePath.normalize()

  /** Repository root resolved from an explicit start path; an injected `sbots.repoRoot` always wins. */
  def repoRootFrom(start: Path): Path =
    configured(RepoRootProperty).getOrElse(detectRepoRoot(start))

  /** Repository root, injected by sbt; otherwise detected by walking parents of the working directory. */
  def repoRoot: Path = repoRootFrom(workingDirectory)

  /** Root of the current sbt project (module or bot). Defaults to the repository root when not injected. */
  def projectRoot: Path = configured(ProjectRootProperty).getOrElse(repoRoot)

  /** The fatal-log history file, only when explicitly configured. */
  def logFile: Option[Path] = configured(LogFileProperty)

  /** Resource directory of a stage inside a project (main resources by default). */
  def resourcesRoot(stage: String = "main", root: Path = projectRoot): Path =
    root.resolve("src").resolve(stage).resolve("resources")

  private def configured(name: String): Option[Path] =
    Option(System.getProperty(name))
      .filter(_.nonEmpty)
      .map(value => Paths.get(value).toAbsolutePath.normalize())

  private def detectRepoRoot(start: Path): Path = {
    def isRepoRoot(candidate: Path): Boolean =
      Files.isRegularFile(candidate.resolve("build.sbt")) &&
        Files.isDirectory(candidate.resolve("modules").resolve("bots"))

    Iterator
      .iterate(Option(start))(_.flatMap(p => Option(p.getParent)))
      .takeWhile(_.isDefined)
      .flatten
      .find(isRepoRoot)
      .getOrElse(start)
  }
}
