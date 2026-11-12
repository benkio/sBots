package com.benkio.chatcore.model.reply

import cats.*
import cats.syntax.all.*
import com.benkio.chatcore.messagefiltering.FilteringForward
import com.benkio.chatcore.messagefiltering.FilteringOlder
import com.benkio.chatcore.messagefiltering.MessageMatches
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageTrigger
import com.benkio.chatcore.model.RegexTextTriggerValue
import com.benkio.chatcore.model.SBotInfo.SBotName
import com.benkio.chatcore.model.TextTrigger
import com.benkio.chatcore.model.TextTriggerValue
import com.benkio.chatcore.model.Trigger
import io.circe.*
import io.circe.generic.semiauto.*

sealed trait ReplyBundle {

  def trigger: Trigger
  def reply: Reply
}

case class ReplyBundleMessage(
    trigger: MessageTrigger,
    reply: PlainReply,
    matcher: MessageMatches
) extends ReplyBundle

object ReplyBundleMessage {

  given replyBundleMessageDecoder: Decoder[ReplyBundleMessage] =
    deriveDecoder[ReplyBundleMessage]
  given replyBundleMessageEncoder: Encoder[ReplyBundleMessage] =
    deriveEncoder[ReplyBundleMessage]

  def selectReplyBundle(
      msg: Message,
      messageRepliesData: List[ReplyBundleMessage],
      ignoreMessagePrefix: Option[String],
      disableForward: Boolean
  ): Option[ReplyBundleMessage] =
    if !FilteringForward.filter(msg, disableForward) || !FilteringOlder.filter(msg)
    then None
    else bestMatch(msg = msg, messageRepliesData = messageRepliesData, ignoreMessagePrefix = ignoreMessagePrefix)

  /** The reply bundle a message with the given text would get, with none of the filtering of a live message: the text
    * is not a message of the chat (e.g. the target of a trigger subscription), so there is neither the ignore prefix
    * nor the forward and age filters.
    */
  def selectReplyBundleByText(
      text: String,
      chatId: ChatId,
      messageRepliesData: List[ReplyBundleMessage]
  ): Option[ReplyBundleMessage] =
    bestMatch(
      msg = Message(messageId = None, date = 0L, chatId = chatId, chatType = "", text = Some(text)),
      messageRepliesData = messageRepliesData,
      ignoreMessagePrefix = None
    )

  private def bestMatch(
      msg: Message,
      messageRepliesData: List[ReplyBundleMessage],
      ignoreMessagePrefix: Option[String]
  ): Option[ReplyBundleMessage] =
    messageRepliesData
      .mapFilter(messageReplyBundle =>
        MessageMatches
          .doesMatch(messageReplyBundle, msg, ignoreMessagePrefix)
      )
      .sortBy(_._1)(using Trigger.orderingInstance.reverse)
      .headOption
      .map(_._2)

  def textToMedia(
      triggers: (String | RegexTextTriggerValue)*
  )(mediaFiles: MediaFile*): ReplyBundleMessage =
    ReplyBundleMessage(
      trigger = TextTrigger(triggers.map(TextTriggerValue.fromStringOrRegex)*),
      reply = MediaReply.fromList(mediaFiles = mediaFiles.toList),
      matcher = MessageMatches.ContainsOnce
    )

  def textToVideo(
      triggers: (String | RegexTextTriggerValue)*
  )(videoFiles: VideoFile*): ReplyBundleMessage =
    textToMedia(triggers*)(videoFiles*)

  def textToMp3(
      triggers: (String | RegexTextTriggerValue)*
  )(mp3Files: Mp3File*): ReplyBundleMessage =
    textToMedia(triggers*)(mp3Files*)

  def textToGif(
      triggers: (String | RegexTextTriggerValue)*
  )(gifFiles: GifFile*): ReplyBundleMessage =
    textToMedia(triggers*)(gifFiles*)

  def textToPhoto(
      triggers: (String | RegexTextTriggerValue)*
  )(photoFiles: PhotoFile*): ReplyBundleMessage =
    textToMedia(triggers*)(photoFiles*)

  def textToSticker(
      triggers: (String | RegexTextTriggerValue)*
  )(photoFiles: Sticker*): ReplyBundleMessage =
    textToMedia(triggers*)(photoFiles*)

  def textToText(
      triggers: (String | RegexTextTriggerValue)*
  )(texts: String*): ReplyBundleMessage =
    ReplyBundleMessage(
      trigger = TextTrigger(triggers.map(TextTriggerValue.fromStringOrRegex)*),
      reply = TextReply.fromList(texts*)(false),
      matcher = MessageMatches.ContainsOnce
    )
}

final case class ReplyBundleCommand(
    trigger: CommandTrigger,
    reply: Reply,
    instruction: CommandInstructionData
) extends ReplyBundle

object ReplyBundleCommand {

  given replyBundleCommandDecoder: Decoder[ReplyBundleCommand] =
    deriveDecoder[ReplyBundleCommand]
  given replyBundleCommandEncoder: Encoder[ReplyBundleCommand] =
    deriveEncoder[ReplyBundleCommand]

  /** The command the message invokes, written with the command syntax of the platform it comes from (Telegram unless
    * said otherwise).
    */
  def selectCommandReplyBundle(
      msg: Message,
      allCommandRepliesData: List[ReplyBundleCommand],
      botName: SBotName,
      syntax: CommandSyntax = CommandSyntax.Telegram
  ): Option[ReplyBundleCommand] =
    msg.text.flatMap(text =>
      allCommandRepliesData.find(rbc => syntax.isInvocationOf(text, rbc.trigger.command, botName))
    )

  def selectCommandReplyBundle(
      commandKey: CommandKey,
      allCommandRepliesData: List[ReplyBundleCommand]
  ): Option[ReplyBundleCommand] =
    allCommandRepliesData.find(rbc => rbc.trigger.command == commandKey.asString)

  def from(
      commandKey: CommandKey,
      allCommandRepliesData: List[ReplyBundleCommand]
  ): Option[ReplyBundleCommand] =
    selectCommandReplyBundle(commandKey = commandKey, allCommandRepliesData = allCommandRepliesData)

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

  private def mdTriggerCell(trigger: String): String =
    trigger.replace("|", "\\|")

  private def mdTableRow(left: String, trigger: String): String = {
    val leftPadded = left.padTo(64, ' ')
    s"| $leftPadded | ${mdTriggerCell(trigger)}".padTo(255, ' ') + " |"
  }

  extension (rb: ReplyBundle) {
    def prettyPrint()(using triggerShow: Show[Trigger]): String = {
      val emptyLine: String            = mdTableRow(" " * 64, "")
      val triggerStrings: List[String] = triggerShow.show(rb.trigger).split('\n').toList
      val result                       = rb.reply.prettyPrint
        .zipAll(that = triggerStrings, thisElem = "", thatElem = "")
        .map { case (mfs, trs) => mdTableRow(mfs, trs) }
        .mkString("\n")
      s"$result\n" + emptyLine + "\n"
    }

    def containsMediaReply(r: ReplyBundle): Boolean =
      r.reply match {
        case MediaReply(_, _) => true
        case _                => false
      }

    def getMediaFiles: Set[MediaFile] = rb.reply match {
      case MediaReply(mediaFiles, _) => mediaFiles
      case _                         => Set.empty
    }
  }
}
