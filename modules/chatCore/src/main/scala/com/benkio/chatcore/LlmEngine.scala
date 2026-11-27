package com.benkio.chatcore

import cats.Applicative
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message

/** The LLM entry point of the [[BotEngine]]. It runs after, and outside, the request timeout, for the messages the
  * reactive part left unanswered. chatCore owns this port; chatLlm supplies the implementation, so the engine never
  * sees the LLM library. The engine only asks for the text: delivering it and the outcome stay with the [[BotEngine]].
  */
trait LlmEngine[F[_]] {

  /** The text to reply to `msg` with, or `None` when the LLM is off for the chat or has nothing to say. */
  def reply(msg: Message): F[Option[Text]]
}

object LlmEngine {

  /** Never answers. */
  def noop[F[_]: Applicative]: LlmEngine[F] = new LlmEngine[F] {
    override def reply(_msg: Message): F[Option[Text]] = Applicative[F].pure(None)
  }
}
