package com.benkio.telegrambotinfrastructure.model

import iozhik.OpenEnum
import telegramium.bots.{Chat as TelegramChat, Message as TelegramMessage, MessageOrigin, User}

final case class Message(
    messageId: Int,
    date: Long,
    chatId: ChatId,
    chatType: String,
    text: Option[String] = None,
    caption: Option[String] = None,
    newChatMembers: List[User] = List.empty,
    leftChatMember: Option[User] = None,
    forwardOrigin: Option[OpenEnum[MessageOrigin]] = None
)

object Message {

  extension (m: Message) {
    def toTelegramium: TelegramMessage =
      TelegramMessage(
        m.messageId,
        date = m.date,
        chat = TelegramChat(id = m.chatId.value, `type` = m.chatType),
        text = m.text,
        caption = m.caption,
        newChatMembers = m.newChatMembers,
        leftChatMember = m.leftChatMember,
        forwardOrigin = m.forwardOrigin
      )
  }

  extension (m: TelegramMessage) {
    def toModel: Message =
      Message(
        messageId = m.messageId,
        date = m.date,
        chatId = ChatId(m.chat.id),
        chatType = m.chat.`type`,
        text = m.text,
        caption = m.caption,
        newChatMembers = m.newChatMembers,
        leftChatMember = m.leftChatMember,
        forwardOrigin = m.forwardOrigin
      )
  }
}
