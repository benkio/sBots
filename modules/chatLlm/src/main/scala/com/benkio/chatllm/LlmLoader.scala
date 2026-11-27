package com.benkio.chatllm

import cats.effect.Async
import cats.effect.Resource
import cats.syntax.either.*
import com.typesafe.config.ConfigFactory
import org.llm4s.config.Llm4sConfig
import org.llm4s.config.Llm4sSettings
import org.llm4s.error.LLMError
import org.llm4s.http.Llm4sHttpClient
import org.llm4s.llmconnect.LLMClient
import org.llm4s.llmconnect.LLMConnect
import org.llm4s.llmconnect.LlmClientOptions
import org.llm4s.types.ProviderModelTypes.ProviderName

final case class LLMLoadingError(message: String) extends Throwable(s"Error during component loading: $message")

object LLMLoadingError {
  def fromLLMError(err: LLMError): LLMLoadingError =
    LLMLoadingError(err.message)
}

/** Loads the llm4s settings and clients from the application configuration. `Llm4sSettings.of` is private to llm4s, so
  * this is the public edge for loading them.
  */
object LlmLoader {

  def load[F[_]: Async]: F[Llm4sSettings] =
    Async[F].fromEither(Llm4sConfig.from(ConfigFactory.load()).leftMap(LLMLoadingError.fromLLMError(_)))

  /** Acquires one configured provider client. The caller owns the client for the lifetime of the returned Resource. */
  def loadClient[F[_]: Async](
      settings: Llm4sSettings,
      http: Llm4sHttpClient,
      providerName: Option[ProviderName] = None
  ): Resource[F, LLMClient] =
    Resource.fromAutoCloseable {
      Async[F].flatMap(
        Async[F].blocking {
          for {
            config   <- providerName.fold(settings.defaultProvider())(settings.provider(_))
            registry <- settings.modelRegistryService()
            client   <- LLMConnect.getClient(config, LlmClientOptions.default.withHttpClient(http))(using
              registry,
              org.llm4s.llmconnect.spi.ProviderRegistry.default
            )
          } yield client
        }
      ) {
        case Right(client) => Async[F].pure(client)
        case Left(err)     => Async[F].raiseError(LLMLoadingError.fromLLMError(err))
      }
    }
}
