package com.benkio.telegrambotinfrastructure

import cats.effect.IO
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite

class ConfigSpec extends CatsEffectSuite {

  given LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

  test("Config.loadConfig should load the correct configuration with default values") {
    Config.loadConfig[IO]("testInfraDBConf").map { config =>
      assert(config.driver == "org.sqlite.JDBC")
      assert(config.dbName == "testDB.sqlite3")
      assert(config.url == "jdbc:sqlite:testDB.sqlite3")
    }
  }
}
