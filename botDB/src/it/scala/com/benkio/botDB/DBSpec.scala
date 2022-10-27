package com.benkio.botDB

import com.benkio.botDB.db.DBMigrator
import munit.CatsEffectSuite
import com.benkio.botDB.db.DatabaseRepository
import cats.effect.IO
import java.nio.file.Paths
import java.nio.file.Files

import java.sql.DriverManager;

import java.sql.Connection

import doobie.Transactor

class DBSpec extends CatsEffectSuite with DBConstants {

  val databaseConnection = FunFixture[Connection](
    setup = { _ =>
      Files.deleteIfExists(Paths.get(dbPath))
      DBMigrator.unsafeMigrate(TestData.config.copy(url = dbUrl))
      val conn    = DriverManager.getConnection(dbUrl)
      val isValid = conn.isValid(10)
      println(s"conn is valid: " + isValid)
      conn
    },
    teardown = { conn =>
      {
        conn.close()
        Files.deleteIfExists(Paths.get(dbPath))
        ()
      }
    }
  )

  def transactor(c: Connection): Transactor[IO] =
    Transactor.fromConnection[IO](c)

  databaseConnection.test("The `media` table should exist and be readable/writable") { connection =>
    connection.createStatement().executeUpdate(DBSpec.mediaSQL)
    val resultSet = connection.createStatement().executeQuery("SELECT media_name, created_at, media_count FROM media")
    resultSet.next()
    val actualMediaName      = resultSet.getString("media_name")
    val actualMediaCreatedAt = resultSet.getString("created_at")
    val actualCount          = resultSet.getInt("media_count")
    assertEquals(actualMediaName, "test media.mp3")
    assertEquals(actualMediaCreatedAt, "2008-01-01 00:00:01")
    assertEquals(actualCount, 0)
  }

  databaseConnection.test("The `timeout` table should exist and be readable/writable") { connection =>
    connection.createStatement().executeUpdate(DBSpec.timeoutSQL)
    val resultSet =
      connection.createStatement().executeQuery("SELECT chat_id, timeout_value, last_interaction FROM timeout")
    resultSet.next()
    val actualChatId                 = resultSet.getString("chat_id")
    val actualTimeoutValue           = resultSet.getString("timeout_value")
    val actualTimeoutLastInteraction = resultSet.getString("last_interaction")
    assertEquals(actualChatId, "123456789")
    assertEquals(actualTimeoutValue, "1000")
    assertEquals(actualTimeoutLastInteraction, "2008-01-01 00:00:01")
  }

  // DatabaseRepository /////////////////////////////////////////////////////////

  databaseConnection.test("DatabaseRepository should be able to insert a Media") { connection =>
    val databaseRepository = DatabaseRepository[IO](transactor(connection))
    assertIO_(databaseRepository.insertMedia(TestData.google))
  }
}

object DBSpec {

  val mediaSQL: String = """
INSERT INTO media (media_name, kind, media_url, created_at, media_count) VALUES ('test media.mp3', NULL, 'https://www.google.com', '2008-01-01 00:00:01', 0);
"""

  val timeoutSQL = """
INSERT INTO timeout (chat_id, timeout_value, last_interaction) VALUES (123456789, 1000 ,'2008-01-01 00:00:01');
"""
}
