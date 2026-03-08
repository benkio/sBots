package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.Async
import com.benkio.chatcore.repository.Repository
import com.benkio.chattelegramadapter.callback.CallbackData
import log.effect.LogWriter
import telegramium.bots.high.Api
import telegramium.bots.MaybeInaccessibleMessage

object TelegramCallbackReply {

  def reply[F[_]: Async: LogWriter: Api](
      msg: MaybeInaccessibleMessage,
      callbackData: CallbackData,
      repository: Repository[F]
  ): F[Unit] = callbackData match {
    case CallbackData.NextPage(currentPage)     => Pagination.reply(msg = msg, newPage = currentPage + 1)
    case CallbackData.PreviousPage(currentPage) => Pagination.reply(msg = msg, newPage = (currentPage - 1).max(0))
    case CallbackData.Media(value)              => Media.reply(msg = msg, mediaName = value, repository = repository)
  }
}
