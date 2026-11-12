package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.Platform

/** The ids of Telegram: numbers, that the platform neutral [[ChatId]] and [[MessageId]] keep as strings. */
object TelegramIds {
  def chatId(id: Long): ChatId      = ChatId(Platform.Telegram, id.toString)
  def messageId(id: Int): MessageId = MessageId(id.toString)

  extension (chatId: ChatId) {

    /** Numeric id used by Telegram. Fails for other platforms. */
    def telegramId: Long =
      if chatId.platform == Platform.Telegram then chatId.id.toLong
      else throw new IllegalArgumentException(s"Not a Telegram chat id: ${chatId.platform.id}:${chatId.id}")
  }

  extension (messageId: MessageId) {

    /** The numeric id of Telegram. Fails for the ids of other platforms. */
    def telegramId: Int = messageId.value.toInt
  }
}
