package com.benkio.chattelegramadapter.http

import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.Notifier
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.high.Api
import telegramium.bots.ChatIntId

/** Sends the notifications to a Telegram chat of the operators: the critical errors and the important events. */
final class TelegramNotifier[F[_]: Async](opsChatId: Long)(using api: Api[F], log: LogWriter[F]) extends Notifier[F] {

  private val chatId: ChatIntId = ChatIntId(id = opsChatId)

  private def sendText(text: String): F[Unit] =
    Methods.sendMessage(chatId = Some(chatId), text = text).exec.void

  override def info(sBotInfo: SBotInfo, message: String): F[Unit] =
    sendText(Notifier.formatInfo(sBotInfo, message))

  override def mediaError(message: Message, mediaFile: MediaFile, error: Throwable): F[Unit] = {
    val formatted = Notifier.formatMediaError(message, mediaFile, error)
    // The bot has no rights in the chat: only logged. The other errors the user can cause with a strange input are skipped
    log.warn(formatted).whenA(Notifier.isMissingRights(error)) *>
      sendText(formatted).whenA(Notifier.isReportable(error))
  }

  override def error(sBotInfo: SBotInfo, context: String, error: Throwable): F[Unit] = {
    val formatted = Notifier.formatError(sBotInfo = sBotInfo, context = context, error = error)
    // Same filter as the media errors: no rights in the chat is only logged, what the user can cause is skipped
    log.warn(formatted).whenA(Notifier.isMissingRights(error)) *>
      sendText(formatted).whenA(Notifier.isReportable(error))
  }
}
