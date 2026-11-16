package com.benkio.chatcore

import cats.effect.IO
import com.benkio.chatcore.config.StartupCheckConfig
import com.benkio.chatcore.initialization.Config
import com.benkio.chatcore.Logger.given
import munit.CatsEffectSuite
import pureconfig.ConfigSource

import scala.concurrent.duration.*

class ConfigSpec extends CatsEffectSuite {

  test("Config.loadConfig should load the correct configuration with default values") {
    Config.loadConfig[IO]("testInfraDBConf").map { config =>
      assert(config.db.driver == "org.sqlite.JDBC")
      assert(config.db.dbName == "testDB.sqlite3")
      assert(config.db.url == "jdbc:sqlite:testDB.sqlite3")
      assertEquals(config.httpCache.dropbox.ttlSeconds, 21600L)
      assertEquals(config.httpCache.dropbox.maxSize, 20)
      assertEquals(config.httpCache.mega.ttlSeconds, 21600L)
      assertEquals(config.httpCache.mega.maxSize, 20)
      assertEquals(config.requestProcessingTimeout, Some(10.seconds))
      assertEquals(config.startupCheck, StartupCheckConfig())
    }
  }

  test("the startup check is configured with the kebab case keys and has defaults") {
    val parsed = ConfigSource
      .string("""{ probe-timeout = 5 seconds, test-media = "a.mp3" }""")
      .load[StartupCheckConfig]
    assertEquals(
      parsed,
      Right(StartupCheckConfig(probeTimeout = 5.seconds, testMedia = Some("a.mp3")))
    )
    assertEquals(StartupCheckConfig().slowThreshold, 3.seconds)
    assert(StartupCheckConfig().enabled)
  }
}
