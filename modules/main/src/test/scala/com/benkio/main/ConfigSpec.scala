package com.benkio.main

import cats.effect.IO
import com.benkio.chatcore.Logger.logWriter
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

  test(
    "Config.loadConfig should load the Telegram block, with a placeholder chat of the operators that the deploy sets"
  ) {
    Config.loadConfig[IO].map { config =>
      assert(config.telegram.webhookBaseUrl.startsWith("https://"), config.telegram.webhookBaseUrl)
      assertEquals(config.telegram.opsChatId, 0L)
    }
  }

  test("Config.loadConfig should load the healthcheck ping as a placeholder url that the deploy sets") {
    Config.loadConfig[IO].map { config =>
      assertEquals(config.healthcheckPing.endpoint.renderString, "https://hc-ping.com/placeholder")
    }
  }

  test("Config.loadConfig should load the Discord block, with the message content intent by default") {
    Config.loadConfig[IO].map { config =>
      assertEquals(config.discord.shutdownTimeout, 10.seconds)
      assert(config.discord.messageContent)
    }
  }

  test("Config.loadConfig should load the IRC block, with TLS and no channels by default") {
    Config.loadConfig[IO].map { config =>
      assert(config.irc.tls)
      assertEquals(config.irc.port, 6697)
      assertEquals(config.irc.channels, List.empty)
      assertEquals(config.irc.reconnectDelay(0), 2.seconds)
      assertEquals(config.irc.messageDelay, 1200.millis)
    }
  }

  test("Config.loadConfig should load the LLM section with its timeout, token limits and per-provider quotas") {
    Config.loadConfig[IO].map { config =>
      assertEquals(config.llm.timeout, 10.seconds)
      assertEquals(config.llm.maxInputTokens, 4096)
      assertEquals(config.llm.maxOutputTokens, 512)
      assertEquals(config.llm.gemini.quota.maxPerMinute, 20)
      assertEquals(config.llm.gemini.quota.maxPerDay, 50)
      assertEquals(config.llm.openrouter.quota.maxPerMinute, 20)
      assertEquals(config.llm.openrouter.quota.maxPerDay, 50)
    }
  }

  test("the text mode store of a bot is configurable in the application.conf of the bot's namespace") {
    com.benkio.chatcore.initialization.Config
      .loadConfig[IO]("rphjb")
      .map { config =>
        assertEquals(config.pagedListTextModeExpiry, 10.minutes)
        assertEquals(config.pagedListTextModeMaxChats, 100)
      }
  }
}
