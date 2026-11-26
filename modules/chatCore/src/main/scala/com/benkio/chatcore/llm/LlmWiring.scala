package com.benkio.chatcore.llm

import cats.Applicative

/** How `main` hands the LLM to the bots: the `/llm` settings, built per bot from the bot's own transactor and id.
  * chatCore owns the shape, chatLlm supplies the implementation, so this module never depends on it.
  */
final case class LlmWiring[F[_]](settings: LlmSettings.Factory[F])

object LlmWiring {

  def noop[F[_]: Applicative]: LlmWiring[F] = LlmWiring(LlmSettings.noopFactory[F])
}
