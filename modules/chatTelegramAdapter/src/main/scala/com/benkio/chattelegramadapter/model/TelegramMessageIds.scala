package com.benkio.chattelegramadapter.model

import telegramium.bots.InaccessibleMessage
import telegramium.bots.MaybeInaccessibleMessage
import telegramium.bots.Message

final case class TelegramMessageIds(chatId: Long, messageId: Int, chatType: String)

object TelegramMessageIds {
  def getIds(msg: MaybeInaccessibleMessage): TelegramMessageIds =
    msg match {
      case m: Message            => TelegramMessageIds(m.chat.id, m.messageId, m.chat.`type`)
      case m: InaccessibleMessage => TelegramMessageIds(m.chat.id, m.messageId, m.chat.`type`)
    }
}
