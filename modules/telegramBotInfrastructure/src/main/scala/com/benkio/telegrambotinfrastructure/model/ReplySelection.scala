package com.benkio.telegrambotinfrastructure.model

import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.reply.MediaReply
import com.benkio.telegrambotinfrastructure.model.reply.Reply
import com.benkio.telegrambotinfrastructure.model.reply.ReplyValue
import com.benkio.telegrambotinfrastructure.model.reply.TextReply
import com.benkio.telegrambotinfrastructure.model.reply.TextReplyM
import io.circe.*
import telegramium.bots.Message

import scala.util.Random

sealed trait ReplySelection {
  def logic[F[_]: Sync](reply: Reply[F], message: Message): F[List[ReplyValue]]
}

case object SelectAll extends ReplySelection {
  def logic[F[_]: Sync](reply: Reply[F], message: Message): F[List[ReplyValue]] =
    reply match {
      case TextReply(text, _)        => Sync[F].pure(text)
      case TextReplyM(textM, _)      => Sync[F].widen(textM(message))
      case MediaReply(mediaFiles, _) => Sync[F].widen(mediaFiles)
    }
}
case object RandomSelection extends ReplySelection {
  def logic[F[_]: Sync](reply: Reply[F], message: Message): F[List[ReplyValue]] =
    for {
      randomNumGen <- Sync[F].delay(new Random())
      replies      <- SelectAll.logic[F](reply, message)
      randomVal    <- Sync[F].delay(randomNumGen.between(0, replies.size))
    } yield List(replies(randomVal))
}

extension (replySelection: ReplySelection)
  def isRandomSelection: Boolean = replySelection match {
    case RandomSelection => true
    case _               => false
  }
  def isSelectAllSelection: Boolean = replySelection match {
    case SelectAll => true
    case _         => false
  }

object ReplySelection {
  given Decoder[ReplySelection] =
    Decoder.decodeString.emap(s =>
      s match {
        case "SelectAll"       => Right(SelectAll)
        case "RandomSelection" => Right(RandomSelection)
        case _                 => Left(s"$s not recognized when decoding `ReplySelection`")
      }
    )
  given Encoder[ReplySelection] = Encoder[ReplySelection](rs => Json.fromString(rs.toString))
}
