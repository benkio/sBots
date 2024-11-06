package com.benkio.telegrambotinfrastructure.model

import com.benkio.telegrambotinfrastructure.telegram.TelegramReply
import telegramium.bots.high.Api
import log.effect.LogWriter

import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import telegramium.bots.Message
import com.benkio.telegrambotinfrastructure.model.Reply.{given, *}
import io.circe.*
import io.circe.generic.semiauto.*

sealed trait ReplyBundle[F[_]] {

  def trigger: Trigger
  def reply: Reply[F]
  def replySelection: ReplySelection
}

final case class ReplyBundleMessage[F[_]](
    trigger: MessageTrigger,
    reply: Reply[F],
    matcher: MessageMatches,
    replySelection: ReplySelection
) extends ReplyBundle[F]

object ReplyBundleMessage {

  given replyBundleMessageDecoder[F[_]: Applicative]: Decoder[ReplyBundleMessage[F]] =
    deriveDecoder[ReplyBundleMessage[F]]
  given replyBundleMessageEncoder: Encoder[ReplyBundleMessage[SyncIO]] =
    deriveEncoder[ReplyBundleMessage[SyncIO]]

  def apply[F[_]](
      trigger: MessageTrigger,
      reply: Reply[F],
      matcher: MessageMatches = MessageMatches.ContainsOnce,
      replySelection: ReplySelection = RandomSelection
  ): ReplyBundleMessage[F] = new ReplyBundleMessage[F](
    trigger = trigger,
    reply = reply,
    matcher = matcher,
    replySelection = replySelection
  )

  def textToMedia[F[_]: Applicative](
      triggers: (String | RegexTextTriggerValue)*
  )(mediaFiles: MediaFile*): ReplyBundleMessage[F] =
    ReplyBundleMessage[F](
      trigger = TextTrigger(triggers.map(TextTriggerValue.fromStringOrRegex)*),
      reply = MediaReply.fromList[F](mediaFiles = mediaFiles.toList)
    )

  def textToVideo[F[_]: Applicative](
      triggers: (String | RegexTextTriggerValue)*
  )(videoFiles: VideoFile*): ReplyBundleMessage[F] =
    textToMedia(triggers*)(videoFiles*)

  def textToMp3[F[_]: Applicative](
      triggers: (String | RegexTextTriggerValue)*
  )(mp3Files: Mp3File*): ReplyBundleMessage[F] =
    textToMedia(triggers*)(mp3Files*)

  def textToGif[F[_]: Applicative](
      triggers: (String | RegexTextTriggerValue)*
  )(gifFiles: GifFile*): ReplyBundleMessage[F] =
    textToMedia(triggers*)(gifFiles*)

  def textToPhoto[F[_]: Applicative](
      triggers: (String | RegexTextTriggerValue)*
  )(photoFiles: PhotoFile*): ReplyBundleMessage[F] =
    textToMedia(triggers*)(photoFiles*)

  def textToText[F[_]: Applicative](
      triggers: (String | RegexTextTriggerValue)*
  )(texts: String*): ReplyBundleMessage[F] =
    ReplyBundleMessage[F](
      trigger = TextTrigger(triggers.map(TextTriggerValue.fromStringOrRegex)*),
      reply = TextReply.fromList[F](texts*)(false)
    )
}

final case class ReplyBundleCommand[F[_]](
    trigger: CommandTrigger,
    reply: Reply[F],
    replySelection: ReplySelection
) extends ReplyBundle[F]

object ReplyBundleCommand {
  def apply[F[_]](
      trigger: CommandTrigger,
      reply: Reply[F],
      replySelection: ReplySelection = RandomSelection
  ): ReplyBundleCommand[F] = new ReplyBundleCommand[F](
    trigger = trigger,
    reply = reply,
    replySelection = replySelection
  )

  def textToMedia[F[_]: Applicative](trigger: String)(mediaFiles: MediaFile*): ReplyBundleCommand[F] =
    ReplyBundleCommand[F](
      trigger = CommandTrigger(trigger),
      reply = MediaReply.fromList[F](mediaFiles = mediaFiles.toList)
    )
}

object ReplyBundle {

  given orderingInstance[F[_]]: Ordering[ReplyBundle[F]] =
    Trigger.orderingInstance.contramap(_.trigger)

  def prettyPrint[F[_]: Applicative](rb: ReplyBundle[F])(using triggerShow: Show[Trigger]): F[String] = {
    val triggerStrings: List[String] = triggerShow.show(rb.trigger).split('\n').toList
    val replyString: F[List[String]] = rb.reply.prettyPrint
    val result = replyString.map(x =>
      x.zipAll(triggerStrings, "", "")
        .map { case (mfs, trs) =>
          s"${mfs.padTo(25, ' ')} | $trs"
        }
        .mkString("\n")
    )
    result.map(r => ("-" * 50) + s"\n$r\n" + ("-" * 50) + "\n")
  }

  def containsMediaReply[F[_]](r: ReplyBundle[F]): Boolean =
    r.reply match {
      case MediaReply(_, _) => true
      case _                => false
    }

  def getMediaFiles[F[_]: Applicative](r: ReplyBundle[F]): F[List[MediaFile]] = r.reply match {
    case MediaReply(mediaFiles, _) => mediaFiles
    case _                         => List.empty.pure[F]
  }

  def computeReplyBundle[F[_]: Async: LogWriter: Api](
      replyBundle: ReplyBundle[F],
      message: Message,
      filter: F[Boolean],
      resourceAccess: ResourceAccess[F]
  )(using telegramReply: TelegramReply[ReplyValue]): F[List[Message]] = for {
    dataToReply <- Async[F].ifM(filter)(
      ifTrue = Async[F].pure(replyBundle.reply),
      ifFalse = Async[F].raiseError(new Exception(s"No replies for the given message: $message"))
    )
    replies <- replyBundle.replySelection.logic(dataToReply, message)
    result <- replies.traverse[F, List[Message]](reply =>
      telegramReply.reply[F](
        reply = reply,
        msg = message,
        resourceAccess = resourceAccess,
        replyToMessage = replyBundle.reply.replyToMessage
      )
    )
  } yield result.flatten
}
