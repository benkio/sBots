package com.benkio.botDB

import com.benkio.botDB.config.ShowConfig
import com.benkio.botDB.config.Config
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData

object TestData {

  val google: DBMediaData = DBMediaData(
    media_name = "google.gif",
    kinds = "[]",
    mime_type = "image/gif",
    media_sources = """["https://www.google.com"]""",
    created_at = 1658054878L.toString,
    media_count = 0
  )
  val amazon: DBMediaData = DBMediaData(
    media_name = "amazon.mp4",
    kinds = """["kind"]""",
    mime_type = "video/mp4",
    media_sources = """["https://www.amazon.com"]""",
    created_at = 1658054878L.toString,
    media_count = 0
  )
  val facebook: DBMediaData = DBMediaData(
    media_name = "facebook.mp3",
    kinds = """["kind_innerKind"]""",
    mime_type = "audio/mpeg",
    media_sources = """["https://www.facebook.com"]""",
    created_at = 1658054878L.toString,
    media_count = 0
  )

  val config: Config = Config(
    driver = "org.sqlite.JDBC",
    dbName = "botDB",
    url = "jdbc:sqlite:C:/sqlite/db/chinook.db",
    migrationsLocations = List("db/migrations"),
    migrationsTable = "FlywaySchemaHistory",
    jsonLocation = List.empty,
    showConfig = ShowConfig(List.empty, false, false)
  )
}
