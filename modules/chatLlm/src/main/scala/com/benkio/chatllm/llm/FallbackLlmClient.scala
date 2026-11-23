package com.benkio.chatllm.llm

import cats.syntax.all.*
import cats.Monad

/** The only component that knows both providers. It tries the primary first, then the fallback when the completion is
  * still pending. The completion rules decide which responses are retryable, so no third attempt is ever made.
  */
object FallbackLlmClient {

  def apply[F[_]: Monad](
      primary: LlmProviderClientApi[F],
      fallback: LlmProviderClientApi[F]
  ): LlmClientApi[F] =
    new FallbackLlmClientImpl[F](primary, fallback)

  private final class FallbackLlmClientImpl[F[_]: Monad](
      primary: LlmProviderClientApi[F],
      fallback: LlmProviderClientApi[F]
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
      client.generate(prompt).map(response => Completion.acceptResponse(completion, client.provider, response))
  }
}
