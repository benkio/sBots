package com.benkio.chatcore

import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.model.reply.GifFile
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PhotoFile
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Sticker
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.reply.VideoFile
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.User
import org.scalacheck.Gen

object Generators {

  val commandKeyGen: Gen[CommandKey] = Gen.oneOf(CommandKey.values.toSeq)

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
  val replyValueGen: Gen[ReplyValue] = Gen.oneOf(textGen, mediaFileGen)

  val userGen: Gen[User] = for {
    id        <- Gen.long
    isBot     <- Gen.boolean
    firstName <- Gen.alphaStr
  } yield User(id, isBot, firstName)

  val messageGen: Gen[Message] = for {
    messageId       <- Gen.int
    date            <- Gen.long
    chatId          <- Gen.long
    chatType        <- Gen.alphaStr
    text            <- Gen.option(Gen.alphaStr)
    caption         <- Gen.option(Gen.alphaStr)
    newChatMembers  <- Gen.listOf(userGen)
    leftChatMember  <- Gen.option(userGen)
    isForward       <- Gen.boolean
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
}
