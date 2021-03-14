package com.benkio.telegramBotInfrastructure.botCapabilities

import org.scalatest._
import java.nio.file._
import scala.language.reflectiveCalls
import matchers.should._
import org.scalatest.wordspec.AnyWordSpec

class ResourcesAccessSpec extends AnyWordSpec with Matchers {

  val resourcesAccessFileSystem = ResourceAccess.fileSystem
  val rootPath                  = Paths.get("").toAbsolutePath()
  val testfile                  = "testFile"

  "ResourcesAccess - buildPath" should {
    "return the expected path" when {
      "the filename is provided" in {
        val result = resourcesAccessFileSystem.buildPath(testfile)
        result shouldEqual Paths.get(rootPath.toString, "src", "main", "resources", testfile)
      }
    }
  }
}
