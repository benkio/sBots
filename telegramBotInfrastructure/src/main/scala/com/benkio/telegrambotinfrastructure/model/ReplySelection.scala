package com.benkio.telegrambotinfrastructure.model

import cats.effect._
import cats.implicits._
import io.chrisdavenport.random.Random

sealed trait ReplySelection {
  def logic[F[_]: Sync](replies: List[Reply]): F[List[Reply]]
}

case object SelectAll extends ReplySelection {
  def logic[F[_]](replies: List[Reply])(implicit syncF: Sync[F]) = syncF.pure(replies)
}
case object RandomSelection extends ReplySelection {
  def logic[F[_]: Sync](replies: List[Reply]) =
    for {
      randomNumGen <- Random.scalaUtilRandom
      randomVal    <- randomNumGen.betweenInt(0, replies.size)
    } yield List(replies(randomVal))
}
