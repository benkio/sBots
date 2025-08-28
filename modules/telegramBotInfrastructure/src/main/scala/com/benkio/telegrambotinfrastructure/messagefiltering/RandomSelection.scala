package com.benkio.telegrambotinfrastructure.messagefiltering

import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.reply.MediaReply
import com.benkio.telegrambotinfrastructure.model.reply.Reply
import com.benkio.telegrambotinfrastructure.model.reply.ReplyValue
import com.benkio.telegrambotinfrastructure.model.reply.TextReply
import com.benkio.telegrambotinfrastructure.model.reply.TextReplyM
import telegramium.bots.Message

import scala.util.Random

object RandomSelection {
  def select[F[_]: Sync](reply: Reply[F], message: Message): F[List[ReplyValue]] =
    for {
      randomNumGen <- Sync[F].delay(new Random())
      replies      <- reply match {
        case TextReply(text, _)        => Sync[F].pure(text)
        case TextReplyM(textM, _)      => Sync[F].widen(textM(message))
        case MediaReply(mediaFiles, _) => Sync[F].widen(mediaFiles)
      }
      randomVal <- Sync[F].delay(randomNumGen.between(0, replies.size))
    } yield List(replies(randomVal))
}
