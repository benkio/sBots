package com.benkio.telegramBotInfrastructure.botCapabilities

import org.scalatest._
import java.nio.file._

class ResourcesAccessSpec extends WordSpec with Matchers {

  val resourcesAccess = new ResourcesAccess {}
  val rootPath = Paths.get("").toAbsolutePath()
  val testfile = "testFile"

  "ResourcesAccess - buildPath" should {
    "return the expected path" when {
      "the filename is provided" in {
        val result = resourcesAccess.buildPath(testfile)
        result.toString shouldEqual (rootPath + "/src/main/resources/" + testfile)
      }
    }
  }
}
