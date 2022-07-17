package com.benkio.botDB

import cats.effect.IO
import doobie.Transactor
import org.testcontainers.utility.DockerImageName
import com.dimafeng.testcontainers.MySQLContainer
import com.benkio.botDB.TestData._
import com.dimafeng.testcontainers.munit.TestContainerForAll
import munit._
import cats.effect.unsafe.implicits.global

import doobie.implicits._

class ITSpec extends FunSuite with TestContainerForAll {
  override val containerDef: MySQLContainer.Def = MySQLContainer.Def(
    dockerImageName = DockerImageName.parse(MySQLContainer.defaultDockerImageName),
    databaseName = config.dbName,
    username = config.user,
    password = config.password,
  )
  test("botDB main should populate the migration with the files in resources") {
    withContainers { mysqlContainer: MySQLContainer =>
      println("url: " + mysqlContainer.jdbcUrl)
      Main.run(List.empty).unsafeRunSync()
      val transactor = Transactor.fromDriverManager[IO](
        mysqlContainer.driverClassName,
        mysqlContainer.jdbcUrl,
        mysqlContainer.username,
        mysqlContainer.password
      )
      val mediaContent = sql"SELECT media_name FROM media;".query[String].to[List].transact(transactor).unsafeRunSync()
      assert(mediaContent.length == 3)
      assert(mediaContent.diff(List("media1.txt", "media2.txt", "media3.txt")).isEmpty)
    }
  }
}
