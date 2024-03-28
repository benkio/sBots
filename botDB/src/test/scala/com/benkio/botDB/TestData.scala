package com.benkio.botDB

import com.benkio.botDB.db.schema.MediaEntity

import java.net.URI
import java.sql.Timestamp
object TestData {

  val google: MediaEntity = MediaEntity(
    media_name = "google.gif",
    kinds = List.empty,
    mime_type = "image/gif",
    media_url = URI.create("https://www.google.com").toURL(),
    created_at = new Timestamp(1658054878L)
  )
  val amazon: MediaEntity = MediaEntity(
    media_name = "amazon.mp4",
    kinds = List("kind"),
    mime_type = "video/mp4",
    media_url = URI.create("https://www.amazon.com").toURL(),
    created_at = new Timestamp(1658054878L)
  )
  val facebook: MediaEntity = MediaEntity(
    media_name = "facebook.mp3",
    kinds = List("kind_innerKind"),
    mime_type = "audio/mpeg",
    media_url = URI.create("https://www.facebook.com").toURL(),
    created_at = new Timestamp(1658054878L)
  )

  val config: Config = Config(
    driver = "org.sqlite.JDBC",
    dbName = "botDB",
    url = "jdbc:sqlite:C:/sqlite/db/chinook.db",
    migrationsLocations = List("db/migrations"),
    migrationsTable = "FlywaySchemaHistory",
    jsonLocation = List.empty
  )
}
