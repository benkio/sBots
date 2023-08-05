package com.benkio.integration

import cats.effect.IO
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.web.UrlFetcher
import doobie.Transactor
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit._
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.http4s.ember.client._
import annotation.unused

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager

final case class DBFixtureResources(
  connection: Connection,
  transactor: Transactor[IO],
  resourceDBLayer: Resource[IO, DBLayer[IO]],
  resourceAccessResource: Resource[IO, ResourceAccess[IO]]
)

trait DBFixture { self: FunSuite =>

  implicit val log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  val dbName: String         = "botDB.sqlite3"
  val resourcePath: String   = new File("./..").getCanonicalPath
  val dbPath: String         = s"$resourcePath/$dbName"
  val dbUrl: String          = s"jdbc:sqlite:$dbPath";
  val deleteDB: Boolean      = false
  val migrationPath: String  = "filesystem:" + resourcePath + "/botDB/src/main/resources/db/migrations"
  val migrationTable: String = "FlywaySchemaHistory"

  lazy val databaseFixture = FunFixture[DBFixtureResources](
    setup = DBFixture.fixtureSetup,
    teardown = DBFixture.teardownFixture
  )
}

object DBFixture {

  def fixtureSetup(@unused testOptions: TestOptions): DBFixtureResources = {
    Class.forName("org.sqlite.JDBC")
    val conn = DriverManager.getConnection(dbUrl)
    runMigrations(dbUrl, migrationTable, migrationPath)
    val transactor                                 = Transactor.fromConnection[IO](conn, None)
    val dbLayerResource: Resource[IO, DBLayer[IO]] = Resource.eval(DBLayer[IO](transactor))
    val resourceAccessResource: Resource[IO, ResourceAccess[IO]] = dbLayerResource.flatMap(dbLayer =>
      for {
        httpClient <- EmberClientBuilder
        .default[IO]
        .withMaxResponseHeaderSize(8192)
        .build
        urlFetcher <- Resource.eval(UrlFetcher[IO](httpClient))
      } yield ResourceAccess.dbResources[IO](
        dbMedia = dbLayer.dbMedia,
        urlFetcher = urlFetcher
      )
    )

    cleanDB(conn)
    DBFixtureResources(
      connection = conn,
      transactor = transactor,
      resourceDBLayer = dbLayerResource,
      resourceAccessResource = resourceAccessResource
    )
  }

  def teardownFixture(fixture: DBFixtureResources): Unit = {
    if (deleteDB) Files.deleteIfExists(Paths.get(dbPath))
    else cleanDB(fixture.connection)
    fixture.connection.close()
    ()
  }

  def cleanDB(connection: Connection): Unit ={
    connection.createStatement().executeUpdate("DELETE FROM timeout;")
    connection.createStatement().executeUpdate("DELETE FROM subscription;")
    connection.createStatement().executeUpdate("DELETE FROM log;")
    connection.createStatement().executeUpdate("UPDATE media SET media_count = 0;")
    ()
  }

  def runMigrations(dbUrl: String, migrationsTable: String, migrationsLocations: String): Unit = {
    val m: FluentConfiguration = Flyway.configure
      .dataSource(
        dbUrl,
        "",
        ""
      )
      .group(true)
      .mixed(true)
      .outOfOrder(false)
      .table(migrationsTable)
      .validateOnMigrate(true)
      .failOnMissingLocations(true)
      .locations(
        migrationsLocations
      )
      .baselineOnMigrate(true)

    val _ = m.load().migrate().migrationsExecuted
    ()
  }

}
