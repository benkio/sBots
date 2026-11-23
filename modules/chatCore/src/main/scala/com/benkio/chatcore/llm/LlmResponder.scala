package com.benkio.chatcore.llm

import cats.Applicative
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.Reply
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.Message

/** The port the engine calls to answer a message directed to the bot with the LLM. Everything provider specific lives
  * behind it (see the `chatLlm` module): the engine only knows this contract.
  */
trait LlmResponder[F[_]] {

  /** @param msg
    *   the directed message to answer
    * @param candidates
    *   the reply bundles of the bot that match the message, offered so the responder can reuse them
    * @return
    *   the reply, or none when the responder does not answer (disabled, rate limited, failed)
    */
  def respond(msg: Message, candidates: List[ReplyBundleMessage]): F[Option[Reply]]
}

object LlmResponder {

  /** Never answers: the behavior of a bot with the feature unconfigured. */
  def noop[F[_]: Applicative]: LlmResponder[F] = new LlmResponder[F] {
    override def respond(msg: Message, candidates: List[ReplyBundleMessage]): F[Option[Reply]] =
      none[Reply].pure[F]
  }
}
