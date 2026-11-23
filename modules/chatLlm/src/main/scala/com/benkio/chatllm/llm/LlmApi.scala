package com.benkio.chatllm.llm

/** The public surface used by `chatCore`: one completion for one prompt, no provider details. */
trait LlmClientApi[F[_]] {
  def complete(prompt: String): F[Completion]
}

/** The surface implemented by one provider client: it only knows its own provider and returns a provider response. */
trait LlmProviderClientApi[F[_]] {
  def provider: LlmProvider
  def generate(value: String): F[ProviderResponse]
}
