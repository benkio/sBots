package com.benkio.telegrambotinfrastructure.model.reply


import cats.*
import cats.syntax.all.*
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

import scala.util.matching.Regex

sealed trait ReplyBundle {

  def trigger: Trigger
  def reply: Reply
}

case class ReplyBundleMessage private (
  trigger: MessageTrigger,
  reply: Reply,
  matcher: MessageMatches,
) extends ReplyBundle

object ReplyBundleMessage {

  given replyBundleMessageDecoder: Decoder[ReplyBundleMessage] =
    deriveDecoder[ReplyBundleMessage]
  given replyBundleMessageEncoder: Encoder[ReplyBundleMessage] =
    deriveEncoder[ReplyBundleMessage]

  def textToMedia(
      triggers: (String | Regex | RegexTextTriggerValue)*
  )(mediaFiles: MediaFile*): ReplyBundleMessage =
    ReplyBundleMessage(
      trigger = TextTrigger(triggers.map(TextTriggerValue.fromStringOrRegex)*),
      reply = MediaReply.fromList(mediaFiles = mediaFiles.toList),
      matcher = MessageMatches.ContainsAll
    )

  def textToVideo(
      triggers: (String | Regex | RegexTextTriggerValue)*
  )(videoFiles: VideoFile*): ReplyBundleMessage =
    textToMedia(triggers*)(videoFiles*)

  def textToMp3(
      triggers: (String | Regex | RegexTextTriggerValue)*
  )(mp3Files: Mp3File*): ReplyBundleMessage =
    textToMedia(triggers*)(mp3Files*)

  def textToGif(
      triggers: (String | Regex | RegexTextTriggerValue)*
  )(gifFiles: GifFile*): ReplyBundleMessage =
    textToMedia(triggers*)(gifFiles*)

  def textToPhoto(
      triggers: (String | Regex | RegexTextTriggerValue)*
  )(photoFiles: PhotoFile*): ReplyBundleMessage =
    textToMedia(triggers*)(photoFiles*)

  def textToSticker(
      triggers: (String | Regex | RegexTextTriggerValue)*
  )(photoFiles: Sticker*): ReplyBundleMessage =
    textToMedia(triggers*)(photoFiles*)

  def textToText(
      triggers: (String | Regex | RegexTextTriggerValue)*
  )(texts: String*): ReplyBundleMessage =
    ReplyBundleMessage(
      trigger = TextTrigger(triggers.map(TextTriggerValue.fromStringOrRegex)*),
      reply = TextReply.fromList(texts*)(false),
      matcher = MessageMatches.ContainsAll
    )
}

final case class ReplyBundleCommand (
  trigger: CommandTrigger,
  reply: Reply,
  instruction: CommandInstructionData
) extends ReplyBundle

object ReplyBundleCommand {

  given replyBundleCommandDecoder: Decoder[ReplyBundleCommand] =
    deriveDecoder[ReplyBundleCommand]
  given replyBundleCommandEncoder: Encoder[ReplyBundleCommand] =
    deriveEncoder[ReplyBundleCommand]

  def textToMedia(trigger: String, instruction: CommandInstructionData)(
      mediaFiles: MediaFile*
  ): ReplyBundleCommand =
    ReplyBundleCommand(
      trigger = CommandTrigger(trigger),
      reply = MediaReply.fromList(mediaFiles = mediaFiles.toList),
      instruction = instruction
    )
}

object ReplyBundle {

  given orderingInstance: Ordering[ReplyBundle] =
    Trigger.orderingInstance.contramap(_.trigger)

  extension (rb: ReplyBundle) {
    def prettyPrint()(using triggerShow: Show[Trigger]): String = {
      val triggerStrings: List[String] = triggerShow.show(rb.trigger).split('\n').toList
      val result = rb.reply.prettyPrint
        .zipAll(that = triggerStrings, thisElem = "", thatElem = "")
        .map { case (mfs, trs) =>
          s"${mfs.padTo(25, ' ')} | $trs"
        }
        .mkString("\n")
      ("-" * 50) + s"\n$result\n" + ("-" * 50) + "\n"
    }

    def containsMediaReply(r: ReplyBundle): Boolean =
      r.reply match {
        case MediaReply(_, _) => true
        case _                => false
      }

    def getMediaFiles(r: ReplyBundle): List[MediaFile] = r.reply match {
      case MediaReply(mediaFiles, _) => mediaFiles
      case _                         => List.empty
    }
  }
}
