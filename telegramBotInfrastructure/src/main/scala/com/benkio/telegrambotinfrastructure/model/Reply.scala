package com.benkio.telegrambotinfrastructure.model

import com.benkio.telegrambotinfrastructure.model.Text.toText
import cats.Applicative
import cats.syntax.all._


import telegramium.bots.Message

sealed trait Reply[F[_]] {
  val replyToMessage: Boolean
}

final case class TextReplyM[F[_]](
    textM: Message => F[List[Text]],
    replyToMessage: Boolean = false
) extends Reply[F]

object TextReplyM:
    def createNoMessage[F[_]:Applicative](values: String*)(
      replyToMessage: Boolean
    ): TextReplyM[F] =
      TextReplyM(
        textM = (_: Message) => Applicative[F].pure(values.toList.toText),
        replyToMessage = replyToMessage
      )

final case class TextReply[F[_]](
    text: F[List[Text]],
    replyToMessage: Boolean = false
) extends Reply[F]

object TextReply:
    def createNoMessage[F[_]:Applicative](values: String*)(
      replyToMessage: Boolean
    ): TextReply[F] =
      TextReply(
        text = Applicative[F].pure(values.toList.toText),
        replyToMessage = replyToMessage
      )

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
