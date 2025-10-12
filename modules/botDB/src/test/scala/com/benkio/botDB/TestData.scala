package com.benkio.botDB

import com.benkio.botDB.config.Config
import com.benkio.botDB.config.ShowConfig
import com.benkio.botDB.config.ShowSourceConfig
import com.benkio.telegrambotinfrastructure.repository.db.DBMediaData

object TestData {

  val google: DBMediaData = DBMediaData(
    media_name = "botid_google.gif",
    bot_id = "botid",
    kinds = "[]",
    mime_type = "image/gif",
    media_sources = """["https://www.google.com"]""",
    created_at = 1658054878L.toString,
    media_count = 0
  )
  val amazon: DBMediaData = DBMediaData(
    media_name = "botid_amazon.mp4",
    bot_id = "botid",
    kinds = """["kind"]""",
    mime_type = "video/mp4",
    media_sources = """["https://www.amazon.com"]""",
    created_at = 1658054878L.toString,
    media_count = 0
  )
  val facebook: DBMediaData = DBMediaData(
    media_name = "botid_facebook.mp3",
    bot_id = "botid",
    kinds = """["kind_innerKind"]""",
    mime_type = "audio/mpeg",
    media_sources = """["https://www.facebook.com"]""",
    created_at = 1658054878L.toString,
    media_count = 0
  )

  val showSourceConfig: ShowSourceConfig = ShowSourceConfig(
    youtubeSources = List("PLO1i4nEhzCLYvR6gBHuZJS4z28he2S8yh"),
    botId = "testbot",
    captionLanguage = "it",
    outputFilePath = "./src/test/resources/testdata/testBotShow.json"
  )

  val config: Config = Config(
    driver = "org.sqlite.JDBC",
    dbName = "../../botDB.sqlite3",
    url = "jdbc:sqlite:../../botDB.sqlite3",
    migrationsLocations = List("db/migrations"),
    migrationsTable = "FlywaySchemaHistory",
    jsonLocation = List("/testdata"),
    showConfig = ShowConfig(List(showSourceConfig), false, false, false, "sBots")
  )
}
