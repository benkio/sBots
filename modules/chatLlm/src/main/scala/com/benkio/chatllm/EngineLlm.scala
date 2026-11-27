package com.benkio.chatllm

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.EngineLlm as CoreEngineLlm
import com.benkio.chatcore.PlatformAdapter
import com.benkio.chatcore.UsageStats.Outcome
import com.benkio.chatcore.llm.LlmResponder
import com.benkio.chatcore.model.Message

/** The chatLlm implementation of the LLM section of the bot engine. It owns the responder built from the startup
  * clients and sends an answer through the delivery the engine provides. Until the LLM replies land the responder never
  * answers, so the engine keeps the outcome of the reactive part.
  */
object EngineLlm {

  def factory[F[_]: Async](responder: LlmResponder[F]): CoreEngineLlm.Factory[F] =
    new CoreEngineLlm.Factory[F] {
      override def build(deliver: CoreEngineLlm.Deliver[F]): CoreEngineLlm[F] =
        EngineLlmImpl[F](responder, deliver)
    }

  private final case class EngineLlmImpl[F[_]: Async](
      responder: LlmResponder[F],
      deliver: CoreEngineLlm.Deliver[F]
  ) extends CoreEngineLlm[F] {

    override def run(msg: Message, outcome: Outcome)(using platform: PlatformAdapter[F]): F[Outcome] =
      responder.respond(msg).flatMap {
        case Some(text) => deliver(text, msg, platform).as(Outcome.LLMReplied)
        case None       => outcome.pure[F]
      }
  }
}
