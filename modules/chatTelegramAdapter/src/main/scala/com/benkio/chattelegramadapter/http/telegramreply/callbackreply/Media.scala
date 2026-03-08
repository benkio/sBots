package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.ChatId as ModelChatId
import com.benkio.chatcore.model.Message as ModelMessage
import com.benkio.chatcore.repository.Repository
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.MediaFileReply
import log.effect.LogWriter
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.high.Api
import telegramium.bots.high.Methods
import telegramium.bots.ChatIntId
import telegramium.bots.InaccessibleMessage
import telegramium.bots.MaybeInaccessibleMessage
import telegramium.bots.Message as TMessage

object Media {

  def reply[F[_]: Async: LogWriter: Api](
      msg: MaybeInaccessibleMessage,
      mediaName: String,
      repository: Repository[F]
  ): F[Unit] = {
    val (chatId, messageId, chatType) = ids(msg)
    val modelMsg                      =
      ModelMessage(
        messageId = messageId,
        date = 0L,
        chatId = ModelChatId(chatId),
        chatType = chatType
      )

    MediaFileReply
      .sendMediaFile(
        reply = MediaFile.fromString(mediaName),
        msg = modelMsg,
        repository = repository,
        replyToMessage = false
      )
      .void *>
      Methods
        .deleteMessage(
          chatId = ChatIntId(chatId),
          messageId = messageId
        )
        .exec
        .void
  }

  private def ids(msg: MaybeInaccessibleMessage): (Long, Int, String) =
    msg match {
      case m: TMessage            => (m.chat.id, m.messageId, m.chat.`type`)
      case m: InaccessibleMessage => (m.chat.id, m.messageId, m.chat.`type`)
    }
}
