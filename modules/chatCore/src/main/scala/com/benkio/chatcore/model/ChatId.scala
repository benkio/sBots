package com.benkio.chatcore.model

import cats.Show

/** A chat identifier scoped to the platform it belongs to: Telegram chat ids are numbers, IRC uses channel/nick names,
  * Discord and Slack use their own id strings.
  */
final case class ChatId(platform: Platform, id: String)

object ChatId {
  def telegram(id: Long): ChatId = ChatId(Platform.Telegram, id.toString)

  given Show[ChatId] = Show.show(c => s"${c.platform.id}:${c.id}")

  extension (chatId: ChatId) {

    /** Numeric id used by Telegram and by the DB, whose chat_id column is still a BIGINT. Fails for other platforms.
      * The DB use goes away once the schema is platform-keyed.
      */
    def telegramId: Long =
      if chatId.platform == Platform.Telegram then chatId.id.toLong
      else throw new IllegalArgumentException(s"Not a Telegram chat id: ${chatId.platform.id}:${chatId.id}")
  }
}
