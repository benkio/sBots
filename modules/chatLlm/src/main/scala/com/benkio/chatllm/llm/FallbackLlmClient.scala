package com.benkio.chatllm.llm

import cats.effect.implicits.*
import cats.effect.Temporal
import cats.syntax.all.*

import java.util.concurrent.TimeoutException
import scala.concurrent.duration.FiniteDuration

/** The only component that knows both providers. It tries the primary first, then the fallback when the completion is
  * still pending. The completion rules decide which responses are retryable, so no third attempt is ever made. The
  * timeout is applied to each provider call: a timeout becomes a recorded provider result, so the attempts that led to
  * it are never lost, while unexpected errors still propagate to the caller.
  */
object FallbackLlmClient {

  def apply[F[_]: Temporal](
      primary: LlmProviderClientApi[F],
      fallback: LlmProviderClientApi[F],
      timeout: FiniteDuration
  ): LlmClientApi[F] =
    new FallbackLlmClientImpl[F](primary, fallback, timeout)

  private final class FallbackLlmClientImpl[F[_]: Temporal](
      primary: LlmProviderClientApi[F],
      fallback: LlmProviderClientApi[F],
      timeout: FiniteDuration
  ) extends LlmClientApi[F] {

    override def complete(prompt: String): F[Completion] =
      for {
        started    <- Completion.started(prompt, primary.provider).pure[F]
        afterFirst <- ask(started, primary, prompt)
        result     <- afterFirst.status match {
          case CompletionStatus.Pending => ask(afterFirst, fallback, prompt)
          case _                        => afterFirst.pure[F]
        }
      } yield result

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
