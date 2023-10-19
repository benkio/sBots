package com.benkio.telegrambotinfrastructure.model

import cats.effect._
import cats.implicits._

import scala.util.Random

sealed trait ReplySelection {
  def logic[F[_]: Sync](replies: List[Reply]): F[List[Reply]]
}

case object SelectAll extends ReplySelection {
  def logic[F[_]](replies: List[Reply])(implicit syncF: Sync[F]): F[List[Reply]] = syncF.pure(replies)
}
case object RandomSelection extends ReplySelection {
  def logic[F[_]: Sync](replies: List[Reply]): F[List[Reply]] =
    for {
      randomNumGen <- Sync[F].delay(new Random())
      randomVal    <- Sync[F].delay(randomNumGen.between(0, replies.size))
    } yield List(replies(randomVal))
}
