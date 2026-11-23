package com.benkio.chatllm.llm

import cats.effect.Async
import pureconfig.*

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

/** The provider switch is a config change: `main.llm.primary-provider`. */
final case class LlmConfig(
    gemini: ProviderConnection,
    openrouter: ProviderConnection,
    primaryProvider: Option[String] = None
) derives ConfigReader

object LlmConfig {

  def primary(config: LlmConfig): Either[String, LlmProvider] =
    LlmProvider.fromString(config.primaryProvider)

  def load[F[_]: Async]: F[LlmConfig] =
    ConfigSource.default
      .at("main.llm")
      .load[LlmConfig]
      .fold(
        err => Async[F].raiseError[LlmConfig](new RuntimeException(err.prettyPrint())),
        config => Async[F].pure(config)
      )
}
