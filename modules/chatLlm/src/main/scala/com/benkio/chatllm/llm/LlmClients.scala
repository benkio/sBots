package com.benkio.chatllm.llm

import cats.effect.Async
import cats.syntax.all.*
import org.http4s.client.Client

/** Factory for the chat LLM client: the configured primary provider and its fallback are wired once, at startup. */
object LlmClients {

  def build[F[_]: Async](client: Client[F]): F[LlmClientApi[F]] =
    for {
      config <- LlmConfig.load[F]
      llm    <- build(client, config)
    } yield llm

  def build[F[_]: Async](client: Client[F], config: LlmConfig): F[LlmClientApi[F]] =
    LlmConfig.primary(config) match {
      case Left(err)              => Async[F].raiseError(new IllegalArgumentException(err))
      case Right(primaryProvider) =>
        primaryProvider match {
          case LlmProvider.Gemini =>
            for {
              primary  <- GeminiClient[F](client, config.gemini)
              fallback <- OpenRouterClient[F](client, config.openrouter)
            } yield FallbackLlmClient[F](primary, fallback)
          case LlmProvider.OpenRouter =>
            for {
              primary  <- OpenRouterClient[F](client, config.openrouter)
              fallback <- GeminiClient[F](client, config.gemini)
            } yield FallbackLlmClient[F](primary, fallback)
        }
    }
}
