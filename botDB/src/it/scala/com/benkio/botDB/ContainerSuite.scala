package com.benkio.botDB

import com.dimafeng.testcontainers.{DockerComposeContainer, ExposedService}
import com.dimafeng.testcontainers.munit.TestContainerForAll
import java.io.File
import munit.Suite

trait ContainerSuite extends Suite with TestContainerForAll {

  val dbServiceName: String = "db_1"
  val dbServicePort: Int    = 3306

  override val containerDef: DockerComposeContainer.Def = DockerComposeContainer.Def(new File("./docker-compose.yml"), exposedServices = Seq(ExposedService(dbServiceName, dbServicePort)))

}
