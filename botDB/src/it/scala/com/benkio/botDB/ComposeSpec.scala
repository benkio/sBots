package com.benkio.botDB

import munit._

class ComposeSpec extends FunSuite with ContainerSuite {

  test("docker-compose file should run and working") {
    withContainers { dockerComposeContainer =>
      assert(dockerComposeContainer.getServicePort(dbServiceName, dbServicePort) > 0)
    }
  }
}
