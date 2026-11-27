package com.benkio.chatcore.llm

import cats.Applicative

/** How `main` hands the LLM to the bots: the `/llm` settings, built per bot from the bot's own transactor and id, and
  * the responder the engine calls at its LLM entry point. chatCore owns the shape, chatLlm supplies the implementation,
  * so this module never depends on it.
  */
final case class LlmWiring[F[_]](settings: LlmSettings.Factory[F], responder: LlmResponder[F])

object LlmWiring {

  def noop[F[_]: Applicative]: LlmWiring[F] =
    LlmWiring(settings = LlmSettings.noopFactory[F], responder = LlmResponder.noop[F])
}
