package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import com.benkio.chatcore.model.ChatId as ModelChatId
import com.benkio.chatcore.model.Message as ModelMessage
import com.benkio.chatcore.model.MessageId as ModelMessageId
import com.benkio.chattelegramadapter.model.TelegramMessageIds
import telegramium.bots.MaybeInaccessibleMessage

object ModelMessageFromCallback {

  def build(msg: MaybeInaccessibleMessage): ModelMessage = {
    val telegramMessageIds = TelegramMessageIds.getIds(msg)
    ModelMessage(
      messageId = Some(ModelMessageId.telegram(telegramMessageIds.messageId)),
      date = 0L,
      chatId = ModelChatId.telegram(telegramMessageIds.chatId),
      chatType = telegramMessageIds.chatType
    )
  }
}
