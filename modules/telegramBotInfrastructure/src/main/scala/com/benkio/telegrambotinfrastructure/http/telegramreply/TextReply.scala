package com.benkio.telegrambotinfrastructure.http.telegramreply


import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import cats.*
import cats.data.EitherT
import cats.effect.*
import cats.implicits.*

import com.benkio.telegrambotinfrastructure.messagefiltering.*









import com.benkio.telegrambotinfrastructure.model.reply.Text

import com.benkio.telegrambotinfrastructure.repository.Repository
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
  given telegramTextReply: TelegramReply[Text] = new TelegramReply[Text] {
    override def reply[F[_]: Async: LogWriter: Api](
      reply: Text,
      msg: Message,
      repository: Repository[F],
      dbLayer: DBLayer[F],
      replyToMessage: Boolean
    ): F[List[Message]] = {
      val chatId: ChatId               = ChatIntId(msg.chat.id)
      val parseMode: Option[ParseMode] = reply.textType match {
        case Text.TextType.Plain    => None
        case Text.TextType.Markdown => Markdown2.some
        case Text.TextType.Html     => Html.some
      }
      val result: EitherT[F, Throwable, List[Message]] =
        for {
          _       <- EitherT.liftF(LogWriter.info(s"[TelegramReply[Text]] reply to message: ${msg.getContent}"))
          _       <- Methods.sendChatAction(chatId, "typing").exec.attemptT
          message <-
            Methods
              .sendMessage(
                chatId = chatId,
                text = reply.value,
                replyParameters = Option.when(replyToMessage)(ReplyParameters(msg.messageId)),
                parseMode = parseMode
              )
              .exec
              .attemptT
        } yield List(message)
      result.getOrElse(List.empty)
    }
  }
}
