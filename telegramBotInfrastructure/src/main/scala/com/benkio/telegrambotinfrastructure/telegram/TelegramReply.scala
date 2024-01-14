package com.benkio.telegrambotinfrastructure.telegram

import com.benkio.telegrambotinfrastructure.model.ReplyValue
import telegramium.bots.InputPartFile
import telegramium.bots.ChatIntId
import com.benkio.telegrambotinfrastructure.model.*
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
              InputPartFile(file),
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
      case mp3: Mp3File     => telegramMp3Reply.reply(mp3, msg, resourceAccess, replyToMessage)
      case gif: GifFile     => telegramGifReply.reply(gif, msg, resourceAccess, replyToMessage)
      case photo: PhotoFile => telegramPhotoReply.reply(photo, msg, resourceAccess, replyToMessage)
      case video: VideoFile => telegramVideoReply.reply(video, msg, resourceAccess, replyToMessage)
      case text: Text       => telegramTextReply.reply(text, msg, resourceAccess, replyToMessage)
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
            chatId,
            ifile,
            replyToMessageId
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
            chatId,
            ifile,
            replyToMessageId
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
            chatId,
            ifile,
            replyToMessageId
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
            chatId,
            ifile,
            replyToMessageId
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
