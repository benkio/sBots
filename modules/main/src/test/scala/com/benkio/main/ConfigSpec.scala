package com.benkio.main

import cats.effect.IO
import munit.CatsEffectSuite

class ConfigSpec extends CatsEffectSuite {

  test("Config.loadConfig should load the correct configuration with default values") {
    Config.loadConfig[IO].map { config =>
      assertEquals(config.hostUrl, "0.0.0.0")
      assertEquals(config.port, 8443)
      assertEquals(config.webhookCertificate, Some("sbotsCertificatePub.pem"))
      assertEquals(config.keystorePath, Some("sbotsKeystore.jks"))
      assertEquals(config.keystorePassword, Some("sbotsKeystorePassword"))
      assertEquals(config.mainDB.driver, "org.sqlite.JDBC")
      assertEquals(config.mainDB.dbName, "../botDB.sqlite3")
      assertEquals(config.mainDB.url, "jdbc:sqlite:../botDB.sqlite3")
    }
  }
}
