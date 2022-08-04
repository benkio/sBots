package com.benkio.botDB

import munit.CatsEffectSuite
import com.benkio.botDB.db.DatabaseRepository
import cats.effect.IO
import com.dimafeng.testcontainers.DockerComposeContainer
import java.sql.DriverManager
import java.sql.Connection

import doobie.Transactor

class DBSpec extends CatsEffectSuite with ContainerSuite {

  def buildDBConnection(container: DockerComposeContainer): Connection =
    DriverManager.getConnection(
      s"jdbc:postgresql://${container.getServiceHost(dbServiceName, dbServicePort)}:${container
          .getServicePort(dbServiceName, dbServicePort)}/$dbName?allowPublicKeyRetrieval=true",
      dbUser,
      dbPassword
    )

  def transactor(c: Connection): Transactor[IO] =
    Transactor.fromConnection[IO](c)

  test("The `media` table should exist and be readable/writable") {
    withContainers { dockerComposeContainer =>
      val connection = buildDBConnection(dockerComposeContainer)
      connection.createStatement().executeUpdate(DBSpec.mediaSQL)
      val resultSet = connection.createStatement().executeQuery("SELECT media_name, created_at FROM media")
      resultSet.next()
      val actualMediaName      = resultSet.getString("media_name")
      val actualMediaCreatedAt = resultSet.getString("created_at")
      assertEquals(actualMediaName, "test media.mp3")
      assertEquals(actualMediaCreatedAt, "2008-01-01 00:00:01")
      connection.close()
    }
  }

  test("The `timeout` table should exist and be readable/writable") {
    withContainers { dockerComposeContainer =>
      val connection = buildDBConnection(dockerComposeContainer)
      connection.createStatement().executeUpdate(DBSpec.timeoutSQL)
      val resultSet = connection.createStatement().executeQuery("SELECT chat_id, timeout_value, last_interaction FROM timeout")
      resultSet.next()
      val actualChatId      = resultSet.getString("chat_id")
      val actualTimeoutValue      = resultSet.getString("timeout_value")
      val actualTimeoutLastInteraction = resultSet.getString("last_interaction")
      assertEquals(actualChatId, "123456789")
      assertEquals(actualTimeoutValue, "1000")
      assertEquals(actualTimeoutLastInteraction, "2008-01-01 00:00:01")
      connection.close()
    }
  }

// DatabaseRepository /////////////////////////////////////////////////////////

  test("DatabaseRepository should be able to insert a Media") {
    withContainers { dockerComposeContainer =>
      val connection         = buildDBConnection(dockerComposeContainer)
      val databaseRepository = DatabaseRepository[IO](transactor(connection))
      assertIO_(databaseRepository.insertMedia(TestData.mediaEntity1))
    }
  }
}

object DBSpec {

  val mediaSQL: String = """
INSERT INTO media (media_name, kind, media_content, created_at) VALUES ('test media.mp3', NULL, decode('89504E470D0A1A0A0000000D494844520000001000000010080200000090916836000000017352474200AECE1CE90000000467414D410000B18F0BFC6105000000097048597300000EC300000EC301C76FA8640000001E49444154384F6350DAE843126220493550F1A80662426C349406472801006AC91F1040F796BD0000000049454E44AE426082', 'hex'), '2008-01-01 00:00:01');
"""

  val timeoutSQL = """
INSERT INTO timeout (chat_id, timeout_value, last_interaction) VALUES (123456789, 1000 ,'2008-01-01 00:00:01');
"""
}
