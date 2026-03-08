package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import com.benkio.chattelegramadapter.callback.CallbackData
import telegramium.bots.high.Api
import telegramium.bots.MaybeInaccessibleMessage
import cats.effect.Async

object TelegramCallbackReply {

  def reply[F[_]: Async: Api](
      msg: MaybeInaccessibleMessage,
      callbackData: CallbackData
  ): F[Unit] = ??? // TODO: implement
}
