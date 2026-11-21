package com.benkio.chatcore.paths

import munit.FunSuite

import java.nio.file.Files
import java.nio.file.Path

class SBotPathsSpec extends FunSuite {

  private val managedProperties: List[String] = List(
    SBotPaths.RepoRootProperty,
    SBotPaths.ProjectRootProperty,
    SBotPaths.LogFileProperty
  )

  private def withProperties(properties: Map[String, Option[String]])(body: => Unit): Unit = {
    val previous = managedProperties.map(name => name -> Option(System.getProperty(name))).toMap
    try {
      managedProperties.foreach { name =>
        System.clearProperty(name)
        ()
      }
      properties.foreach {
        case (name, Some(value)) => System.setProperty(name, value)
        case (name, None)        =>
          System.clearProperty(name)
          ()
      }
      body
    } finally {
      previous.foreach {
        case (name, Some(value)) => System.setProperty(name, value)
        case (name, None)        =>
          System.clearProperty(name)
          ()
      }
    }
  }

  test("repoRoot and projectRoot use the injected properties") {
    val root    = Files.createTempDirectory("sbots-repo-root-").toAbsolutePath.normalize()
    val project = root.resolve("modules").resolve("main")
    withProperties(
      Map(
        SBotPaths.RepoRootProperty    -> Some(root.toString),
        SBotPaths.ProjectRootProperty -> Some(project.toString)
      )
    ) {
      assertEquals(SBotPaths.repoRoot, root)
      assertEquals(SBotPaths.projectRoot, project)
      assertEquals(SBotPaths.resourcesRoot("test"), project.resolve("src").resolve("test").resolve("resources"))
    }
  }

  test("projectRoot falls back to repoRoot when not injected") {
    val root = Files.createTempDirectory("sbots-repo-root-").toAbsolutePath.normalize()
    withProperties(
      Map(
        SBotPaths.RepoRootProperty    -> Some(root.toString),
        SBotPaths.ProjectRootProperty -> None
      )
    ) {
      assertEquals(SBotPaths.projectRoot, root)
    }
  }

  test("logFile is present only when explicitly configured") {
    val history: Path = Files.createTempFile("sbots-history-", ".txt").toAbsolutePath.normalize()
    withProperties(Map(SBotPaths.LogFileProperty -> None)) {
      assertEquals(SBotPaths.logFile, None)
    }
    withProperties(Map(SBotPaths.LogFileProperty -> Some(history.toString))) {
      assertEquals(SBotPaths.logFile, Some(history))
    }
  }
}
