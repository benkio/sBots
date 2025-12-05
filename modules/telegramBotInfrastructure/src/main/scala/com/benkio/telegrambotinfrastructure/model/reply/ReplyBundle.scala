package com.benkio.telegrambotinfrastructure.model.reply

import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import cats.*
import scala.annotation.targetName

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

import scala.util.matching.Regex

sealed trait ReplyBundle {

  def trigger: Trigger
  def reply: Reply
  def sBotId: SBotId
}

case class ReplyBundleMessage private (
  trigger: MessageTrigger,
  reply: Reply,
  matcher: MessageMatches,
  sBotId: SBotId
) extends ReplyBundle

object ReplyBundleMessage {

  given replyBundleMessageDecoder: Decoder[ReplyBundleMessage] =
    deriveDecoder[ReplyBundleMessage]
  given replyBundleMessageEncoder: Encoder[ReplyBundleMessage] =
    deriveEncoder[ReplyBundleMessage]

  @targetName("ReplyBundleMessageApply")
  def apply(
      trigger: MessageTrigger,
      reply: Reply,
      matcher: MessageMatches = MessageMatches.ContainsOnce
  )(using sBotId: SBotId): ReplyBundleMessage = ReplyBundleMessage(
    trigger = trigger,
    reply = reply,
    matcher = matcher,
    sBotId = sBotId
  )

  def textToMedia(
      triggers: (String | Regex | RegexTextTriggerValue)*
  )(mediaFiles: MediaFile*)(using sBotId: SBotId): ReplyBundleMessage =
    ReplyBundleMessage(
      trigger = TextTrigger(triggers.map(TextTriggerValue.fromStringOrRegex)*),
      reply = MediaReply.fromList(mediaFiles = mediaFiles.toList)
    )

  def textToVideo(
      triggers: (String | Regex | RegexTextTriggerValue)*
  )(videoFiles: VideoFile*)(using sBotId: SBotId): ReplyBundleMessage =
    textToMedia(triggers*)(videoFiles*)

  def textToMp3(
      triggers: (String | Regex | RegexTextTriggerValue)*
  )(mp3Files: Mp3File*)(using sBotId: SBotId): ReplyBundleMessage =
    textToMedia(triggers*)(mp3Files*)

  def textToGif(
      triggers: (String | Regex | RegexTextTriggerValue)*
  )(gifFiles: GifFile*)(using sBotId: SBotId): ReplyBundleMessage =
    textToMedia(triggers*)(gifFiles*)

  def textToPhoto(
      triggers: (String | Regex | RegexTextTriggerValue)*
  )(photoFiles: PhotoFile*)(using sBotId: SBotId): ReplyBundleMessage =
    textToMedia(triggers*)(photoFiles*)

  def textToSticker(
      triggers: (String | Regex | RegexTextTriggerValue)*
  )(photoFiles: Sticker*)(using sBotId: SBotId): ReplyBundleMessage =
    textToMedia(triggers*)(photoFiles*)

  def textToText(
      triggers: (String | Regex | RegexTextTriggerValue)*
  )(texts: String*)(using sBotId: SBotId): ReplyBundleMessage =
    ReplyBundleMessage(
      trigger = TextTrigger(triggers.map(TextTriggerValue.fromStringOrRegex)*),
      reply = TextReply.fromList(texts*)(false)
    )
}

final case class ReplyBundleCommand private (
  trigger: CommandTrigger,
  reply: Reply,
  instruction: CommandInstructionData,
  sBotId: SBotId
) extends ReplyBundle

object ReplyBundleCommand {
  @targetName("ReplyBundleCommandApply")
  def apply(
      trigger: CommandTrigger,
      reply: Reply,
      instruction: CommandInstructionData
  )(using sBotId: SBotId): ReplyBundleCommand = ReplyBundleCommand(
    trigger = trigger,
    reply = reply,
    instruction = instruction,
    sBotId = sBotId
  )

  def textToMedia(trigger: String, instruction: CommandInstructionData)(
      mediaFiles: MediaFile*
  )(using sBotId: SBotId): ReplyBundleCommand =
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
