package com.benkio.botDB

import org.testcontainers.containers.wait.strategy.Wait
import com.dimafeng.testcontainers.DockerComposeContainer
import com.dimafeng.testcontainers.ExposedService
import com.dimafeng.testcontainers.WaitingForService
import com.dimafeng.testcontainers.munit.TestContainerForAll
import java.io.File
import munit.Suite

trait ContainerSuite extends Suite with TestContainerForAll {

  val dbServiceName: String = "db_1"
  val dbServicePort: Int    = 5432
  val dbUser: String        = "botUser"
  val dbPassword: String    = "botPassword"
  val dbName: String        = "botDB"

  override val containerDef: DockerComposeContainer.Def = DockerComposeContainer.Def(
    new File("./docker-compose.yml"),
    exposedServices = Seq(ExposedService(dbServiceName, dbServicePort)),
    waitingFor = Some(WaitingForService("flyway_1", Wait.forLogMessage(".*Success.*\\n", 1)))
  )

}
