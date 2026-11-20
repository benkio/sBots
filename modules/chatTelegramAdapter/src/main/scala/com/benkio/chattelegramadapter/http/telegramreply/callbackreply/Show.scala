package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.repository.db.DBShow
import com.benkio.chatcore.PagedListNavigation
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.TextReply
import com.benkio.chattelegramadapter.model.TelegramMessageIds
import log.effect.LogWriter
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.high.Api
import telegramium.bots.high.Methods
import telegramium.bots.ChatIntId
import telegramium.bots.MaybeInaccessibleMessage

object Show {

  def reply[F[_]: Async: LogWriter: Api](
      msg: MaybeInaccessibleMessage,
      showId: String,
      timestamp: Option[String],
      showDb: DBShow[F]
  ): F[Unit] = {
    val telegramMessageIds = TelegramMessageIds.getIds(msg)
    val modelMsg           = ModelMessageFromCallback.build(msg)

    for {
      _    <- LogWriter.info(s"[Show.reply] reply to callback for show $showId and timestamp $timestamp")
      text <- PagedListNavigation.showText(showId, timestamp, showDb)
      _    <- TextReply
        .sendText(
          reply = text,
          msg = modelMsg,
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
