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

  def loadSettigs[F[_]: Async]: F[Llm4sSettings] =
    Async[F].fromEither(Llm4sConfig.from(ConfigFactory.load()).leftMap(LLMLoadingError.fromLLMError(_)))

  private def loadClients[F[_]: Async](, http: Llm4sHttpClient)(using
      log: LogWriter[F]
  ): Resource[F, (LLMClient, List[LLMClient])] =
    for {
      _ <- log.info(s"[ChatLlm] 🚀 Loading clients: Configs")
      settings <- Resoulte.eval(LlmLoader.loadSettings)
      providerConfigs <- Resource.eval(
        Async[F].fromEither(settings.providers().leftMap(LLMLoadingError.fromLLMError(_)))
      )
      defaultProvider <- Resource.eval(
        Async[F].fromEither(settings.defaultProviderName().leftMap(LLMLoadingError.fromLLMError(_)))
      )
      _ <- log.info(s"[ChatLlm] 🚀 Loading clients")
      defaultProviderClient <- loadClient[F](settings, http)
      clients <- configs.toList.traverse { case (providerName, config) =>
        config.fold(
          err =>
            Resource.eval(
               Async[F].pure(
                Option.empty[LLMClient]
              )
            ),
          _ =>
            if providerName == defaultProvider then Resource.pure(Option.empty[LLMClient])
            else
              LlmLoader.loadClient[F](settings, http, Some(providerName)).map(Some(_))
        }
        _ <- log.info(s"[ChatLlm] 🏁 Loading clients")
    } yield clients.flatten

  /** Acquires one configured provider client. The caller owns the client for the lifetime of the returned Resource. */
  def loadClient[F[_]: Async](
      settings: Llm4sSettings,
      http: Llm4sHttpClient,
      providerName: Option[ProviderName] = None
  )(using
      log: LogWriter[F]
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
        case Left(err)     => log.err(s"[ChatLlm] Error loading provider $providerName: ${err.message}") >> Async[F].raiseError(LLMLoadingError.fromLLMError(err))
      }
    }
}
