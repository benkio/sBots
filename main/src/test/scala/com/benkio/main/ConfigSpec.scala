package com.benkio.main

import munit.CatsEffectSuite
import cats.effect.IO

class ConfigSpec extends CatsEffectSuite {

  test("Config.loadConfig should load the correct configuration with default values") {
    Config.loadConfig[IO].map { config =>
      assert(config.hostUrl == "0.0.0.0")
      assert(config.port == 8443)
      assert(config.webhookCertificate == Some("sbotsCertificatePub.pem"))
      assert(config.keystorePath == Some("sbotsKeystore.jks"))
      assert(config.keystorePassword == Some("sbotsKeystorePassword"))
    }
  }
}
