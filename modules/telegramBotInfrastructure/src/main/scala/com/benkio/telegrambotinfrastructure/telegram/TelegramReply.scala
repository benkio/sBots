package com.benkio.telegrambotinfrastructure.telegram

import com.benkio.telegrambotinfrastructure.model.reply.Sticker
import com.benkio.telegrambotinfrastructure.model.reply.Document
import com.benkio.telegrambotinfrastructure.model.reply.ReplyValue
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.GifFile
import com.benkio.telegrambotinfrastructure.model.reply.Mp3File
import com.benkio.telegrambotinfrastructure.model.reply.VideoFile
import com.benkio.telegrambotinfrastructure.model.reply.PhotoFile
import com.benkio.telegrambotinfrastructure.model.reply.Text

import telegramium.bots.ChatIntId
import telegramium.bots.ChatId
import telegramium.bots.ReplyParameters
import telegramium.bots.IFile
import telegramium.bots.client.Method
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import telegramium.bots.high.*
import telegramium.bots.high.implicits.*
import log.effect.LogWriter
import telegramium.bots.Message
import cats.*
import cats.data.EitherT
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.media.toTelegramApi

trait TelegramReply[A] {
  def reply[F[_]: Async: LogWriter: Api](
      reply: A,
      msg: Message,
      resourceAccess: ResourceAccess[F],
      replyToMessage: Boolean
  ): F[List[Message]]
}

