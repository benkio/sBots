package com.benkio.telegrambotinfrastructure.http.telegramreply

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.messagefiltering.*
import com.benkio.telegrambotinfrastructure.model.reply.Text
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.high.implicits.*
import telegramium.bots.ChatId
import telegramium.bots.ChatIntId
import telegramium.bots.Html
import telegramium.bots.Markdown2
import telegramium.bots.Message
import telegramium.bots.ParseMode
import telegramium.bots.ReplyParameters

object TextReply {
  def sendText[F[_]: Async: LogWriter: Api](
      reply: Text,
      msg: Message,
      replyToMessage: Boolean
  ): F[List[Message]] = {
    val chatId: ChatId               = ChatIntId(msg.chat.id)
    val parseMode: Option[ParseMode] = reply.textType match {
      case Text.TextType.Plain    => None
      case Text.TextType.Markdown => Markdown2.some
      case Text.TextType.Html     => Html.some
    }
    val result: F[List[Message]] =
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
              Async[F].sleep(ttl) >>
                LogWriter.info(s"[TelegramReply[Text]] deleting `${reply.value}` after $ttl") >>
                Methods
                  .deleteMessage(
                    chatId = ChatIntId(message.chat.id),
                    messageId = message.messageId
                  )
                  .exec
                  .handleErrorWith(e =>
                    LogWriter
                      .error(
                        s"[TelegramReply[Text]] error occurred when deleting `${reply.value}` after $ttl. Error: $e"
                      )
                      .as(false)
                  )
            )
            .void
        })
      } yield List(message)
    result.handleErrorWith(e =>
      LogWriter.error(s"[TextReply] error occurred when sending `${reply.value}`. Error: $e") *> List.empty.pure[F]
    )
  }
}
