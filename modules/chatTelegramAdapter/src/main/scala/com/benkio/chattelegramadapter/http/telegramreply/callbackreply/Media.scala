package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.ChatId as ModelChatId
import com.benkio.chatcore.model.Message as ModelMessage
import com.benkio.chatcore.repository.Repository
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.MediaFileReply
import com.benkio.chattelegramadapter.model.TelegramMessageIds
import log.effect.LogWriter
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.high.Api
import telegramium.bots.high.Methods
import telegramium.bots.ChatIntId
import telegramium.bots.MaybeInaccessibleMessage

object Media {

  def reply[F[_]: Async: LogWriter: Api](
      msg: MaybeInaccessibleMessage,
      mediaName: String,
      repository: Repository[F]
  ): F[Unit] = {
    val telegramMessageIds = TelegramMessageIds.getIds(msg)
    val modelMsg           =
      ModelMessage(
        messageId = telegramMessageIds.messageId,
        date = 0L,
        chatId = ModelChatId(telegramMessageIds.chatId),
        chatType = telegramMessageIds.chatType
      )

    for {
      _ <- LogWriter.info(s"[Media.reply] reply to callback for data $mediaName")
      _ <- MediaFileReply
        .sendMediaFile(
          reply = MediaFile.fromString(mediaName),
          msg = modelMsg,
          repository = repository,
          replyToMessage = false
        )
      _ <- Methods
        .deleteMessage(
          chatId = ChatIntId(telegramMessageIds.chatId),
          messageId = telegramMessageIds.messageId
        )
        .exec
    } yield ()

  }
}
