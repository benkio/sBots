package com.benkio.chattelegramadapter.conversions

import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.User
import telegramium.bots.Chat as TelegramChat
import telegramium.bots.InaccessibleMessage
import telegramium.bots.MaybeInaccessibleMessage
import telegramium.bots.Message as TelegramMessage
import telegramium.bots.ReplyParameters
import telegramium.bots.User as TelegramUser

object MessageConversions {

  extension (m: Message) {

    /** The parameters to reply to the message, when asked to and when there is a message to reply to: what a
      * subscription sends has none.
      */
    def replyParameters(replyToMessage: Boolean): Option[ReplyParameters] =
      Option.when(replyToMessage)(m.messageId).flatten.map(id => ReplyParameters(Some(id.telegramId)))

    def toTelegramium: TelegramMessage =
      TelegramMessage(
        messageId = m.messageId.fold(0)(_.telegramId),
        date = m.date.toInt,
        chat = TelegramChat(id = m.chatId.telegramId, `type` = m.chatType),
        text = m.text,
        caption = m.caption
      )
  }

  extension (m: TelegramMessage) {
    def toModel: Message =
      Message(
        messageId = Some(MessageId.telegram(m.messageId)),
        date = m.date.toLong,
        chatId = ChatId.telegram(m.chat.id),
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
            messageId = Some(MessageId.telegram(i.messageId)),
            date = 0L,
            chatId = ChatId.telegram(i.chat.id),
            chatType = i.chat.`type`
          )
        )
    }
  }
}
