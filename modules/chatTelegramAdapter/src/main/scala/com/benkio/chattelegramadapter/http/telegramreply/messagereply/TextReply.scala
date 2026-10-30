package com.benkio.chattelegramadapter.http.telegramreply.messagereply

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.messagefiltering.*
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.high.implicits.*
import telegramium.bots.ChatId
import telegramium.bots.ChatIntId
import telegramium.bots.Message as TMessage
import telegramium.bots.ReplyParameters

import scala.concurrent.duration.FiniteDuration

object TextReply {
  def sendText[F[_]: Async: LogWriter: Api](
      reply: Text,
      msg: Message,
      replyToMessage: Boolean
  ): F[List[TMessage]] = {
    val chatId: ChatId            = ChatIntId(msg.chatId.value)
    val result: F[List[TMessage]] =
      for {
        _       <- LogWriter.info(s"[TelegramMessageReply[Text]] reply to message: ${msg.getContent}")
        _       <- Methods.sendChatAction(chatId, "typing").exec
        message <-
          Methods
            .sendMessage(
              chatId = chatId,
              text = reply.value,
              replyParameters = Option.when(replyToMessage)(ReplyParameters(Some(msg.messageId)))
            )
            .exec
        _ <- reply.timeToLive.fold(Async[F].unit)(ttl => {
          Async[F]
            .start(
              deleteMessage(
                chatId = message.chat.id,
                messageId = message.messageId,
                ttl = ttl,
                value = reply.value
              )
            )
            .void
        })
      } yield List(message)
    result
      .handleErrorWith(e =>
        LogWriter.error(s"[TextReply] error occurred when sending `${reply.value}`. Error: $e") *> List.empty.pure[F]
      )
  }

  def deleteMessage[F[_]: Async: LogWriter: Api](
      chatId: Long,
      messageId: Int,
      ttl: FiniteDuration,
      value: String
  ): F[Boolean] = {
    Async[F].sleep(ttl) >>
      LogWriter.info(s"[TelegramMessageReply[Text]] deleting `$value` after $ttl") >>
      Methods
        .deleteMessage(
          chatId = ChatIntId(chatId),
          messageId = messageId
        )
        .exec
        .handleErrorWith(e =>
          LogWriter
            .error(
              s"[TelegramMessageReply[Text]] error occurred when deleting `$value` after $ttl. Error: $e"
            )
            .as(false)
        )
  }
}
