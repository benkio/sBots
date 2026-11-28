package com.benkio.main

import cats.effect.IO
import com.benkio.chatcore.llm.Persona
import com.benkio.chatcore.Logger.logWriter
import com.benkio.chatllm.LLMLoadingError
import com.benkio.chatllm.LlmEngine
import com.benkio.chatllm.LlmLoader
import com.benkio.chatllm.LlmReliabilitySettings
import com.typesafe.config.Config as TypesafeConfig
import com.typesafe.config.ConfigFactory
import munit.CatsEffectSuite
import org.llm4s.reliability.CircuitBreakerConfig
import org.llm4s.reliability.RateLimitConfig

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

  test("LlmLoader.loadSettings reads the llm4s block of application.conf") {
    LlmLoader.loadSettings[IO]().map { settings =>
      assertEquals(settings.defaultProviderName().isRight, true)
    }
  }

  test("LlmReliabilitySettings.load reads the reliability block of application.conf") {
    LlmReliabilitySettings.load[IO]().map { reliability =>
      assertEquals(reliability.enabled, true)
      assertEquals(reliability.deadline, Some(10.seconds))
      assertEquals(reliability.retryPolicy.maxAttempts, 2)
      assertEquals(reliability.circuitBreaker, CircuitBreakerConfig(5, 30.seconds, 2))
      assertEquals(reliability.rateLimit, RateLimitConfig(false, 60, 60))
    }
  }

  /** An llm4s config with a single provider, to keep the loading tests independent of the environment. */
  private def llmConfig(
      defaultProvider: String,
      apiKey: Option[String],
      retryStrategy: String = "fixed"
  ): TypesafeConfig =
    ConfigFactory.parseString(
      s"""llm4s {
         |  defaultProvider = "$defaultProvider"
         |  reliability {
         |    enabled = true
         |    deadline = 10s
         |    retry {
         |      strategy = "$retryStrategy"
         |      max-attempts = 2
         |      delay = 200ms
         |      max-delay = 5s
         |    }
         |    circuit-breaker {
         |      failure-threshold = 5
         |      recovery-timeout = 30s
         |      success-threshold = 2
         |    }
         |    rate-limit {
         |      enabled = false
         |      requests-per-minute = 60
         |      burst-capacity = 60
         |    }
         |  }
         |  providers {
         |    gemini-free {
         |      provider = "gemini"
         |      model    = "gemini-flash-lite-latest"
         |      ${apiKey.fold("")(k => s"apiKey = \"$k\"")}
         |    }
         |  }
         |}""".stripMargin
    )

  test("LlmEngine.dependencies fails with an LLMLoadingError when the default provider is not configured") {
    LlmEngine
      .dependencies[IO](llmConfig(defaultProvider = "undefined", apiKey = Some("stub-key")))
      .use_
      .intercept[LLMLoadingError]
  }

  test("LlmEngine.dependencies fails with an LLMLoadingError when a provider has no apiKey") {
    LlmEngine
      .dependencies[IO](llmConfig(defaultProvider = "gemini-free", apiKey = None))
      .use_
      .intercept[LLMLoadingError]
  }

  test("LlmEngine.dependencies fails with an LLMLoadingError on an invalid reliability config") {
    LlmEngine
      .dependencies[IO](llmConfig(defaultProvider = "gemini-free", apiKey = Some("stub-key"), retryStrategy = "bogus"))
      .use_
      .intercept[LLMLoadingError]
  }

  test("LlmEngine.dependencies loads the llm4s wiring and builds an engine") {
    // The reply path is covered by LlmEngineSpec with a stub responder: asking a real provider here would make the
    // test depend on the network and on a valid API key.
    LlmEngine
      .dependencies[IO](llmConfig(defaultProvider = "gemini-free", apiKey = Some("stub-key")))
      .flatMap(_.build(Persona("a persona")))
      .use_
      .void
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