object TelegramReply:
  def telegramFileReplyPattern[F[_]: Async: LogWriter: Api](
      msg: Message,
      resourceAccess: ResourceAccess[F],
      chatAction: String,
      mediaFile: MediaFile,
      replyToMessage: Boolean,
      sendFileAPIMethod: (ChatId, IFile, Option[Int]) => Method[Message]
  ): F[List[Message]] = {
    val chatId: ChatId = ChatIntId(msg.chat.id)
    val result: EitherT[F, Throwable, List[Message]] = for {
      _ <- Methods.sendChatAction(chatId, chatAction).exec.attemptT
      message <-
        resourceAccess
          .getResourceFile(mediaFile)
          .use[Message](file =>
            sendFileAPIMethod(
              chatId,
              file.toTelegramApi,
              Option.when(replyToMessage)(msg.messageId)
            ).exec
          )
          .attemptT
    } yield List(message)
    result.getOrElse(List.empty)
  }

  given telegramReplyValue: TelegramReply[ReplyValue] = new TelegramReply[ReplyValue] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: ReplyValue,
        msg: Message,
        resourceAccess: ResourceAccess[F],
        replyToMessage: Boolean
    ): F[List[Message]] = reply match {
      case mp3: Mp3File       => telegramMp3Reply.reply(mp3, msg, resourceAccess, replyToMessage)
      case gif: GifFile       => telegramGifReply.reply(gif, msg, resourceAccess, replyToMessage)
      case photo: PhotoFile   => telegramPhotoReply.reply(photo, msg, resourceAccess, replyToMessage)
      case video: VideoFile   => telegramVideoReply.reply(video, msg, resourceAccess, replyToMessage)
      case document: Document => telegramDocumentReply.reply(document, msg, resourceAccess, replyToMessage)
      case sticker: Sticker   => telegramStickerReply.reply(sticker, msg, resourceAccess, replyToMessage)
      case text: Text         => telegramTextReply.reply(text, msg, resourceAccess, replyToMessage)
    }
  }

  given telegramMp3Reply: TelegramReply[Mp3File] = new TelegramReply[Mp3File] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: Mp3File,
        msg: Message,
        resourceAccess: ResourceAccess[F],
        replyToMessage: Boolean
    ): F[List[Message]] = {
      TelegramReply.telegramFileReplyPattern[F](
        msg = msg,
        resourceAccess = resourceAccess,
        "upload_voice",
        mediaFile = reply,
        replyToMessage = replyToMessage,
        sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
          Methods.sendAudio(
            chatId = chatId,
            audio = ifile,
            messageThreadId = replyToMessageId
          )
      )
    }
  }

  given telegramGifReply: TelegramReply[GifFile] = new TelegramReply[GifFile] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: GifFile,
        msg: Message,
        resourceAccess: ResourceAccess[F],
        replyToMessage: Boolean
    ): F[List[Message]] = {
      TelegramReply.telegramFileReplyPattern[F](
        msg = msg,
        resourceAccess = resourceAccess,
        "upload_document",
        mediaFile = reply,
        replyToMessage = replyToMessage,
        sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
          Methods.sendAnimation(
            chatId = chatId,
            animation = ifile,
            messageThreadId = replyToMessageId
          )
      )
    }
  }

  given telegramPhotoReply: TelegramReply[PhotoFile] = new TelegramReply[PhotoFile] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: PhotoFile,
        msg: Message,
        resourceAccess: ResourceAccess[F],
        replyToMessage: Boolean
    ): F[List[Message]] = {
      TelegramReply.telegramFileReplyPattern[F](
        msg = msg,
        resourceAccess = resourceAccess,
        "upload_photo",
        mediaFile = reply,
        replyToMessage = replyToMessage,
        sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
          Methods.sendPhoto(
            chatId = chatId,
            photo = ifile,
            messageThreadId = replyToMessageId
          )
      )
    }
  }

  given telegramVideoReply: TelegramReply[VideoFile] = new TelegramReply[VideoFile] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: VideoFile,
        msg: Message,
        resourceAccess: ResourceAccess[F],
        replyToMessage: Boolean
    ): F[List[Message]] = {
      TelegramReply.telegramFileReplyPattern[F](
        msg = msg,
        resourceAccess = resourceAccess,
        "upload_video",
        mediaFile = reply,
        replyToMessage = replyToMessage,
        sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
          Methods.sendVideo(
            chatId = chatId,
            video = ifile,
            messageThreadId = replyToMessageId
          )
      )
    }
  }

  given telegramDocumentReply: TelegramReply[Document] = new TelegramReply[Document] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: Document,
        msg: Message,
        resourceAccess: ResourceAccess[F],
        replyToMessage: Boolean
    ): F[List[Message]] = {
      TelegramReply.telegramFileReplyPattern[F](
        msg = msg,
        resourceAccess = resourceAccess,
        "upload_video",
        mediaFile = reply,
        replyToMessage = replyToMessage,
        sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
          Methods.sendDocument(
            chatId = chatId,
            document = ifile,
            messageThreadId = replyToMessageId
          )
      )
    }
  }

  given telegramStickerReply: TelegramReply[Sticker] = new TelegramReply[Sticker] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: Sticker,
        msg: Message,
        resourceAccess: ResourceAccess[F],
        replyToMessage: Boolean
    ): F[List[Message]] = {
      TelegramReply.telegramFileReplyPattern[F](
        msg = msg,
        resourceAccess = resourceAccess,
        "upload_video",
        mediaFile = reply,
        replyToMessage = replyToMessage,
        sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
          Methods.sendSticker(
            chatId = chatId,
            sticker = ifile,
            messageThreadId = replyToMessageId
          )
      )
    }
  }

  given telegramTextReply: TelegramReply[Text] = new TelegramReply[Text] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: Text,
        msg: Message,
        resourceAccess: ResourceAccess[F],
        replyToMessage: Boolean
    ): F[List[Message]] = {
      val chatId: ChatId = ChatIntId(msg.chat.id)
      val result: EitherT[F, Throwable, List[Message]] =
        for {
          _ <- Methods.sendChatAction(chatId, "typing").exec.attemptT
          message <-
            Methods
              .sendMessage(
                chatId = chatId,
                text = reply.value,
                replyParameters = Option.when(replyToMessage)(ReplyParameters(msg.messageId))
              )
              .exec
              .attemptT
        } yield List(message)
      result.getOrElse(List.empty)
    }
  }
end TelegramReply
