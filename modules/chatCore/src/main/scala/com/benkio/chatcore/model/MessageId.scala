package com.benkio.chatcore.model

import cats.Show

/** The id of a message in its chat. A string, because it depends on the platform: Telegram's are numbers that fit an
  * `Int`, Discord's are 64 bit numbers, Slack's are timestamps like `1700000000.000200`, IRC has none.
  */
final case class MessageId(value: String)

object MessageId {
  def telegram(id: Int): MessageId = MessageId(id.toString)

  given Show[MessageId] = Show.show(_.value)

  extension (messageId: MessageId) {

    /** The numeric id of Telegram. Fails for the ids of other platforms. */
    def telegramId: Int = messageId.value.toInt
  }
}
