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

  /** Computes the value of an effectful reply, e.g. the one of a command, for the chat of the message. */
  def runEffectful(reply: EffectfulReply, message: Message): F[ReplyValue]
}
