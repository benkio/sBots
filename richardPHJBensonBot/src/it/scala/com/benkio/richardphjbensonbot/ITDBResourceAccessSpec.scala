package com.benkio.richardphjbensonbot

import java.time.Instant
import java.sql.Timestamp
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
import scala.concurrent.duration._

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

  // DBResourceAccess tests
  test("DBResourceAccess.getResourceByteArray should return the expected content") {
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
  test("DBResourceAccess.getResourcesByKind should return the expected list of files with expected content") {
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
  // DBTimeout tests
  test("DBTimeout.getOrDefault should return the default timeout if the chat id is not present in the database") {
    withContainers { dockerComposeContainer =>
      val config     = buildConfig(dockerComposeContainer)
      val connection = buildDBConnection(dockerComposeContainer)
      val transactor = Transactor.fromDriverManager[IO](
        config.driver,
        config.url,
        config.user,
        config.password,
      )
      val dbTimeout = DBTimeout[IO](transactor)
      val actual = for {
        _      <- Resource.eval(ITDBResourceAccessSpec.initDB(connection))
        result <- Resource.eval(dbTimeout.getOrDefault(100L)) // Not present ChatID
      } yield result
      actual
        .use { timeout =>
          IO {
            assertEquals(timeout.chat_id, 100L)
            assertEquals(timeout.timeout_value, "0")
          }
        }
        .unsafeRunSync()
    }
  }
  test("DBTimeout.getOrDefault should return the timeout if the chat id is present in the database") {
    withContainers { dockerComposeContainer =>
      val config     = buildConfig(dockerComposeContainer)
      val connection = buildDBConnection(dockerComposeContainer)
      val transactor = Transactor.fromDriverManager[IO](
        config.driver,
        config.url,
        config.user,
        config.password,
      )
      val dbTimeout = DBTimeout[IO](transactor)
      val actual = for {
        _      <- Resource.eval(ITDBResourceAccessSpec.initDB(connection))
        result <- Resource.eval(dbTimeout.getOrDefault(1L)) // Present ChatID
      } yield result
      actual
        .use { timeout =>
          IO {
            assertEquals(timeout.chat_id, 1L)
            assertEquals(timeout.timeout_value, "15000")
          }
        }
        .unsafeRunSync()
    }
  }
  test("DBTimeout.setTimeout should insert the timeout if the chat id is not present in the database") {
    withContainers { dockerComposeContainer =>
      val config     = buildConfig(dockerComposeContainer)
      val connection = buildDBConnection(dockerComposeContainer)
      val transactor = Transactor.fromDriverManager[IO](
        config.driver,
        config.url,
        config.user,
        config.password,
      )
      val dbTimeout = DBTimeout[IO](transactor)
      val chatId    = 2L
      val timeout   = Timeout(chatId, 2.seconds.toMillis.toString, Timestamp.from(Instant.now()))
      val actual = for {
        _      <- Resource.eval(ITDBResourceAccessSpec.initDB(connection))
        _      <- Resource.eval(dbTimeout.setTimeout(timeout))
        result <- Resource.eval(dbTimeout.getOrDefault(chatId))
      } yield result
      actual
        .use { timeout =>
          IO {
            assertEquals(timeout.chat_id, 2L)
            assertEquals(timeout.timeout_value, "2000")
          }
        }
        .unsafeRunSync()
    }
  }
  test("DBTimeout.setTimeout should update the timeout if the chat id is present in the database") {
    withContainers { dockerComposeContainer =>
      val config     = buildConfig(dockerComposeContainer)
      val connection = buildDBConnection(dockerComposeContainer)
      val transactor = Transactor.fromDriverManager[IO](
        config.driver,
        config.url,
        config.user,
        config.password,
      )
      val dbTimeout = DBTimeout[IO](transactor)
      val chatId    = 1L
      val timeout   = Timeout(chatId, 2.seconds.toMillis.toString, Timestamp.from(Instant.now()))
      val actual = for {
        _      <- Resource.eval(ITDBResourceAccessSpec.initDB(connection))
        _      <- Resource.eval(dbTimeout.setTimeout(timeout))
        result <- Resource.eval(dbTimeout.getOrDefault(chatId))
      } yield result
      actual
        .use { timeout =>
          IO {
            assertEquals(timeout.chat_id, 1L)
            assertEquals(timeout.timeout_value, "2000")
          }
        }
        .unsafeRunSync()
    }
  }
  test("DBTimeout.logLastInteraction should insert the timeout if the chat id is not present in the database") {
    withContainers { dockerComposeContainer =>
      val config     = buildConfig(dockerComposeContainer)
      val connection = buildDBConnection(dockerComposeContainer)
      val transactor = Transactor.fromDriverManager[IO](
        config.driver,
        config.url,
        config.user,
        config.password,
      )
      val dbTimeout = DBTimeout[IO](transactor)
      val chatId    = 2L
      val actual = for {
        _      <- Resource.eval(ITDBResourceAccessSpec.initDB(connection))
        _      <- Resource.eval(dbTimeout.logLastInteraction(chatId))
        result <- Resource.eval(dbTimeout.getOrDefault(chatId))
      } yield result
      actual
        .use { timeout =>
          IO {
            assertEquals(timeout.chat_id, 2L)
            assertEquals(timeout.timeout_value, "0")
          }
        }
        .unsafeRunSync()
    }
  }
  test(
    "DBTimeout.logLastInteraction should update the timeout last interaction if the chat id is present in the database"
  ) {
    withContainers { dockerComposeContainer =>
      val config     = buildConfig(dockerComposeContainer)
      val connection = buildDBConnection(dockerComposeContainer)
      val transactor = Transactor.fromDriverManager[IO](
        config.driver,
        config.url,
        config.user,
        config.password,
      )
      val dbTimeout = DBTimeout[IO](transactor)
      val chatId    = 1L
      val actual = for {
        _           <- Resource.eval(ITDBResourceAccessSpec.initDB(connection))
        prevResult  <- Resource.eval(dbTimeout.getOrDefault(chatId))
        _           <- Resource.eval(dbTimeout.logLastInteraction(chatId))
        afterResult <- Resource.eval(dbTimeout.getOrDefault(chatId))
      } yield (prevResult, afterResult)
      actual
        .use { case (prevTimeout, afterTimeout) =>
          IO {
            assertEquals(prevTimeout.chat_id, 1L)
            assertEquals(prevTimeout.timeout_value, "15000")
            assertEquals(afterTimeout.chat_id, 1L)
            assertEquals(afterTimeout.timeout_value, "15000")
            assert(prevTimeout.last_interaction.before(afterTimeout.last_interaction))
          }
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
    """INSERT INTO timeout (chat_id, timeout_value, last_interaction) VALUES ('1', '15000', '2010-01-01 00:00:01');""",
  )

  // Insert data in the DB
  def initDB(connection: Connection): IO[Unit] = IO {
    connection.createStatement().executeUpdate("DELETE FROM media;")
    connection.createStatement().executeUpdate("DELETE FROM timeout;")
    inserts.foreach { insert =>
      connection.createStatement().executeUpdate(insert)
    }
  }

}
