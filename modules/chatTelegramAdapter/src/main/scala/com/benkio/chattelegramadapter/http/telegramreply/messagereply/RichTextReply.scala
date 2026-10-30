package com.benkio.chattelegramadapter.http.telegramreply.messagereply

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.messagefiltering.*
import com.benkio.chatcore.model.Message
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.high.implicits.*
import telegramium.bots.ChatId
import telegramium.bots.ChatIntId
import telegramium.bots.InputRichMessage
import telegramium.bots.Message as TMessage
import telegramium.bots.ReplyParameters

import scala.concurrent.duration.FiniteDuration

object RichTextReply {

  def sendRichText[F[_]: Async: LogWriter: Api](
      richMessage: InputRichMessage,
      msg: Message,
      replyToMessage: Boolean,
      timeToLive: Option[FiniteDuration] = None
  ): F[List[TMessage]] = {
    val chatId: ChatId            = ChatIntId(msg.chatId.value)
    val result: F[List[TMessage]] =
      for {
        _       <- LogWriter.info(s"[TelegramMessageReply[RichText]] sendRichText: ${msg.getContent}")
        _       <- Methods.sendChatAction(chatId, "typing").exec
        message <-
          Methods
            .sendRichMessage(
              chatId = chatId,
              richMessage = richMessage,
              replyParameters = Option.when(replyToMessage)(ReplyParameters(Some(msg.messageId)))
            )
            .exec
        _ <- timeToLive.fold(Async[F].unit)(ttl =>
          Async[F]
            .start(
              TextReply.deleteMessage(
                chatId = message.chat.id,
                messageId = message.messageId,
                ttl = ttl,
                value = richMessage.toString
              )
            )
            .void
        )
      } yield List(message)
    result
      .handleErrorWith(e =>
        LogWriter.error(s"[RichTextReply] error occurred when sending rich text. Error: $e") *> List.empty.pure[F]
      )
  }
}
