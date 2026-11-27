package com.benkio.chatllm

import cats.effect.Async
import cats.syntax.either.*
import com.typesafe.config.ConfigFactory
import org.llm4s.config.Llm4sConfig
import org.llm4s.config.Llm4sSettings

/** Loads the llm4s settings from the application configuration. `Llm4sSettings.of` is private to llm4s, so this is the
  * public edge for loading them.
  */
object LlmSettingsLoader {

  def load[F[_]: Async]: F[Llm4sSettings] =
    Async[F].fromEither(
      Llm4sConfig
        .from(ConfigFactory.load())
        .leftMap(err => new RuntimeException(err.message))
    )
}
