package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.Async
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.repository.db.DBMedia
import com.benkio.chatcore.repository.Repository
import com.benkio.chattelegramadapter.model.CallbackData
import log.effect.LogWriter
import telegramium.bots.high.Api
import telegramium.bots.MaybeInaccessibleMessage

object TelegramCallbackReply {

  def reply[F[_]: Async: LogWriter: Api](
      msg: MaybeInaccessibleMessage,
      callbackData: CallbackData,
      repository: Repository[F],
      dbMedia: DBMedia[F],
      sBotInfo: SBotInfo
  ): F[Unit] = callbackData match {
    case CallbackData.NextPage(currentPage, commandKey) =>
      Pagination.reply(msg = msg, newPage = currentPage + 1, dbMedia = dbMedia, sBotInfo = sBotInfo, commandKey = commandKey)
    case CallbackData.PreviousPage(currentPage, commandKey) =>
      Pagination.reply(msg = msg, newPage = (currentPage - 1).max(0), dbMedia = dbMedia, sBotInfo = sBotInfo, commandKey = commandKey)
    case CallbackData.Media(value) => Media.reply(msg = msg, mediaName = value, repository = repository)
  }
}
