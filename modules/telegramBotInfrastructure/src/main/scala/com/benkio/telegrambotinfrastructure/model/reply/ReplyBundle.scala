package com.benkio.telegrambotinfrastructure.model.reply

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.model.CommandInstructionData
import com.benkio.telegrambotinfrastructure.model.CommandTrigger
import com.benkio.telegrambotinfrastructure.model.MessageTrigger
import com.benkio.telegrambotinfrastructure.model.RegexTextTriggerValue
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import com.benkio.telegrambotinfrastructure.model.TextTriggerValue
import com.benkio.telegrambotinfrastructure.model.Trigger
import io.circe.*
import io.circe.generic.semiauto.*

sealed trait ReplyBundle[F[_]] {

  def trigger: Trigger
  def reply: Reply[F]
}

final case class ReplyBundleMessage[F[_]](trigger: MessageTrigger, reply: Reply[F], matcher: MessageMatches)
    extends ReplyBundle[F]

object ReplyBundleMessage {

  given replyBundleMessageDecoder[F[_]: Applicative]: Decoder[ReplyBundleMessage[F]] =
    deriveDecoder[ReplyBundleMessage[F]]
  given replyBundleMessageEncoder: Encoder[ReplyBundleMessage[SyncIO]] =
    deriveEncoder[ReplyBundleMessage[SyncIO]]

  def apply[F[_]](
      trigger: MessageTrigger,
      reply: Reply[F],
      matcher: MessageMatches = MessageMatches.ContainsOnce
  ): ReplyBundleMessage[F] = new ReplyBundleMessage[F](
    trigger = trigger,
    reply = reply,
    matcher = matcher
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

  def textToSticker[F[_]: Applicative](
      triggers: (String | RegexTextTriggerValue)*
  )(photoFiles: Sticker*): ReplyBundleMessage[F] =
    textToMedia(triggers*)(photoFiles*)

  def textToText[F[_]](
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
    instruction: CommandInstructionData
) extends ReplyBundle[F]

object ReplyBundleCommand {
  def apply[F[_]](
      trigger: CommandTrigger,
      reply: Reply[F],
      instruction: CommandInstructionData
  ): ReplyBundleCommand[F] = new ReplyBundleCommand[F](
    trigger = trigger,
    reply = reply,
    instruction = instruction
  )

  def textToMedia[F[_]: Applicative](trigger: String, instruction: CommandInstructionData)(
      mediaFiles: MediaFile*
  ): ReplyBundleCommand[F] =
    ReplyBundleCommand[F](
      trigger = CommandTrigger(trigger),
      reply = MediaReply.fromList[F](mediaFiles = mediaFiles.toList),
      instruction = instruction
    )
}

object ReplyBundle {

  given orderingInstance[F[_]]: Ordering[ReplyBundle[F]] =
    Trigger.orderingInstance.contramap(_.trigger)

  extension [F[_]: ApplicativeThrow](rb: ReplyBundle[F])
    def prettyPrint()(using triggerShow: Show[Trigger]): F[String] = {
      val triggerStrings: List[String] = triggerShow.show(rb.trigger).split('\n').toList
      rb.reply.prettyPrint
        .handleError(_ => List.empty)
        .map(x =>
          val r = x
            .zipAll(triggerStrings, "", "")
            .map { case (mfs, trs) =>
              s"${mfs.padTo(25, ' ')} | $trs"
            }
            .mkString("\n")
          ("-" * 50) + s"\n$r\n" + ("-" * 50) + "\n"
        )
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
}
