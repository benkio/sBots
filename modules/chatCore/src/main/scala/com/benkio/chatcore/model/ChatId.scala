package com.benkio.chatcore.model

opaque type ChatId = Long
object ChatId {
  def apply(id: Long): ChatId                = id
  extension (chatId: ChatId) def value: Long = chatId
}
