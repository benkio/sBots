package com.benkio.chatcore.messagefiltering

import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.model.reply.ReplyValue

import scala.util.Random

object RandomSelection {
  def select[F[_]: Sync](replies: List[ReplyValue]): F[ReplyValue] =
    for {
      randomNumGen <- Sync[F].delay(new Random())
      randomVal    <- Sync[F].delay(randomNumGen.between(0, replies.size))
    } yield replies(randomVal)
}
