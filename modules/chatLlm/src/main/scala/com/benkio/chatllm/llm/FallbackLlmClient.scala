package com.benkio.chatllm.llm

import cats.effect.implicits.*
import cats.effect.Temporal
import cats.syntax.all.*

import java.util.concurrent.TimeoutException
import scala.concurrent.duration.FiniteDuration

/** The only component that knows both providers. Each provider call is admitted by its own quota: the primary is asked
  * first when it has budget; when the primary is exhausted the fallback is asked first. A retryable primary failure is
  * followed by the fallback only when the fallback has budget. When neither provider can be called no request is made
  * and the completion is silent (the notification is the responsibility of `LimitedLlmClient`). The completion rules
  * decide which responses are retryable, so no third attempt is ever made. The timeout is applied to each provider
  * call: a timeout becomes a recorded provider result, while unexpected errors still propagate to the caller.
  */
object FallbackLlmClient {

  def apply[F[_]: Temporal](
      primary: LlmProviderClientApi[F],
      fallback: LlmProviderClientApi[F],
      timeout: FiniteDuration,
      limiter: LlmRateLimiter[F]
  ): LlmClientApi[F] =
    new FallbackLlmClientImpl[F](primary, fallback, timeout, limiter)

  private final class FallbackLlmClientImpl[F[_]: Temporal](
      primary: LlmProviderClientApi[F],
      fallback: LlmProviderClientApi[F],
      timeout: FiniteDuration,
      limiter: LlmRateLimiter[F]
  ) extends LlmClientApi[F] {

    override def complete(prompt: String): F[Completion] =
      limiter.acquire(primary.provider).flatMap {
        case RateLimitDecision.Allowed =>
          ask(completion = Completion.started(prompt, primary.provider), client = primary, prompt = prompt)
            .flatMap(afterPrimary(_, prompt))
        case RateLimitDecision.Exhausted(_) =>
          // Primary quota exhausted: skip it and ask the fallback, if it still has budget.
          limiter.acquire(fallback.provider).flatMap {
            case RateLimitDecision.Allowed =>
              ask(
                completion = Completion.startedWith(prompt, primary.provider, fallback.provider),
                client = fallback,
                prompt = prompt
              )
            case RateLimitDecision.Exhausted(_) =>
              // Neither provider can be called: no request, no reply.
              Completion.notRequested(prompt, primary.provider, CompletionStatus.Silent).pure[F]
          }
      }

    /** The primary has answered: retry against the fallback only when the completion is still pending and the fallback
      * has quota. An exhausted fallback leaves a failed completion recording only the call that really happened.
      */
    private def afterPrimary(completion: Completion, prompt: String): F[Completion] =
      completion.status match {
        case CompletionStatus.Pending =>
          limiter.acquire(fallback.provider).flatMap {
            case RateLimitDecision.Allowed      => ask(completion = completion, client = fallback, prompt = prompt)
            case RateLimitDecision.Exhausted(_) => Completion.failUnresolved(completion).pure[F]
          }
        case _ => completion.pure[F]
      }

    private def ask(completion: Completion, client: LlmProviderClientApi[F], prompt: String): F[Completion] =
      client.generate(prompt).timeout(timeout).attempt.flatMap {
        case Right(response) =>
          Completion.acceptResponse(completion, client.provider, response).pure[F]
        case Left(_: TimeoutException) =>
          // Terminal for this provider, but kept in the completion: the fallback decision still applies.
          Completion.acceptResponse(completion, client.provider, ProviderResponse.timeout).pure[F]
        case Left(error) =>
          Temporal[F].raiseError(error)
      }
  }
}
