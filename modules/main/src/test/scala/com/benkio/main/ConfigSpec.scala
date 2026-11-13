package com.benkio.main

import cats.effect.IO
import munit.CatsEffectSuite

import scala.concurrent.duration.*

class ConfigSpec extends CatsEffectSuite {

  test("Config.loadConfig should load the correct configuration with default values") {
    Config.loadConfig[IO].map { config =>
      assertEquals(config.http.hostUrl, "0.0.0.0")
      assertEquals(config.http.port, 8443)
      assertEquals(config.http.keystorePath, None)
      assertEquals(config.http.keystorePassword, None)
      assertEquals(config.telegram.webhookCertificate, None)
      assertEquals(config.mainDB.driver, "org.sqlite.JDBC")
      assertEquals(config.mainDB.dbName, "../botDB.sqlite3")
      assertEquals(config.mainDB.url, "jdbc:sqlite:../botDB.sqlite3")
    }
  }

  test("Config.loadConfig should load the Telegram block, with the chat of the operators of before as the default") {
    Config.loadConfig[IO].map { config =>
      assert(config.telegram.webhookBaseUrl.startsWith("https://"), config.telegram.webhookBaseUrl)
      assertEquals(config.telegram.opsChatId, -4145546019L)
    }
  }

  test("Config.loadConfig should load the Discord block, with the message content intent by default") {
    Config.loadConfig[IO].map { config =>
      assertEquals(config.discord.shutdownTimeout, 10.seconds)
      assert(config.discord.messageContent)
    }
  }
}
