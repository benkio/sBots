package com.benkio.chatcore.llm

import cats.Applicative
import com.benkio.chatcore.model.SBotInfo.SBotId
import doobie.util.transactor.Transactor

import scala.concurrent.duration.*

/** What a bot's engine needs from the LLM: the responder, and the time it has to answer. */
final case class LlmEngine[F[_]](responder: LlmResponder[F], timeout: FiniteDuration)

object LlmEngine {

  val defaultTimeout: FiniteDuration = 10.seconds

  /** The feature unconfigured: the responder never answers. */
  def noop[F[_]: Applicative]: LlmEngine[F] = LlmEngine(LlmResponder.noop[F], defaultTimeout)
}

/** How `main` hands the LLM to the bots: the `/llm` settings and the engine, both built per bot from the bot's own
  * transactor and id. chatCore owns the shape, chatLlm supplies the implementation, so this module never depends on it.
  * The engine factory never fails the bot: when the LLM cannot be offered (missing keys or persona) it returns
  * [[LlmEngine.noop]].
  */
final case class LlmWiring[F[_]](
    settings: LlmSettings.Factory[F],
    engine: LlmWiring.EngineFactory[F]
)

object LlmWiring {
  type EngineFactory[F[_]] = (Transactor[F], SBotId) => F[LlmEngine[F]]

  def noop[F[_]: Applicative]: LlmWiring[F] =
    LlmWiring(LlmSettings.noopFactory[F], (_, _) => Applicative[F].pure(LlmEngine.noop[F]))
}
