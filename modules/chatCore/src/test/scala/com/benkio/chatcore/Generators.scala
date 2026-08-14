package com.benkio.chatcore

import com.benkio.chatcore.http.MegaClient.MegaUriComponents
import com.benkio.chatcore.messagefiltering.MessageMatches
import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.model.reply.GifFile
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.MediaReply
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PhotoFile
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.ReplyValueCore
import com.benkio.chatcore.model.reply.Sticker
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.reply.VideoFile
import com.benkio.chatcore.model.show.Show
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.LeftMemberTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageLengthTrigger
import com.benkio.chatcore.model.MessageTrigger
import com.benkio.chatcore.model.MimeType
import com.benkio.chatcore.model.NewMemberTrigger
import com.benkio.chatcore.model.RegexTextTriggerValue
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTrigger
import com.benkio.chatcore.model.TextTriggerValue
import com.benkio.chatcore.model.Trigger
import com.benkio.chatcore.model.User
import org.scalacheck.Gen

import java.time.LocalDate
import scala.concurrent.duration.*

trait Generators {

  val commandKeyGen: Gen[CommandKey] = Gen.oneOf(CommandKey.values.toSeq)

  val commandInputGen: Gen[(CommandKey, String)] = for {
    key        <- commandKeyGen
    withSlash  <- Gen.oneOf(true, false)
    withAtBot  <- Gen.oneOf(true, false)
    botSuffix  <- Gen.alphaNumStr.suchThat(_.nonEmpty)
    leadingWs  <- Gen.oneOf("", " ", "  ")
    trailingWs <- Gen.oneOf("", " ", "  ")
    cased      <- Gen.oneOf(key.asString, key.asString.toUpperCase, key.asString.capitalize)
    base = (if withSlash then "/" else "") + cased + (if withAtBot then s"@$botSuffix" else "")
  } yield key -> (leadingWs + base + trailingWs)

  val textTypeGen: Gen[Text.TextType] = Gen.oneOf(Text.TextType.values.toSeq)
  val textGen: Gen[Text]              = for {
    ttl      <- Gen.option(Gen.finiteDuration)
    textType <- textTypeGen
    value    <- Gen.alphaStr
  } yield Text(value = value, timeToLive = ttl, textType = textType)
  val gifGen: Gen[GifFile]         = Gen.alphaStr.map(s => GifFile(s + "Gif.mp4"))
  val mp3Gen: Gen[Mp3File]         = Gen.alphaStr.map(s => Mp3File(s + ".mp3"))
  val photoGen: Gen[PhotoFile]     = Gen.alphaStr.map(s => PhotoFile(s + ".jpg"))
  val videoGen: Gen[VideoFile]     = Gen.alphaStr.map(s => VideoFile(s + ".mp4"))
  val stickerGen: Gen[Sticker]     = Gen.alphaStr.map(s => Sticker(s + ".sticker"))
  val documentGen: Gen[Document]   = Gen.alphaStr.map(Document(_))
  val mediaFileGen: Gen[MediaFile] = Gen.oneOf(
    gifGen,
    mp3Gen,
    photoGen,
    videoGen,
    stickerGen,
    documentGen
  )
  val coreReplyValueCoreGen: Gen[ReplyValueCore] =
    Gen.oneOf(textGen, mediaFileGen)
  val coreReplyValueGen: Gen[ReplyValue] = coreReplyValueCoreGen

  val userGen: Gen[User] = for {
    id        <- Gen.long
    isBot     <- Gen.oneOf(false, true)
    firstName <- Gen.alphaStr
  } yield User(id, isBot, firstName)

  val stringTextTriggerValueGen: Gen[StringTextTriggerValue] =
    Gen
      .choose(3, 12)
      .flatMap(n => Gen.listOfN(n, Gen.alphaLowerChar).map(_.mkString))
      .map(StringTextTriggerValue.apply)

  val regexTextTriggerValueGen: Gen[RegexTextTriggerValue] = for {
    needle <- Gen
      .choose(3, 12)
      .flatMap(n => Gen.listOfN(n, Gen.alphaLowerChar).map(_.mkString))
    regex = ("\\Q" + needle + "\\E").r
  } yield RegexTextTriggerValue(regex, needle.length)

  val triggerValueGen: Gen[TextTriggerValue] =
    Gen.oneOf(stringTextTriggerValueGen, regexTextTriggerValueGen)

  val textTriggerGen: Gen[TextTrigger] =
    Gen.nonEmptyListOf(triggerValueGen).map(vs => TextTrigger(vs*))

  val messageTriggerGen: Gen[MessageTrigger] = Gen.oneOf(
    textTriggerGen,
    Gen.choose(0, 200).map(MessageLengthTrigger.apply),
    Gen.const(NewMemberTrigger),
    Gen.const(LeftMemberTrigger)
  )

