package com.benkio.chatcore

import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.Platform

/** Telegram looking ids for the tests that do not care about the platform. */
object TestIds {
  def chatId(id: Long): ChatId      = ChatId(Platform.Telegram, id.toString)
  def messageId(id: Int): MessageId = MessageId(id.toString)
}
