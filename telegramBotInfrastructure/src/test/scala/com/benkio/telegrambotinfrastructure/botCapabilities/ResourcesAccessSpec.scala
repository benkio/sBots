package com.benkio.telegrambotinfrastructure.botCapabilities

import munit.FunSuite

import java.nio.file._
import scala.language.reflectiveCalls

class ResourcesAccessSpec extends FunSuite {

  val testfile                  = "testFile"
  val resourcesAccessFileSystem = ResourceAccess.fileSystem
  val rootPath                  = Paths.get("").toAbsolutePath()

  test("ResourceAccess - buildPath should return the expected path when the filename is provided") {
    val result = resourcesAccessFileSystem.buildPath(testfile)
    assertEquals(result, Paths.get(rootPath.toString, "src", "main", "resources", testfile))
  }
}
