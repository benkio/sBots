package com.benkio.integration.integrationmunit.botDB

import cats.effect.IO
import com.benkio.botDB.db.DBMigrator
import com.benkio.botDB.TestData
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia
import doobie.Transactor
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager

class DBSpec extends CatsEffectSuite with Constants {

  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  val databaseConnection: FunFixture[Connection] = FunFixture[Connection](
    setup = { _ =>
      Files.deleteIfExists(Paths.get(dbPath))
      val _       = DBMigrator.unsafeMigrate(TestData.config.copy(url = dbUrl))
      val conn    = DriverManager.getConnection(dbUrl)
      val isValid = conn.isValid(10)
      println("conn is valid: " + isValid)
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
    Transactor.fromConnection[IO](c, None)

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
      connection
        .createStatement()
        .executeQuery("SELECT chat_id, bot_name, timeout_value, last_interaction FROM timeout")
    resultSet.next()
    val actualChatId                 = resultSet.getString("chat_id")
    val actualBotName                = resultSet.getString("bot_name")
    val actualTimeoutValue           = resultSet.getString("timeout_value")
    val actualTimeoutLastInteraction = resultSet.getString("last_interaction")
    assertEquals(actualChatId, "123456789")
    assertEquals(actualBotName, "botName")
    assertEquals(actualTimeoutValue, "1000")
    assertEquals(actualTimeoutLastInteraction, "2008-01-01 00:00:01")
  }

  // DBMedia /////////////////////////////////////////////////////////

  databaseConnection.test("DBMedia should be able to insert a Media") { connection =>
    assertIO_(
      DBMedia[IO](transactor(connection)).flatMap(
        _.insertMedia(TestData.google)
      )
    )
  }
}

object DBSpec {

  val mediaSQL: String = """
INSERT INTO media (media_name, kinds, mime_type, media_sources, created_at, media_count) VALUES ('test media.mp3', '', 'audio/mpeg', json('["https://www.google.com"]'), '2008-01-01 00:00:01', 0);
"""

  val timeoutSQL = """
INSERT INTO timeout (chat_id, bot_name, timeout_value, last_interaction) VALUES (123456789, 'botName', 1000 ,'2008-01-01 00:00:01');
"""
}