  val triggerGen: Gen[Trigger] = Gen.oneOf(
    triggerValueGen.map(tv => TextTrigger(tv)),
    Gen.choose(0, 200).map(MessageLengthTrigger.apply),
    Gen.asciiPrintableStr.map(CommandTrigger.apply),
    Gen.const(NewMemberTrigger),
    Gen.const(LeftMemberTrigger)
  )

  val messageGen: Gen[Message] = for {
    messageId      <- Gen.choose(Int.MinValue, Int.MaxValue)
    date           <- Gen.long
    chatId         <- Gen.long
    chatType       <- Gen.alphaStr
    text           <- Gen.option(Gen.alphaStr)
    caption        <- Gen.option(Gen.alphaStr)
    newChatMembers <- Gen.listOf(userGen)
    leftChatMember <- Gen.option(userGen)
    isForward      <- Gen.oneOf(false, true)
  } yield Message(
    messageId = messageId,
    date = date,
    chatId = ChatId(chatId),
    chatType = chatType,
    text = text,
    caption = caption,
    newChatMembers = newChatMembers,
    leftChatMember = leftChatMember,
    isForward = isForward
  )

  val youtubeTimestampFiniteDurationGen: Gen[FiniteDuration] =
    Gen.chooseNum(0L, 48L * 3600L).map(_.seconds)

  val showGen: Gen[Show] = for {
    id             <- Gen.alphaNumStr.suchThat(_.nonEmpty)
    botId          <- Gen.alphaNumStr.suchThat(_.nonEmpty).map(SBotId(_))
    title          <- Gen.alphaStr
    year           <- Gen.chooseNum(2005, 2030)
    month          <- Gen.chooseNum(1, 12)
    day            <- Gen.chooseNum(1, 28)
    duration       <- Gen.chooseNum(1, 200000)
    description    <- Gen.option(Gen.alphaStr)
    isLive         <- Gen.oneOf(true, false)
    caption        <- Gen.option(Gen.alphaStr)
    timestamp      <- Gen.option(youtubeTimestampFiniteDurationGen)
    captionEntries <- Gen
      .containerOf[Vector, (FiniteDuration, String)](
        for {
          ts   <- youtubeTimestampFiniteDurationGen
          text <- Gen.alphaStr
        } yield ts -> text
      )
      .map(_.sortBy(_._1))
  } yield Show(
    id = id,
    botId = botId,
    title = title,
    uploadDate = LocalDate.of(year, month, day),
    duration = duration,
    description = description,
    isLive = isLive,
    originAutomaticCaption = caption,
    originAutomaticCaptionSrt = captionEntries,
    timestamp = timestamp
  )

  val messageMatchesGen: Gen[MessageMatches] =
    Gen.oneOf(MessageMatches.ContainsOnce, MessageMatches.ContainsAll)

  val replyBundleMessageGen: Gen[ReplyBundleMessage] = for {
    trigger <- messageTriggerGen
    media   <- Gen.nonEmptyListOf(mediaFileGen)
    matcher <- messageMatchesGen
  } yield ReplyBundleMessage(
    trigger = trigger,
    reply = MediaReply(mediaFiles = media.toSet),
    matcher = matcher
  )

  val timeoutHhMmSsGen: Gen[(String, FiniteDuration)] = for {
    hours   <- Gen.choose(0, 23)
    minutes <- Gen.choose(0, 59)
    seconds <- Gen.choose(0, 59)
    formatted =
      f"$hours%02d:$minutes%02d:$seconds%02d"
    duration = hours.hours + minutes.minutes + seconds.seconds
  } yield formatted -> duration

  val mimeTypeGen: Gen[MimeType] = Gen.oneOf(MimeType.values.toSeq)

  val mediaNameForMimeGen: Gen[(String, MimeType)] = Gen.oneOf(
    Gen.alphaNumStr.map(s => (s"$s.gif", MimeType.GIF)),
    Gen.alphaNumStr.map(s => (s"$s.jpg", MimeType.JPEG)),
    Gen.alphaNumStr.map(s => (s"$s.png", MimeType.PNG)),
    Gen.alphaNumStr.map(s => (s"$s.mp3", MimeType.MPEG)),
    Gen.alphaNumStr.map(s => (s"${s}gif.mp4", MimeType.GIF)),
    Gen.alphaNumStr.suchThat(s => !s.toLowerCase.endsWith("gif")).map(s => (s"$s.mp4", MimeType.MP4))
  )

  val sBotIdGen: Gen[SBotId] =
    Gen.alphaNumStr.suchThat(_.nonEmpty).map(SBotId(_))

  val megaUriComponentsGen: Gen[MegaUriComponents] = for {
    fileId     <- Gen.listOfN(8, Gen.alphaChar).map(_.mkString)
    decryptKey <- Gen.listOfN(8, Gen.alphaChar).map(_.mkString)
  } yield MegaUriComponents(
    fileId = fileId,
    decryptKey = decryptKey
  )

}

object Generators extends Generators
