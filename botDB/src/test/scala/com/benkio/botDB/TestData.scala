package com.benkio.botDB

import com.benkio.botDB.Config
import com.benkio.botDB.db.schema.MediaEntity

import java.net.URL
import java.sql.Timestamp
object TestData {

  val google = MediaEntity(
    media_name = "google.gif",
    kind = None,
    media_url = new URL("https://www.google.com"),
    created_at = new Timestamp(1658054878L)
  )
  val amazon = MediaEntity(
    media_name = "amazon.mp4",
    kind = Some("kind"),
    media_url = new URL("https://www.amazon.com"),
    created_at = new Timestamp(1658054878L)
  )
  val facebook = MediaEntity(
    media_name = "facebook.mp3",
    kind = Some("kind_innerKind"),
    media_url = new URL("https://www.facebook.com"),
    created_at = new Timestamp(1658054878L)
  )

  val config: Config = Config(
    driver = "org.sqlite.JDBC",
    dbName = "botDB",
    url = "jdbc:sqlite:C:/sqlite/db/chinook.db",
    migrationsLocations = List("db/migrations"),
    migrationsTable = "FlywaySchemaHistory",
    csvLocation = List.empty
  )
}
