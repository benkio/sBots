package com.benkio.chattelegramadapter.http

import cats.syntax.all.*
import cats.Functor
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.high.Api
import telegramium.bots.high.Methods

/** Answers the press of an inline keyboard button, so Telegram clears the pending state of the callback. */
object TelegramCallbackAnswer {

  /** Acknowledges a press the bot handled. */
  def ack[F[_]: Functor](callbackQueryId: String)(using api: Api[F]): F[Unit] =
    Methods.answerCallbackQuery(callbackQueryId = callbackQueryId).exec.void

  /** Acknowledges a press the bot could not handle, with a short message only the pressing user sees. */
  def reject[F[_]: Functor](callbackQueryId: String, text: String)(using api: Api[F]): F[Unit] =
    Methods.answerCallbackQuery(callbackQueryId = callbackQueryId, text = Some(text)).exec.void
}
