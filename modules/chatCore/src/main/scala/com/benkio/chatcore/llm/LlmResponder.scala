package com.benkio.chatcore.llm

import cats.syntax.all.*
import cats.Applicative
import cats.FlatMap
import com.benkio.chatcore.model.reply.Reply
import com.benkio.chatcore.model.Message

/** The port the engine calls to answer a message directed to the bot with the LLM. Everything provider specific lives
  * behind it (see the `chatLlm` module): the engine only knows this contract.
  */
trait LlmResponder[F[_]] {

  /** Whether this responder can ever answer. The engine skips the whole LLM stage (settings lookup, typing action) when
    * it cannot, as the `noop` default.
    */
  def answers: Boolean = true

  /** @param msg
    *   the directed message to answer
    * @return
    *   the reply, or none when the responder does not answer (disabled, rate limited, failed)
    */
  def respond(msg: Message): F[Option[Reply]]

  /** [[respond]] preceded by `typing`, the platform's typing action. Both run inside the engine's timeout, so a
    * responder that observes its own cancellation overrides this to cover the typing phase too. A failing `typing` is
    * the caller's to handle.
    */
  def respondAfter(msg: Message, typing: F[Unit])(using
      FlatMap[F]
  ): F[Option[Reply]] =
    typing.flatMap(_ => respond(msg))

  /** The engine hands over every plain message once it is handled, after [[respond]] if the LLM was asked, so the
    * message is not part of its own prompt. What is kept (and whether the chat enabled it) is up to the responder.
    */
  def remember(msg: Message)(using Applicative[F]): F[Unit] = {
    val _ = msg
    ().pure[F]
  }

  /** The engine hands over the reply the LLM stage sent (after [[remember]] of the message it answers). */
  def rememberAnswer(msg: Message, reply: Reply)(using
      Applicative[F]
  ): F[Unit] = {
    val _ = (msg, reply)
    ().pure[F]
  }
}

object LlmResponder {

  /** Never answers: the behavior of a bot with the feature unconfigured. */
  def noop[F[_]: Applicative]: LlmResponder[F] = new LlmResponder[F] {
    override val answers: Boolean                        = false
    override def respond(msg: Message): F[Option[Reply]] =
      none[Reply].pure[F]
  }
}
