package com.benkio.chatcore.llm

import cats.Applicative
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message

/** The chatCore-owned port the [[com.benkio.chatcore.EngineLlm]] uses to ask for an LLM answer. chatLlm supplies the
  * implementation; chatCore never sees the LLM library.
  */
trait LlmResponder[F[_]] {

  /** The answer to send for `msg`, or `None` when the LLM has nothing to say. */
  def respond(msg: Message): F[Option[Text]]
}

object LlmResponder {

  /** Until the LLM replies are rebuilt: never answers, so the engine keeps the outcome it already had. */
  def noop[F[_]: Applicative]: LlmResponder[F] = new LlmResponder[F] {
    override def respond(msg: Message): F[Option[Text]] = Applicative[F].pure(None)
  }
}
