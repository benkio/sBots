package com.benkio.chatcore

import cats.effect.IO
import com.benkio.chatcore.initialization.Config
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite

class ConfigSpec extends CatsEffectSuite {

  test("Config.loadConfig should load the correct configuration with default values") {
    Config.loadConfig[IO]("testInfraDBConf").map { config =>
      assert(config.db.driver == "org.sqlite.JDBC")
      assert(config.db.dbName == "testDB.sqlite3")
      assert(config.db.url == "jdbc:sqlite:testDB.sqlite3")
    }
  }
}
