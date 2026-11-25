package com.benkio.chatllm.llm

/** The public surface used by `chatCore`: one completion for one prompt, no provider details. */
trait LlmClientApi[F[_]] {
  def complete(prompt: String): F[Completion]

  /** Like `complete`, and reports the completion after every finished provider attempt, so a caller that is cancelled
    * mid-way (the engine's timeout) still knows the attempts and timings completed before the cancel. The default
    * reports nothing.
    */
  def complete(prompt: String, progress: Completion => F[Unit]): F[Completion] = {
    val _ = progress
    complete(prompt)
  }
}

/** The surface implemented by one provider client: it only knows its own provider and returns a provider response. */
trait LlmProviderClientApi[F[_]] {
  def provider: LlmProvider
  def generate(value: String): F[ProviderResponse]
}
