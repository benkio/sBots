package com.benkio.chatllm.llm

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.Notifier
import org.http4s.client.Client

/** Factory for the chat LLM client: the configured primary provider and its fallback are wired once, at startup. The
  * returned client also owns the one limiter shared by all the bots, with separate quotas for the two providers.
  */
object LlmClients {

  /** The standalone loader keeps the config in `chatLlm`: callers that already have it use the overload below. */
  def build[F[_]: Async](client: Client[F]): F[LlmClientApi[F]] =
    for {
      config <- LlmConfig.load[F]
      llm    <- build(client, config)
    } yield llm

  def build[F[_]: Async](client: Client[F], config: LlmConfig): F[LlmClientApi[F]] =
    build(client = client, config = config, notifier = Notifier.noop[F])

  def build[F[_]: Async](
      client: Client[F],
      config: LlmConfig,
      notifier: Notifier[F]
  ): F[LlmClientApi[F]] =
    LlmConfig.validate(config) match {
      case Left(err) => Async[F].raiseError(new IllegalArgumentException(err))
      case Right(()) =>
        for {
          limiter    <- LlmRateLimiter[F](LlmConfig.quotaFor(config))
          underlying <- buildProviderClient(client = client, config = config, limiter = limiter)
        } yield LimitedLlmClient[F](underlying, config, limiter, notifier)
    }

  private def buildProviderClient[F[_]: Async](
      client: Client[F],
      config: LlmConfig,
      limiter: LlmRateLimiter[F]
  ): F[LlmClientApi[F]] =
    LlmConfig.primary(config) match {
      case Left(err)              => Async[F].raiseError(new IllegalArgumentException(err))
      case Right(primaryProvider) =>
        val maxOutputTokens = config.maxOutputTokens
        primaryProvider match {
          case LlmProvider.Gemini =>
            for {
              primary  <- GeminiClient[F](client, config.gemini, maxOutputTokens)
              fallback <- OpenRouterClient[F](client, config.openrouter, maxOutputTokens)
            } yield FallbackLlmClient[F](primary, fallback, config.timeout, limiter)
          case LlmProvider.OpenRouter =>
            for {
              primary  <- OpenRouterClient[F](client, config.openrouter, maxOutputTokens)
              fallback <- GeminiClient[F](client, config.gemini, maxOutputTokens)
            } yield FallbackLlmClient[F](primary, fallback, config.timeout, limiter)
        }
    }
}
