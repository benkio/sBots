package com.benkio.telegrambotinfrastructure.http.telegramreply

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.messagefiltering.*
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.model.Message
import com.benkio.telegrambotinfrastructure.model.Message.toModel
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.high.implicits.*
import telegramium.bots.ChatId
import telegramium.bots.ChatIntId
import telegramium.bots.Html
import telegramium.bots.Markdown2
import telegramium.bots.Message as TMessage
import telegramium.bots.ParseMode
import telegramium.bots.ReplyParameters

import scala.concurrent.duration.FiniteDuration

object TextReply {
  def sendText[F[_]: Async: LogWriter: Api](
      reply: Text,
      msg: Message,
      replyToMessage: Boolean
  ): F[List[Message]] = {
    val chatId: ChatId               = ChatIntId(msg.chatId.value)
    val parseMode: Option[ParseMode] = reply.textType match {
      case Text.TextType.Plain    => None
      case Text.TextType.Markdown => Markdown2.some
      case Text.TextType.Html     => Html.some
    }
    val result: F[List[TMessage]] =
      for {
        _       <- LogWriter.info(s"[TelegramReply[Text]] reply to message: ${msg.getContent}")
        _       <- Methods.sendChatAction(chatId, "typing").exec
        message <-
          Methods
            .sendMessage(
              chatId = chatId,
              text = reply.value,
              replyParameters = Option.when(replyToMessage)(ReplyParameters(msg.messageId)),
              parseMode = parseMode
            )
            .exec
        _ <- reply.timeToLive.fold(Async[F].unit)(ttl => {
          Async[F]
            .start(
              deleteMessage(
                chatId = message.chat.id,
                messageId = message.messageId,
                ttl = ttl,
                reply = reply
              )
            )
            .void
        })
      } yield List(message)
    result
      .map(_.map(_.toModel))
      .handleErrorWith(e =>
        LogWriter.error(s"[TextReply] error occurred when sending `${reply.value}`. Error: $e") *> List.empty.pure[F]
      )
  }

  def deleteMessage[F[_]: Async: LogWriter: Api](
      chatId: Long,
      messageId: Int,
      ttl: FiniteDuration,
      reply: Text
  ): F[Boolean] = {
    Async[F].sleep(ttl) >>
      LogWriter.info(s"[TelegramReply[Text]] deleting `${reply.value}` after $ttl") >>
      Methods
        .deleteMessage(
          chatId = ChatIntId(chatId),
          messageId = messageId
        )
        .exec
        .handleErrorWith(e =>
          LogWriter
            .error(
              s"[TelegramReply[Text]] error occurred when deleting `${reply.value}` after $ttl. Error: $e"
            )
            .as(false)
        )
  }
}
