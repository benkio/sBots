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

    def invalid(field: String, reason: String) =
      LLMLoadingError(s"invalid llm4s.reliability.$field: $reason")

    def retryPolicy: Either[LLMLoadingError, RetryPolicy] =
      if settings.retry.maxAttempts < 1 then
        Left(invalid("retry.max-attempts", s"must be at least 1, was ${settings.retry.maxAttempts}"))
      else if settings.retry.delay < Duration.Zero then
        Left(invalid("retry.delay", s"must not be negative, was ${settings.retry.delay}"))
      else
        settings.retry.strategy match {
          case "none"        => Right(RetryPolicy.noRetry)
          case "fixed"       => Right(RetryPolicy.fixedDelay(settings.retry.maxAttempts, settings.retry.delay))
          case "linear"      => Right(RetryPolicy.linearBackoff(settings.retry.maxAttempts, settings.retry.delay))
          case "exponential" =>
            Either.cond(
              settings.retry.maxDelay >= settings.retry.delay,
              RetryPolicy.exponentialBackoff(settings.retry.maxAttempts, settings.retry.delay, settings.retry.maxDelay),
              invalid("retry.max-delay", s"must be at least retry.delay (${settings.retry.delay}), was ${settings.retry.maxDelay}")
            )
          case other =>
            Left(invalid("retry.strategy", s"unknown strategy '$other' (expected none, fixed, linear, exponential)"))
        }

    for {
      policy <- retryPolicy
      _      <- Either.cond(
        settings.deadline.forall(_ > Duration.Zero),
        (),
        invalid("deadline", s"must be positive, was ${settings.deadline.getOrElse(Duration.Zero)}")
      )
      _ <- Either.cond(
        !settings.rateLimit.enabled || settings.rateLimit.requestsPerMinute >= 1,
        (),
        invalid(
          "rateLimit.requests-per-minute",
          s"must be at least 1 when rate limiting is enabled, was ${settings.rateLimit.requestsPerMinute}"
        )
      )
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
