package com.benkio.telegrambotinfrastructure

import scala.io.Source
import java.io.File
import doobie.Transactor
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

  val dbName: String        = "botDB.sqlite3"
  val resourcePath: String  = new File("./..").getCanonicalPath
  val dbPath: String        = s"$resourcePath/$dbName"
  val dbUrl: String         = s"jdbc:sqlite:$dbPath";
  val deleteDB: Boolean     = false
  val migrationPath: String = resourcePath + "/botDB/src/main/resources/db/migrations"

  lazy val databaseFixture = FunFixture[(Connection, DBResourceAccess[IO], Transactor[IO])](
    setup = { _ =>
      Class.forName("org.sqlite.JDBC")
      val conn = DriverManager.getConnection(dbUrl)
      val ddls = getMigrations(migrationPath)
      ddls.foreach { ddl =>
        {
          val query     = ddl.getLines().mkString(" ")
          val statement = conn.createStatement()
          statement.executeUpdate(query)
        }
      }
      val transactor = Transactor.fromConnection[IO](conn)
      val urlFetcher = UrlFetcher[IO]()
      val resourceAccess: DBResourceAccess[IO] = new DBResourceAccess[IO](
        transactor = transactor,
        urlFetcher = urlFetcher,
        log = log
      )
      conn.createStatement().executeUpdate("DELETE FROM timeout;")
      (conn, resourceAccess, transactor)
    },
    teardown = { conn_resourceAccess =>
      {
        if (deleteDB) Files.deleteIfExists(Paths.get(dbPath))
        else conn_resourceAccess._1.createStatement().executeUpdate("DELETE FROM timeout;")
        conn_resourceAccess._1.close()
        ()
      }
    }
  )

  def getMigrations(migrationPath: String): List[BufferedSource] = {
    val migrationDir = new File(migrationPath)
    val migrations = if (migrationDir.exists && migrationDir.isDirectory) {
      migrationDir.listFiles.filter(_.isFile).toList
    } else { List[File]() }
    migrations.map(m => Source.fromFile(m))
  }

}
