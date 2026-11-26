package com.benkio.replieseditor.server.load

import munit.FunSuite

import java.nio.file.Files
import java.nio.file.Path

class RepoRootSpec extends FunSuite {

  private def mkRepoLike(dir: Path): Unit = {
    Files.createDirectories(dir.resolve("modules").resolve("bots"))
    Files.writeString(dir.resolve("build.sbt"), "// test\n")
    ()
  }

  test("detect uses sbots.repoRoot when set and valid") {
    val dir = Files.createTempDirectory("repo-root-test-").toAbsolutePath.normalize()
    mkRepoLike(dir)

    val old = Option(System.getProperty("sbots.repoRoot"))
    System.setProperty("sbots.repoRoot", dir.toString)
    try {
      val detected = RepoRoot.detect(start = dir.resolve("some").resolve("nested"))
      assertEquals(detected, dir)
    } finally {
      old.fold {
        System.clearProperty("sbots.repoRoot")
        ()
      } { value =>
        System.setProperty("sbots.repoRoot", value)
        ()
      }
    }
  }

  test("detect walks parents to find build.sbt + modules/bots") {
    val dir = Files.createTempDirectory("repo-root-walk-test-").toAbsolutePath.normalize()
    mkRepoLike(dir)
    val nested = Files.createDirectories(dir.resolve("a").resolve("b").resolve("c"))

    val old = Option(System.getProperty("sbots.repoRoot"))
    System.clearProperty("sbots.repoRoot")
    try {
      val detected = RepoRoot.detect(start = nested)
      assertEquals(detected, dir)
    } finally {
      old.fold {
        System.clearProperty("sbots.repoRoot")
        ()
      } { value =>
        System.setProperty("sbots.repoRoot", value)
        ()
      }
    }
  }
}
