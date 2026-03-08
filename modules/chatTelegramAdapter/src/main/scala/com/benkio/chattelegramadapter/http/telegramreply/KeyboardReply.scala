package com.benkio.chattelegramadapter.http.telegramreply

import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.messagefiltering.getContent
import com.benkio.chatcore.model.media.Media
import com.benkio.chatcore.model.Message
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.high.implicits.methodOps
import telegramium.bots.ChatId
import telegramium.bots.ChatIntId
import telegramium.bots.InlineKeyboardMarkup
import telegramium.bots.Message as TMessage

import scala.annotation.unused

object KeyboardReply {
  def sendKeyboard[F[_]: Async: LogWriter: Api](
      reply: List[Media],
      keyboardTitle: String,
      msg: Message,
      @unused replyToMessage: Boolean
  ): F[List[TMessage]] = {
    val chatId: ChatId            = ChatIntId(msg.chatId.value)
    val result: F[List[TMessage]] =
      for {
        _       <- LogWriter.info(s"[TelegramReply[Keyboard]] reply to message: ${msg.getContent}")
        _       <- Methods.sendChatAction(chatId, "typing").exec
        message <-
          Methods
            .sendMessage(
              chatId = chatId,
              text = keyboardTitle,
              replyMarkup = Some(
                buildInlineKeyboard(reply)
              )
            )
            .exec
      } yield List(message)
    result
      .handleErrorWith(e =>
        LogWriter.error(s"[KeyboardReply] error occurred when sending keyboard. Error: $e") *> List.empty.pure[F]
      )
  }

  private def buildInlineKeyboard(data: List[Media]): InlineKeyboardMarkup = ??? // TODO: implement
}
