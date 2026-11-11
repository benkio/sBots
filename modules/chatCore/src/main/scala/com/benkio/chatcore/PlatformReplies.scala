package com.benkio.chatcore

import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.Message

/** What the [[BotEngine]] needs from a platform: the way to send a reply and to compute the replies that depend on the
  * platform (e.g. the ones made of buttons).
  */
trait PlatformReplies[F[_]] {

  /** Sends the reply value to the chat of the message. */
  def send(replyValue: ReplyValue, message: Message, replyToMessage: Boolean): F[Unit]

  /** Computes the value of an effectful reply, e.g. the one of a command, for the chat of the message. */
  def runEffectful(reply: EffectfulReply, message: Message): F[ReplyValue]
}
