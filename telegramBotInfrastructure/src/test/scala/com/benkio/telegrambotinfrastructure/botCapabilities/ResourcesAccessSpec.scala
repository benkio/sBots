package com.benkio.telegrambotinfrastructure.botCapabilities

import org.scalatest._
import java.nio.file._
import scala.language.reflectiveCalls
import matchers.should._
import org.scalatest.wordspec.AnyWordSpec

class ResourcesAccessSpec extends AnyWordSpec with Matchers {

  val testSubmoduleDirectory    = "subModule"
  val testfile                  = "testFile"
  val resourcesAccessFileSystem = ResourceAccess.fileSystem(testSubmoduleDirectory)
  val rootPath                  = Paths.get(testSubmoduleDirectory).toAbsolutePath()

  "ResourcesAccess - buildPath" should {
    "return the expected path" when {
      "the filename is provided" in {
        val result = resourcesAccessFileSystem.buildPath(testfile)
        result shouldEqual Paths.get(rootPath.toString, "src", "main", "resources", testfile)
      }
    }
  }
}
