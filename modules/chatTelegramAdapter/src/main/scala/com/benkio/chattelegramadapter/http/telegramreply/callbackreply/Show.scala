package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.Async
import com.benkio.chatcore.repository.Repository
import log.effect.LogWriter
import telegramium.bots.high.Api
import telegramium.bots.MaybeInaccessibleMessage

import scala.annotation.unused

object Show {

  def reply[F[_]: Async: LogWriter: Api](
      msg: MaybeInaccessibleMessage,
      showId: String,
      showDb: DBShow[F]
  ): F[Unit] = {
    // val telegramMessageIds = TelegramMessageIds.getIds(msg)
    // val modelMsg           = ModelMessageFromCallback.build(msg)

    // for {
    //   _ <- LogWriter.info(s"[Media.reply] reply to callback for data $mediaName")
    //   _ <- MediaFileReply
    //     .sendMediaFile(
    //       reply = MediaFile.fromString(mediaName),
    //       msg = modelMsg,
    //       repository = repository,
    //       replyToMessage = false
    //     )
    //   _ <- Methods
    //     .deleteMessage(
    //       chatId = ChatIntId(telegramMessageIds.chatId),
    //       messageId = telegramMessageIds.messageId
    //     )
    //     .exec
    // } yield ()

  }
}
