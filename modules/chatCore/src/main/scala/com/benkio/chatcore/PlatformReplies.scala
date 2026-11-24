package com.benkio.chatcore

import cats.syntax.all.*
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.Message

/** What the [[BotEngine]] needs from a platform: the way to send a reply and to compute the replies that depend on the
  * platform (e.g. the ones made of buttons).
  */
trait PlatformReplies[F[_]] {

  /** Sends the reply value to the chat of the message. */
  def send(replyValue: ReplyValue, message: Message, replyToMessage: Boolean): F[Unit]

  /** Sends all reply values of a logical reply to the chat of the message, in order. Platforms that need to enforce a
    * budget over the whole reply (e.g. IRC line limits) should override this.
    */
  def sendAll(replyValues: List[ReplyValue], message: Message, replyToMessage: Boolean)(using
      cats.Monad[F]
  ): F[Unit] =
    replyValues.traverse_(send(_, message, replyToMessage))

  /** Shows the "typing" action in the chat of the message while a slow reply (the LLM) is computed. Platforms with no
    * such action do nothing.
    */
  def sendTyping(message: Message)(using cats.Applicative[F]): F[Unit] = {
    val _ = message
    cats.Applicative[F].unit
  }

  /** Computes the value of an effectful reply, e.g. the one of a command, for the chat of the message. `overridePage`
    * is the page the reply runs at (the platform-neutral navigation of the paged lists, e.g. the text commands of the
    * platforms without buttons).
    */
  def runEffectful(
      reply: EffectfulReply,
      message: Message,
      overridePage: Option[Int] = None
  ): F[ReplyValue]
}
