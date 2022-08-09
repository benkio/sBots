package com.benkio.botDB

import cats.effect.IO
import doobie.Transactor
import org.testcontainers.utility.DockerImageName
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.benkio.botDB.TestData._
import com.dimafeng.testcontainers.munit.TestContainerForAll
import munit._
import cats.effect.unsafe.implicits.global

import doobie.implicits._

class ITSpec extends FunSuite with TestContainerForAll {
  override val containerDef: PostgreSQLContainer.Def = PostgreSQLContainer.Def(
    dockerImageName = DockerImageName.parse("postgres:alpine"),
    databaseName = config.dbName,
    username = config.user,
    password = config.password,
  )

  def setEnv(key: String, value: String) = {
    val field = System.getenv().getClass.getDeclaredField("m")
    field.setAccessible(true)
    val map = field.get(System.getenv()).asInstanceOf[java.util.Map[java.lang.String, java.lang.String]]
    map.put(key, value)
  }

  test("botDB main should populate the migration with the files in resources") {
    withContainers { postgresContainer: PostgreSQLContainer =>
      val testPort = postgresContainer.container.getMappedPort(config.port)
      println("url: " + postgresContainer.jdbcUrl)
      println("port: " + testPort)

      setEnv("DB_PORT", testPort.toString)
      setEnv("RESOURCE_LOCATION", "/testdata/")

      Main.run(List.empty).unsafeRunSync()

      val transactor = Transactor.fromDriverManager[IO](
        postgresContainer.driverClassName,
        postgresContainer.jdbcUrl,
        postgresContainer.username,
        postgresContainer.password
      )
      val mediaContent = sql"SELECT media_name FROM media;".query[String].to[List].transact(transactor).unsafeRunSync()
      assert(mediaContent.length == 3)
      assert(mediaContent.diff(List("media1.txt", "media2.txt", "media3.txt")).isEmpty)
    }
  }
}
