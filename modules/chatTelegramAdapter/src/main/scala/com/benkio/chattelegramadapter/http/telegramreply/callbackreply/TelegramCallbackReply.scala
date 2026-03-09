package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import cats.effect.Async
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message as ModelMessage
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chattelegramadapter.model.CallbackData
import log.effect.LogWriter
import telegramium.bots.high.Api
import telegramium.bots.MaybeInaccessibleMessage

import scala.concurrent.duration.FiniteDuration

object TelegramCallbackReply {

  def reply[F[_]: Async: LogWriter: Api](
      msg: MaybeInaccessibleMessage,
      callbackData: CallbackData,
      repository: Repository[F],
      sBotConfig: SBotConfig,
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, ModelMessage => F[List[Text]]],
      dbLayer: DBLayer[F],
      ttl: Option[FiniteDuration]
  ): F[Unit] = callbackData match {
    case CallbackData.NextPage(currentPage, commandKey) =>
      Pagination.reply(
        msg = msg,
        newPage = currentPage + 1,
        commandKey = commandKey,
        sBotConfig = sBotConfig,
        repository = repository,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = effectfulCallbacks,
        dbLayer = dbLayer,
        ttl = ttl
      )
    case CallbackData.PreviousPage(currentPage, commandKey) =>
      Pagination.reply(
        msg = msg,
        newPage = (currentPage - 1).max(0),
        commandKey = commandKey,
        sBotConfig = sBotConfig,
        repository = repository,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = effectfulCallbacks,
        dbLayer = dbLayer,
        ttl = ttl
      )
    case CallbackData.Media(value) => Media.reply(msg = msg, mediaName = value, repository = repository)
  }
}
