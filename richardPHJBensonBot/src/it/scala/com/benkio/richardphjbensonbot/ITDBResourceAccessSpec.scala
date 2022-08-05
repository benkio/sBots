package com.benkio.richardphjbensonbot

import doobie.Transactor
import cats.effect.Resource
import log.effect.fs2.SyncLogWriter.consoleLog
import log.effect.LogWriter

import java.sql.DriverManager
import java.sql.Connection
import cats.effect.IO
import com.dimafeng.testcontainers.DockerComposeContainer
import munit._
import cats.effect.unsafe.implicits.global

import java.nio.file.Files

class ITDBResourceAccessSpec extends FunSuite with ContainerSuite {

  def buildDBConnection(container: DockerComposeContainer): Connection =
    DriverManager.getConnection(
      s"jdbc:postgresql://${container.getServiceHost(dbServiceName, dbServicePort)}:${container
          .getServicePort(dbServiceName, dbServicePort)}/$dbName?allowPublicKeyRetrieval=true",
      dbUser,
      dbPassword
    )
  implicit val log: LogWriter[IO] = consoleLog

  def buildConfig(container: DockerComposeContainer): Config =
    Config(
      url = s"jdbc:postgresql://${container.getServiceHost(dbServiceName, dbServicePort)}:${container
          .getServicePort(dbServiceName, dbServicePort)}/$dbName?allowPublicKeyRetrieval=true",
      user = dbUser,
      password = dbPassword,
      port = dbServicePort.toString,
      dbName = dbServiceName,
      host = "localhost",
      driver = "org.postgresql.Driver"
    )

  test("getResourceByteArray should return the expected content") {
    withContainers { dockerComposeContainer =>
      val config     = buildConfig(dockerComposeContainer)
      val connection = buildDBConnection(dockerComposeContainer)
      val transactor = Transactor.fromDriverManager[IO](
        config.driver,
        config.url,
        config.user,
        config.password,
      )
      val resourceAccess = DBResourceAccess[IO](transactor)
      val obtained = for {
        _      <- Resource.eval(ITDBResourceAccessSpec.initDB(connection))
        result <- resourceAccess.getResourceByteArray("test media.mp3")
      } yield result

      obtained
        .use { byteArray =>
          IO(
            assertEquals(byteArray.toList, List(1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte))
          )
        }
        .unsafeRunSync()
    }
  }
  test("getResourcesByKind should return the expected list of files with expected content") {
    withContainers { dockerComposeContainer =>
      val config     = buildConfig(dockerComposeContainer)
      val connection = buildDBConnection(dockerComposeContainer)
      val transactor = Transactor.fromDriverManager[IO](
        config.driver,
        config.url,
        config.user,
        config.password,
      )
      val resourceAccess = DBResourceAccess[IO](transactor)
      val obtained = for {

        _      <- Resource.eval(ITDBResourceAccessSpec.initDB(connection))
        result <- resourceAccess.getResourcesByKind("kind")
      } yield result

      obtained
        .use { files =>
          IO(
            files.foreach { file =>
              assert(file.getName().startsWith("test media"))
              assert(file.getName().endsWith(".mp3"))
              assertEquals(Files.readAllBytes(file.toPath).toList, List(1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte))
            }
          )
        }
        .unsafeRunSync()
    }
  }
}

object ITDBResourceAccessSpec {

  val inserts = List(
    """INSERT INTO media (media_name, kind, media_content, created_at) VALUES ('test media.mp3', NULL, decode('010203040506070809', 'hex'), '2008-01-01 00:00:01');""",
    """INSERT INTO media (media_name, kind, media_content, created_at) VALUES ('test media2.mp3', 'kind', decode('010203040506070809', 'hex'), '2009-01-01 00:00:01');""",
    """INSERT INTO media (media_name, kind, media_content, created_at) VALUES ('test media3.mp3', 'kind', decode('010203040506070809', 'hex'), '2010-01-01 00:00:01');""",
  )

  // Insert data in the DB
  def initDB(connection: Connection): IO[Unit] = IO {
    connection.createStatement().executeUpdate("DELETE FROM media;")
    inserts.foreach { insert =>
      connection.createStatement().executeUpdate(insert)
    }
  }

}
