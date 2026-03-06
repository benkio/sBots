package com.benkio.chatcore.messagefiltering

import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.MediaReply
import com.benkio.chatcore.model.reply.Reply
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.TextReply

import scala.util.Random

object RandomSelection {
  def select[F[_]: Sync](reply: Reply): F[ReplyValue] =
    for {
      randomNumGen <- Sync[F].delay(new Random())
      replies = reply match {
        case TextReply(text, _)        => text
        case MediaReply(mediaFiles, _) => mediaFiles
        case EffectfulReply(key, _)    => List(key)
      }
      randomVal <- Sync[F].delay(randomNumGen.between(0, replies.size))
    } yield replies(randomVal)
}
