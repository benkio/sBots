package com.benkio.chatllm

import cats.effect.IO
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import munit.CatsEffectSuite
import org.llm4s.error.ProcessingError
import org.llm4s.reliability.CircuitBreakerConfig
import org.llm4s.reliability.RateLimitConfig

import scala.concurrent.duration.*

class LlmReliabilitySettingsSpec extends CatsEffectSuite {

  private val error = ProcessingError("test", "test")

  private def reliabilityBody(
      enabled: Boolean = true,
      deadline: String = "10s",
      strategy: String = "fixed",
      maxAttempts: Int = 2,
      delay: String = "200ms",
      maxDelay: String = "5s",
      failureThreshold: Int = 5,
      recoveryTimeout: String = "30s",
      successThreshold: Int = 2,
      rateEnabled: Boolean = false,
      requestsPerMinute: Int = 60,
      burstCapacity: Int = 60
  ): String =
    s"""enabled = $enabled
       |deadline = $deadline
       |retry {
       |  strategy = "$strategy"
       |  max-attempts = $maxAttempts
       |  delay = $delay
       |  max-delay = $maxDelay
       |}
       |circuit-breaker {
       |  failure-threshold = $failureThreshold
       |  recovery-timeout = $recoveryTimeout
       |  success-threshold = $successThreshold
       |}
       |rate-limit {
       |  enabled = $rateEnabled
       |  requests-per-minute = $requestsPerMinute
       |  burst-capacity = $burstCapacity
       |}""".stripMargin

  private def config(body: String = reliabilityBody()): Config =
    ConfigFactory.parseString(s"llm4s { reliability { $body } }")

  test("maps the configured knobs to the llm4s reliability config") {
    val parsed = LlmReliabilitySettings.parse(config()).toOption.get
    assertEquals(parsed.enabled, true)
    assertEquals(parsed.deadline, Some(10.seconds))
    assertEquals(parsed.retryPolicy.maxAttempts, 2)
    assertEquals(parsed.retryPolicy.delayFor(1, error), 200.millis)
    assertEquals(parsed.circuitBreaker, CircuitBreakerConfig(5, 30.seconds, 2))
    assertEquals(parsed.rateLimit, RateLimitConfig(enabled = false, requestsPerMinute = 60, burstCapacity = 60))
  }

  test("maps every retry strategy") {
    val expected = List(
      ("none", 1, 0.millis),
      ("fixed", 2, 200.millis),
      ("linear", 2, 200.millis),
      ("exponential", 2, 200.millis)
    )
    expected.foreach { (strategy, maxAttempts, firstRetryDelay) =>
      val parsed = LlmReliabilitySettings.parse(config(reliabilityBody(strategy = strategy))).toOption.get
      assertEquals(parsed.retryPolicy.maxAttempts, maxAttempts, strategy)
      assertEquals(parsed.retryPolicy.delayFor(1, error), firstRetryDelay, strategy)
    }
  }

  test("maps an enabled local rate limit") {
    val parsed = LlmReliabilitySettings
      .parse(config(reliabilityBody(rateEnabled = true, requestsPerMinute = 30, burstCapacity = 5)))
      .toOption
      .get
    assertEquals(parsed.rateLimit, RateLimitConfig(enabled = true, requestsPerMinute = 30, burstCapacity = 5))
  }

  test("load reads the same block from a Config") {
    LlmReliabilitySettings.load[IO](config()).map { reliability =>
      assertEquals(reliability.deadline, Some(10.seconds))
    }
  }

  test("invalid values fail with the name of the offending field") {
    val invalid = List(
      "retry.strategy"                -> reliabilityBody(strategy = "bogus"),
      "retry.max-attempts"            -> reliabilityBody(maxAttempts = 0),
      "retry.delay"                   -> reliabilityBody(delay = "-1s"),
      "retry.max-delay"               -> reliabilityBody(strategy = "exponential", maxDelay = "1ms"),
      "deadline"                      -> reliabilityBody(deadline = "0ms"),
      "circuitBreaker"                -> reliabilityBody(failureThreshold = 0),
      "rateLimit.requests-per-minute" -> reliabilityBody(rateEnabled = true, requestsPerMinute = 0),
      "rateLimit.burstCapacity"       -> reliabilityBody(rateEnabled = true, burstCapacity = 0)
    )
    invalid.foreach { (field, body) =>
      val failure = LlmReliabilitySettings.parse(config(body)).left.toOption.get
      assert(failure.getMessage.contains(field), s"$field missing from ${failure.getMessage}")
    }
  }

  test("a missing reliability block fails at startup") {
    val failure = LlmReliabilitySettings.parse(ConfigFactory.parseString("llm4s { defaultProvider = \"x\" }"))
    assert(failure.isLeft)
    assert(failure.left.toOption.get.getMessage.contains("llm4s.reliability"))
  }
}
