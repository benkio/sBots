package com.benkio.chattelegramadapter.conversions

import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.User
import telegramium.bots.Chat as TelegramChat
import telegramium.bots.InaccessibleMessage
import telegramium.bots.MaybeInaccessibleMessage
import telegramium.bots.Message as TelegramMessage
import telegramium.bots.User as TelegramUser

object MessageConversions {

  extension (m: Message) {
    def toTelegramium: TelegramMessage =
      TelegramMessage(
        messageId = m.messageId,
        date = m.date.toInt,
        chat = TelegramChat(id = m.chatId.value, `type` = m.chatType),
        text = m.text,
        caption = m.caption
      )
  }

  extension (m: TelegramMessage) {
    def toModel: Message =
      Message(
        messageId = m.messageId,
        date = m.date.toLong,
        chatId = ChatId(m.chat.id),
        chatType = m.chat.`type`,
        text = m.text,
        caption = m.caption,
        newChatMembers =
          m.newChatMembers.map((u: TelegramUser) => User(id = u.id, isBot = u.isBot, firstName = u.firstName)),
        leftChatMember =
          m.leftChatMember.map((u: TelegramUser) => User(id = u.id, isBot = u.isBot, firstName = u.firstName)),
        isForward = m.forwardOrigin.nonEmpty
      )
  }

  extension (msg: MaybeInaccessibleMessage) {
    def toModelMessage: Option[Message] = msg match {
      case m: TelegramMessage     => Some(m.toModel)
      case i: InaccessibleMessage =>
        Some(
          Message(
            messageId = i.messageId,
            date = 0L,
            chatId = ChatId(i.chat.id),
            chatType = i.chat.`type`
          )
        )
    }
  }
}
