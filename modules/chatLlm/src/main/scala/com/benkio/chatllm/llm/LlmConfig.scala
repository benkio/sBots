package com.benkio.chatllm.llm

import cats.effect.Async
import pureconfig.*

import scala.concurrent.duration.*

/** A secret value that redacts itself in logs and toString. */
final case class Sensitive(value: String) {
  override def toString: String = "***"
}

object Sensitive {
  given ConfigReader[Sensitive] = ConfigReader.stringConfigReader.map(Sensitive(_))
}

/** Connection details for one provider. The endpoint is a full URI rather than a host, so both the public APIs and a
  * local stub can be configured with the same shape.
  */
final case class ProviderConnection(
    endpoint: String,
    model: Option[String] = None,
    apiKey: Option[Sensitive] = None
) derives ConfigReader

/** The client-side budget shared by every bot and every provider. Both windows slide: a request that is more than a
  * minute (or a day) old no longer counts. One limiter guards the whole LLM feature, so a fallback attempt does not
  * consume a second slot.
  */
final case class LlmBudgetConfig(
    maxPerMinute: Int = 20,
    maxPerDay: Int = 50
) derives ConfigReader

/** The section `main.llm` of the application configuration. */
final case class LlmConfig(
    gemini: ProviderConnection,
    openrouter: ProviderConnection,
    primaryProvider: Option[String] = None,
    timeout: FiniteDuration = LlmConfig.defaultTimeout,
    maxInputTokens: Int = LlmConfig.defaultMaxInputTokens,
    maxOutputTokens: Int = LlmConfig.defaultMaxOutputTokens,
    budget: LlmBudgetConfig = LlmBudgetConfig()
) derives ConfigReader

object LlmConfig {

  val defaultTimeout: FiniteDuration = 10.seconds
  val defaultMaxInputTokens: Int     = 4096
  val defaultMaxOutputTokens: Int    = 512
  val defaultMaxPerMinute: Int       = 20
  val defaultMaxPerDay: Int          = 50

  /** The primary provider is only used to label completions: the budget is shared across providers too. */
  def primary(config: LlmConfig): Either[String, LlmProvider] =
    LlmProvider.fromString(config.primaryProvider)

  /** The fallback can call either provider, so both keys are needed before any request is admitted. Without them the
    * client fails completions locally instead of sending anonymous requests that the providers reject.
    */
  def credentialsComplete(config: LlmConfig): Boolean =
    config.gemini.apiKey.isDefined && config.openrouter.apiKey.isDefined

  /** Invalid tuning makes the startup fail instead of silently disabling the limits. */
  def validate(config: LlmConfig): Either[String, Unit] = {
    val problems = List(
      if config.timeout.length <= 0 then Some("llm.timeout must be positive") else None,
      if config.maxInputTokens <= 0 then Some("llm.max-input-tokens must be positive") else None,
      if config.maxOutputTokens <= 0 then Some("llm.max-output-tokens must be positive") else None,
      if config.budget.maxPerMinute <= 0 then Some("llm.budget.max-per-minute must be positive") else None,
      if config.budget.maxPerDay <= 0 then Some("llm.budget.max-per-day must be positive") else None
    ).flatten
    Either.cond(problems.isEmpty, (), problems.mkString(", "))
  }

  def load[F[_]: Async]: F[LlmConfig] =
    ConfigSource.default
      .at("main.llm")
      .load[LlmConfig]
      .fold(
        err => Async[F].raiseError[LlmConfig](new RuntimeException(err.prettyPrint())),
        config =>
          validate(config) match {
            case Left(err) => Async[F].raiseError[LlmConfig](new IllegalArgumentException(err))
            case Right(()) => Async[F].pure(config)
          }
      )
}
