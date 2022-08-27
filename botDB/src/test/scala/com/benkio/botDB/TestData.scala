package com.benkio.botDB

import com.benkio.botDB.Config
import com.benkio.botDB.db.schema.MediaEntity

import java.sql.Timestamp
object TestData {

  val mediaEntity1 = MediaEntity(
    media_name = "media1.txt",
    kind = None,
    media_content = Array(),
    created_at = new Timestamp(1658054878L)
  )
  val mediaEntity2 = MediaEntity(
    media_name = "media2.txt",
    kind = Some("kind"),
    media_content = Array(),
    created_at = new Timestamp(1658054878L)
  )
  val mediaEntity3 = MediaEntity(
    media_name = "media3.txt",
    kind = Some("kind_innerKind"),
    media_content = Array(),
    created_at = new Timestamp(1658054878L)
  )

  val config: Config = Config(
    driver = "org.postgresql.Driver",
    dbName = "botDB",
    user = "botUser",
    password = "botPassword",
    host = "localhost",
    port = 5432,
    url = "jdbc:postgresql://localhost:5432/botDB",
    migrationsLocations = List("db/migrations"),
    migrationsTable = "FlywaySchemaHistory",
    csvLocation = "/testdata/"
  )
}
