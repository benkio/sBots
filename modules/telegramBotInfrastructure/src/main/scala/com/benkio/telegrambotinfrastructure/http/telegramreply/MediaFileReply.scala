package com.benkio.telegrambotinfrastructure.http.telegramreply

import cats.effect.*
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.model.reply.Document
import com.benkio.telegrambotinfrastructure.model.reply.GifFile
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.Mp3File
import com.benkio.telegrambotinfrastructure.model.reply.PhotoFile
import com.benkio.telegrambotinfrastructure.model.reply.Sticker
import com.benkio.telegrambotinfrastructure.model.reply.VideoFile
import com.benkio.telegrambotinfrastructure.model.Message
import com.benkio.telegrambotinfrastructure.model.Message.toModel
import com.benkio.telegrambotinfrastructure.model.Message.toTelegramium
import com.benkio.telegrambotinfrastructure.repository.Repository
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.Message as TMessage

object MediaFileReply {

  def sendMediaFile[F[_]: Async: LogWriter: Api](
      reply: MediaFile,
      msg: Message,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[Message]] = (reply match {
    case mp3: Mp3File =>
      sendMp3(
        reply = mp3,
        msg = msg.toTelegramium,
        repository = repository,
        replyToMessage = replyToMessage
      )
    case gif: GifFile =>
      sendGif(
        reply = gif,
        msg = msg.toTelegramium,
        repository = repository,
        replyToMessage = replyToMessage
      )
    case photo: PhotoFile =>
      sendPhoto(
        reply = photo,
        msg = msg.toTelegramium,
        repository = repository,
        replyToMessage = replyToMessage
      )
    case video: VideoFile =>
      sendVideo(
        reply = video,
        msg = msg.toTelegramium,
        repository = repository,
        replyToMessage = replyToMessage
      )
    case document: Document =>
      sendDocument(
        reply = document,
        msg = msg.toTelegramium,
        repository = repository,
        replyToMessage = replyToMessage
      )
    case sticker: Sticker =>
      sendSticker(
        reply = sticker,
        msg = msg.toTelegramium,
        repository = repository,
        replyToMessage = replyToMessage
      )
  }).map(_.map(_.toModel))

  def sendMp3[F[_]: Async: LogWriter: Api](
      reply: Mp3File,
      msg: TMessage,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] = {
    TelegramReply.telegramFileReplyPattern[F](
      msg = msg,
      repository = repository,
      chatAction = "upload_voice",
      mediaFile = reply,
      replyToMessage = replyToMessage,
      sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
        Methods.sendAudio(
          chatId = chatId,
          audio = ifile,
          replyParameters = replyToMessageId
        )
    )
  }

  def sendGif[F[_]: Async: LogWriter: Api](
      reply: GifFile,
      msg: TMessage,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] = {
    TelegramReply.telegramFileReplyPattern[F](
      msg = msg,
      repository = repository,
      chatAction = "upload_document",
      mediaFile = reply,
      replyToMessage = replyToMessage,
      sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
        Methods.sendAnimation(
          chatId = chatId,
          animation = ifile,
          replyParameters = replyToMessageId
        )
    )
  }

  def sendPhoto[F[_]: Async: LogWriter: Api](
      reply: PhotoFile,
      msg: TMessage,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] = {
    TelegramReply.telegramFileReplyPattern[F](
      msg = msg,
      repository = repository,
      chatAction = "upload_photo",
      mediaFile = reply,
      replyToMessage = replyToMessage,
      sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
        Methods.sendPhoto(
          chatId = chatId,
          photo = ifile,
          replyParameters = replyToMessageId
        )
    )
  }

  def sendVideo[F[_]: Async: LogWriter: Api](
      reply: VideoFile,
      msg: TMessage,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] = {
    TelegramReply.telegramFileReplyPattern[F](
      msg = msg,
      repository = repository,
      chatAction = "upload_video",
      mediaFile = reply,
      replyToMessage = replyToMessage,
      sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
        Methods.sendVideo(
          chatId = chatId,
          video = ifile,
          replyParameters = replyToMessageId
        )
    )
  }

  def sendDocument[F[_]: Async: LogWriter: Api](
      reply: Document,
      msg: TMessage,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] = {
    TelegramReply.telegramFileReplyPattern[F](
      msg = msg,
      repository = repository,
      chatAction = "upload_video",
      mediaFile = reply,
      replyToMessage = replyToMessage,
      sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
        Methods.sendDocument(
          chatId = chatId,
          document = ifile,
          replyParameters = replyToMessageId
        )
    )
  }

  def sendSticker[F[_]: Async: LogWriter: Api](
      reply: Sticker,
      msg: TMessage,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] = {
    TelegramReply.telegramFileReplyPattern[F](
      msg = msg,
      repository = repository,
      chatAction = "upload_video",
      mediaFile = reply,
      replyToMessage = replyToMessage,
      sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
        Methods.sendSticker(
          chatId = chatId,
          sticker = ifile,
          replyParameters = replyToMessageId
        )
    )
  }
}
