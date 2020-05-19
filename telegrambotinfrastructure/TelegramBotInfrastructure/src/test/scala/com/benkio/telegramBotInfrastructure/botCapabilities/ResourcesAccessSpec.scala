package com.benkio.telegramBotInfrastructure.botCapabilities

import org.scalatest._

class ResourcesAccessSpec extends WordSpec with Matchers {

  val resourcesAccess = new ResourcesAccess {}

  "ResourcesAccess - buildPath" should {
    "return the expected path" when {
      "the filename is provided" in {
        val result = resourcesAccess.buildPath("testFile")
        println(s"result: $result")
      }
    }
  }
}
