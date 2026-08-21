package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import com.benkio.chatcore.model.ChatId as ModelChatId
import com.benkio.chatcore.model.Message as ModelMessage
import com.benkio.chattelegramadapter.model.TelegramMessageIds
import telegramium.bots.MaybeInaccessibleMessage

object ModelMessageFromCallback {

  def build(msg: MaybeInaccessibleMessage): ModelMessage = {
    val telegramMessageIds = TelegramMessageIds.getIds(msg)
    ModelMessage(
      messageId = telegramMessageIds.messageId,
      date = 0L,
      chatId = ModelChatId(telegramMessageIds.chatId),
      chatType = telegramMessageIds.chatType
    )
  }
}
