package com.benkio.telegrambotinfrastructure

import cats.effect.IO
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia
import com.benkio.telegrambotinfrastructure.resources.db.DBResourceAccess.DBResourceAccess
import com.benkio.telegrambotinfrastructure.web.UrlFetcher
import doobie.Transactor
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit._
import org.http4s.ember.client._

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import scala.io.BufferedSource
import scala.io.Source

final case class DBFixtureResources(
    connection: Connection,
    resourceAccessResource: Resource[IO, DBResourceAccess[IO]],
    dbMediaResource: Resource[IO, DBMedia[IO]],
    transactor: Transactor[IO]
)

trait DBFixture { self: FunSuite =>

  implicit val log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  val dbName: String        = "botDB.sqlite3"
  val resourcePath: String  = new File("./..").getCanonicalPath
  val dbPath: String        = s"$resourcePath/$dbName"
  val dbUrl: String         = s"jdbc:sqlite:$dbPath";
  val deleteDB: Boolean     = false
  val migrationPath: String = resourcePath + "/botDB/src/main/resources/db/migrations"

  lazy val databaseFixture = FunFixture[DBFixtureResources](
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
      val transactor                                 = Transactor.fromConnection[IO](conn)
      val dbMediaResource: Resource[IO, DBMedia[IO]] = Resource.eval(DBMedia[IO](transactor))
      val resourceAccessResource: Resource[IO, DBResourceAccess[IO]] = dbMediaResource.flatMap(dbMedia =>
        for {
          httpClient <- EmberClientBuilder.default[IO].build
          urlFetcher <- Resource.eval(UrlFetcher[IO](httpClient))
        } yield new DBResourceAccess[IO](
          dbMedia = dbMedia,
          urlFetcher = urlFetcher
        )
      )

      conn.createStatement().executeUpdate("DELETE FROM timeout;")
      DBFixtureResources(
        connection = conn,
        resourceAccessResource = resourceAccessResource,
        dbMediaResource = dbMediaResource,
        transactor = transactor
      )
    },
    teardown = { fixture =>
      {
        if (deleteDB) Files.deleteIfExists(Paths.get(dbPath))
        else fixture.connection.createStatement().executeUpdate("DELETE FROM timeout;")
        fixture.connection.close()
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
