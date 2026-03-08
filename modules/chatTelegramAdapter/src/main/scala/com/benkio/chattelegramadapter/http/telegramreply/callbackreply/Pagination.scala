package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.Async
import telegramium.bots.high.Api
import telegramium.bots.MaybeInaccessibleMessage

object Pagination {

  def reply[F[_]: Async: Api](msg: MaybeInaccessibleMessage, newPage: Int): F[Unit] = {
    ???
    // TODO:
    // re-query the db
    // change the page
    // edit the markup message
    /*
     editMessageReplyMarkup(
                chatId = Some(ChatIntId(msg.chat.id)),
                messageId = Some(msg.messageId),
                replyMarkup = None
              ).exec
     */
  }
}
