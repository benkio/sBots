package com.benkio.telegrambotinfrastructure.messagefiltering

import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.reply.EffectfulReply
import com.benkio.telegrambotinfrastructure.model.reply.MediaReply
import com.benkio.telegrambotinfrastructure.model.reply.Reply
import com.benkio.telegrambotinfrastructure.model.reply.ReplyValue
import com.benkio.telegrambotinfrastructure.model.reply.TextReply

import scala.util.Random

object RandomSelection {
  def select[F[_]: Sync](reply: Reply): F[List[ReplyValue]] =
    for {
      randomNumGen <- Sync[F].delay(new Random())
      replies = reply match {
        case TextReply(text, _)        => text
        case MediaReply(mediaFiles, _) => mediaFiles
        case EffectfulReply(key, _)    => List(key)
      }
      randomVal <- Sync[F].delay(randomNumGen.between(0, replies.size))
    } yield List(replies(randomVal))
}
