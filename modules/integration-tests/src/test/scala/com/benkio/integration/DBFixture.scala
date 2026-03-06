package com.benkio.integration

import annotation.unused
import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.http.DropboxClient
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.db.DBRepository
import com.benkio.chatcore.repository.Repository
import com.benkio.integrationtest.Logger.given
import doobie.Transactor
import munit.*
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.Flyway
import org.http4s.ember.client.*

import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager

import _root_.log.effect.LogWriter

final case class DBFixtureResources(
    connection: Connection,
    transactor: Transactor[IO],
    resourceDBLayer: Resource[IO, DBLayer[IO]],
    repositoryResource: Resource[IO, Repository[IO]],
    dropboxClientResource: Resource[IO, DropboxClient[IO]]
)

trait DBFixture { self: FunSuite =>

  lazy val databaseFixture: FunFixture[DBFixtureResources] = FunFixture[DBFixtureResources](
    setup = DBFixture.fixtureSetup,
    teardown = DBFixture.teardownFixture
  )
}

object DBFixture {

  val dbName: String         = "botDB.sqlite3"
  val resourcePath: String   = Paths.get(".").resolve("..").toAbsolutePath().normalize().toString
  val dbPath: String         = s"$resourcePath/../$dbName"
  val dbUrl: String          = s"jdbc:sqlite:$dbPath";
  val deleteDB: Boolean      = false
  val migrationPath: String  = "filesystem:" + resourcePath + "/botDB/src/main/resources/db/migrations"
  val migrationTable: String = "FlywaySchemaHistory"

  def fixtureSetup(@unused testOptions: TestOptions)(using log: LogWriter[IO]): DBFixtureResources = {
    Class.forName("org.sqlite.JDBC")
    val conn = DriverManager.getConnection(DBFixture.dbUrl)

    runMigrations(
      dbUrl = DBFixture.dbUrl,
      migrationsTable = DBFixture.migrationTable,
      migrationsLocations = DBFixture.migrationPath
    )
    val transactor                                 = Transactor.fromConnection[IO](conn, None)
    val dbLayerResource: Resource[IO, DBLayer[IO]] = Resource.eval(DBLayer[IO](transactor))
    val dropboxClientNRepositoryResource: Resource[IO, (DropboxClient[IO], Repository[IO])] =
      dbLayerResource.flatMap(dbLayer =>
        for {
          _          <- Resource.eval(log.debug(s"DbUrl: $dbUrl ||| migrations path: $migrationPath"))
          httpClient <- EmberClientBuilder
            .default[IO]
            .withMaxResponseHeaderSize(8192)
            .build
          dropboxClient <- Resource.eval(DropboxClient[IO](httpClient))
        } yield (
          dropboxClient,
          DBRepository.dbResources[IO](
            dbMedia = dbLayer.dbMedia,
            dropboxClient = dropboxClient
          )
        )
      )

    cleanDB(conn)
    DBFixtureResources(
      connection = conn,
      transactor = transactor,
      resourceDBLayer = dbLayerResource,
      repositoryResource = dropboxClientNRepositoryResource.map(_._2),
      dropboxClientResource = dropboxClientNRepositoryResource.map(_._1)
    )
  }

  def teardownFixture(fixture: DBFixtureResources): Unit = {
    if deleteDB then Files.deleteIfExists(Paths.get(dbPath))
    else cleanDB(fixture.connection)
    fixture.connection.close()
    ()
  }

  def cleanDB(connection: Connection): Unit = {
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
