package com.benkio.telegrambotinfrastructure.botCapabilities

import org.scalatest._
import org.scalatest.wordspec.AnyWordSpec

import java.nio.file._
import scala.language.reflectiveCalls

import matchers.should._

class ResourcesAccessSpec extends AnyWordSpec with Matchers {

  val testfile                  = "testFile"
  val resourcesAccessFileSystem = ResourceAccess.fileSystem
  val rootPath                  = Paths.get("").toAbsolutePath()

  "ResourcesAccess - buildPath" should {
    "return the expected path" when {
      "the filename is provided" in {
        val result = resourcesAccessFileSystem.buildPath(testfile)
        result shouldEqual Paths.get(rootPath.toString, "src", "main", "resources", testfile)
      }
    }
  }
}
