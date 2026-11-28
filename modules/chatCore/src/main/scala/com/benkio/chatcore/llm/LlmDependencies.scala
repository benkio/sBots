package com.benkio.chatcore.llm

import cats.effect.Resource
import cats.Applicative
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.LlmEngine

/** How `main` hands the LLM to the bots. `build` is supplied by chatLlm through `main` (with the shared startup clients
  * already loaded); `BotCoreSetup` calls it once the bot's own `DBLayer` is ready and its [[Persona]] is loaded.
  * chatCore owns the shape, so this module never depends on the LLM library.
  */
final case class LlmDependencies[F[_]](
    build: (DBLayer[F], Persona) => Resource[F, LlmEngine[F]]
)

object LlmDependencies {

  /** The engine never answers. */
  def noop[F[_]: Applicative]: LlmDependencies[F] =
    LlmDependencies((_, _) => Resource.pure(LlmEngine.noop[F]))
}
