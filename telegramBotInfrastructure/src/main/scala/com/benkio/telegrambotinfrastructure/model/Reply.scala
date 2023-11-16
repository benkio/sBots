package com.benkio.telegrambotinfrastructure.model

import cats.Applicative
import cats.Show
import cats.implicits._
import telegramium.bots.Message

sealed trait Reply[F[_]] {
  val replyToMessage: Boolean
}

final case class TextReplyM[F[_]](
    textM: Message => F[List[Text]],
    replyToMessage: Boolean = false
) extends Reply[F]

final case class TextReply[F[_]](
    text: F[List[Text]],
    replyToMessage: Boolean = false
) extends Reply[F]

final case class MediaReply[F[_]](
  mediaFiles: F[List[MediaFile]],
  replyToMessage: Boolean = false
) extends Reply[F]

object Reply:
  extension [F[_]: Applicative](r: Reply[F])
    def prettyPrint: F[List[String]] = r match {
      case TextReply(textF,_) => textF.map(txt => txt.map(_.show))
      case TextReplyM(_,_) => Applicative[F].pure(List.empty)
      case MediaReply(mediaFilesF,_) => mediaFilesF.map(mfs => mfs.map(_.show))
}
