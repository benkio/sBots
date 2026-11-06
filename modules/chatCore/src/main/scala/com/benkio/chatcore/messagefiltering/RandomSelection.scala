package com.benkio.chatcore.messagefiltering

import cats.effect.*
import cats.effect.std.Random
import cats.implicits.*
import com.benkio.chatcore.model.reply.ReplyValue

object RandomSelection {
  def select[F[_]: Sync](replies: Iterable[ReplyValue]): F[ReplyValue] =
    Random.scalaUtilRandom[F].flatMap(_.elementOf(replies))
}
