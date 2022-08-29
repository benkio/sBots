package com.benkio.richardphjbensonbot

import log.effect.LogLevels
import cats.effect.IO
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogWriter
import java.sql.DriverManager
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import scala.io.BufferedSource
import munit._

trait DBFixture { self: FunSuite =>

  implicit val log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  val dbName: String       = "botDB.db"
  val resourcePath: String = getClass.getResource("/").getPath
  val dbPath: String       = s"$resourcePath$dbName"
  val dbUrl: String        = s"jdbc:sqlite:$dbPath";

  val config = Config(
    url = dbUrl,
    dbName = dbName,
    driver = "org.sqlite.JDBC"
  )

  val databaseConnection = FunFixture[Connection](
    setup = { _ =>
      Files.deleteIfExists(Paths.get(dbPath))
      val conn    = DriverManager.getConnection(dbUrl)
      val isValid = conn.isValid(10)
      println(s"conn is valid: " + isValid)
      val ddls = getMigrations
      ddls.foreach { ddl =>
        {
          val statement = conn.createStatement()
          statement.executeQuery(ddl.getLines().mkString(" "))
        }
      }
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

  def getMigrations: List[BufferedSource] = ???

}
