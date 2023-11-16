package com.benkio.telegrambotinfrastructure.model

import cats.Applicative
import telegramium.bots.Message
import cats.effect._
import cats.implicits._

import scala.util.Random

sealed trait ReplySelection {
  def logic[F[_]: Sync](reply: Reply[F], message: Message): F[List[ReplyValue]]
}

case object SelectAll extends ReplySelection {
  def logic[F[_]: Applicative](reply: Reply[F],message: Message)(implicit syncF: Sync[F]): F[List[ReplyValue]] =
    reply match {
      case TextReply(text, _) => Applicative[F].pure(text)
      case TextReplyM(textM, _) => textM(message)
      case MediaReply(mediaFiles, _) => mediaFiles
    }
  syncF.pure(replies)
}
case object RandomSelection extends ReplySelection {
  def logic[F[_]: Sync](reply: Reply[F], message: Message): F[List[ReplyValue]] =
    for {
      randomNumGen <- Sync[F].delay(new Random())
      replies = SelectAll.logic[F](reply, message)
      randomVal    <- Sync[F].delay(randomNumGen.between(0, replies.size))
    } yield List(replies(randomVal))
}
