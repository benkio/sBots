package com.benkio.chatcore.model

import cats.Show

/** A chat identifier scoped to the platform it belongs to: Telegram's are numbers, IRC uses channel/nick names, Discord
  * and Slack use their own id strings.
  */
final case class ChatId(platform: Platform, id: String)

object ChatId {

  /** Rebuilds a chat id from the `platform` and `chat_id` columns of the DB. */
  def fromIds(platform: String, id: String): Either[Throwable, ChatId] =
    Platform.fromId(platform).left.map(new IllegalArgumentException(_)).map(ChatId(_, id))

  given Show[ChatId] = Show.show(c => s"${c.platform.id}:${c.id}")

}
