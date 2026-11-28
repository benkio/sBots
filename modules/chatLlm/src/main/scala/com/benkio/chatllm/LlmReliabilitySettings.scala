package com.benkio.chatllm

import cats.effect.Async
import cats.syntax.either.*
import cats.syntax.flatMap.*
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.llm4s.reliability.CircuitBreakerConfig
import org.llm4s.reliability.RateLimitConfig
import org.llm4s.reliability.ReliabilityConfig
import org.llm4s.reliability.RetryPolicy
import pureconfig.*
import pureconfig.ConfigSource

import scala.concurrent.duration.*

final case class RetrySettings(
    strategy: String,
    maxAttempts: Int,
    delay: FiniteDuration,
    maxDelay: FiniteDuration
) derives ConfigReader

final case class CircuitBreakerSettings(
    failureThreshold: Int,
    recoveryTimeout: FiniteDuration,
    successThreshold: Int
) derives ConfigReader

final case class RateLimitSettings(
    enabled: Boolean,
    requestsPerMinute: Int,
    burstCapacity: Int
) derives ConfigReader

/** The llm4s reliability knobs, read from `llm4s.reliability` in the application configuration. Every value is set in
  * HOCON: changing a retry, deadline or rate-limit value never requires a code change.
  */
final case class LlmReliabilitySettings(
    enabled: Boolean,
    deadline: Option[FiniteDuration],
    retry: RetrySettings,
    circuitBreaker: CircuitBreakerSettings,
    rateLimit: RateLimitSettings
) derives ConfigReader

object LlmReliabilitySettings {

  /** Loads and validates the reliability block. A malformed or invalid block fails at startup with an
    * [[LLMLoadingError]] naming the offending field.
    */
  def load[F[_]: Async](config: => Config = ConfigFactory.load()): F[ReliabilityConfig] =
    Async[F].blocking(parse(config)).flatMap(Async[F].fromEither(_))

  def parse(config: Config): Either[LLMLoadingError, ReliabilityConfig] =
    ConfigSource
      .fromConfig(config)
      .at("llm4s.reliability")
      .load[LlmReliabilitySettings]
      .leftMap(errors => LLMLoadingError(s"invalid llm4s.reliability: ${errors.prettyPrint()}"))
      .flatMap(toReliabilityConfig)

  def toReliabilityConfig(settings: LlmReliabilitySettings): Either[LLMLoadingError, ReliabilityConfig] = {

    val validStrategies: List[String] = List("none", "fixed", "linear", "exponential")

    def invalid(field: String, reason: String) =
      LLMLoadingError(s"invalid llm4s.reliability.$field: $reason")

    def retryPolicy: Either[LLMLoadingError, RetryPolicy] = {
      val buildSteps: List[(LlmReliabilitySettings => Boolean, Either[LLMLoadingError, RetryPolicy])] =
        List(
          (
            _.deadline.exists(_ <= Duration.Zero),
            Left(invalid("deadline", s"must be positive, was ${settings.deadline.getOrElse(Duration.Zero)}"))
          ),
          (
            settings => settings.rateLimit.enabled && settings.rateLimit.requestsPerMinute < 1,
            Left(
              invalid(
                "rateLimit.requests-per-minute",
                s"must be at least 1 when rate limiting is enabled, was ${settings.rateLimit.requestsPerMinute}"
              )
            )
          ),
          (
            _.rateLimit.burstCapacity < 1,
            Left(
              invalid(
                "rateLimit.burstCapacity",
                s"must be at least 1, was ${settings.rateLimit.burstCapacity}"
              )
            )
          ),
          (
            _.retry.maxAttempts < 1,
            Left(invalid("retry.max-attempts", s"must be at least 1, was ${settings.retry.maxAttempts}"))
          ),
          (
            _.retry.delay < Duration.Zero,
            Left(invalid("retry.delay", s"must not be negative, was ${settings.retry.delay}"))
          ),
          (
            settings => !validStrategies.contains(settings.retry.strategy),
            Left(
              invalid(
                "retry.strategy",
                s"""unknown strategy '${settings.retry.strategy}' (expected ${validStrategies.mkString(", ")})"""
              )
            )
          ),
          (
            settings => settings.retry.strategy == "exponential" && settings.retry.maxDelay < settings.retry.delay,
            Left(
              invalid(
                "retry.max-delay",
                s"must be at least retry.delay (${settings.retry.delay}), was ${settings.retry.maxDelay}"
              )
            )
          ),
          (_.retry.strategy == "none", Right(RetryPolicy.noRetry)),
          (
            _.retry.strategy == "fixed",
            Right(RetryPolicy.fixedDelay(settings.retry.maxAttempts, settings.retry.delay))
          ),
          (
            _.retry.strategy == "linear",
            Right(RetryPolicy.linearBackoff(settings.retry.maxAttempts, settings.retry.delay))
          ),
          (
            _.retry.strategy == "exponential",
            Right(
              RetryPolicy.exponentialBackoff(settings.retry.maxAttempts, settings.retry.delay, settings.retry.maxDelay)
            )
          )
        )
      buildSteps
        .collectFirst { case (condition, result) if condition(settings) == true => result }
        .getOrElse(
          Left(
            invalid(
              "retry.strategy",
              s"""unknown strategy '${settings.retry.strategy}' (expected ${validStrategies.mkString(", ")})"""
            )
          )
        )
    }

    for {
      policy <- retryPolicy
      reliability = ReliabilityConfig(
        retryPolicy = policy,
        circuitBreaker = CircuitBreakerConfig(
          failureThreshold = settings.circuitBreaker.failureThreshold,
          recoveryTimeout = settings.circuitBreaker.recoveryTimeout,
          successThreshold = settings.circuitBreaker.successThreshold
        ),
        rateLimit = RateLimitConfig(
          enabled = settings.rateLimit.enabled,
          requestsPerMinute = settings.rateLimit.requestsPerMinute,
          burstCapacity = settings.rateLimit.burstCapacity
        ),
        deadline = settings.deadline,
        enabled = settings.enabled
      )
      _ <- reliability.validate.leftMap(error => LLMLoadingError(s"invalid llm4s.reliability: ${error.message}"))
    } yield reliability
  }
}
