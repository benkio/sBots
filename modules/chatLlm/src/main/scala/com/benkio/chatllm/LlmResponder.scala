package com.benkio.chatllm

import cats.Applicative
import com.benkio.chatcore.llm.Persona
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message

import scala.annotation.unused

/** The chatCore-owned port the [[com.benkio.chatcore.LlmEngine]] uses to ask for an LLM answer. chatLlm supplies the
  * implementation; chatCore never sees the LLM library.
  */
trait LlmResponder[F[_]] {

  /** The answer to send for `msg`, playing the `persona`, or `None` when the LLM has nothing to say. */
  def respond(persona: Persona, msg: Message): F[Option[Text]]
}

object LlmResponder {

  /** The responder over the startup clients (the default provider and its fallbacks). The LLM replies are being
    * rebuilt, so for now it never answers.
    */
  def apply[F[_]: Applicative](@unused clients: LlmFallbackClients): LlmResponder[F] = noop[F]

  /** Until the LLM replies are rebuilt: never answers, so the engine keeps the outcome it already had. */
  def noop[F[_]: Applicative]: LlmResponder[F] = new LlmResponder[F] {
    override def respond(_persona: Persona, _msg: Message): F[Option[Text]] = Applicative[F].pure(None)
  }
}
