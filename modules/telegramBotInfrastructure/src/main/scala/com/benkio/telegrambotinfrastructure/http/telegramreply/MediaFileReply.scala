package com.benkio.telegrambotinfrastructure.http.telegramreply

import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import cats.effect.*
import com.benkio.telegrambotinfrastructure.model.reply.Document
import com.benkio.telegrambotinfrastructure.model.reply.GifFile
import com.benkio.telegrambotinfrastructure.model.reply.Mp3File
import com.benkio.telegrambotinfrastructure.model.reply.PhotoFile
import com.benkio.telegrambotinfrastructure.model.reply.Sticker
import com.benkio.telegrambotinfrastructure.model.reply.VideoFile
import com.benkio.telegrambotinfrastructure.repository.Repository
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.Message

object MediaFileReply {

  given telegramMediaFileReplyValue: TelegramReply[MediaFile] = new TelegramReply[MediaFile] {
    override def reply[F[_]: Async: LogWriter: Api](
        reply: MediaFile,
        msg: Message,
        repository: Repository[F],
        dbLayer: DBLayer[F],
        replyToMessage: Boolean
    )(using botId: SBotId): F[List[Message]] = reply match {
      case mp3: Mp3File =>
        telegramMp3Reply.reply(
          reply = mp3,
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage
        )
      case gif: GifFile =>
        telegramGifReply.reply(
          reply = gif,
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage
        )
      case photo: PhotoFile =>
        telegramPhotoReply.reply(
          reply = photo,
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage
        )
      case video: VideoFile =>
        telegramVideoReply.reply(
          reply = video,
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage
        )
      case document: Document =>
        telegramDocumentReply.reply(
          reply = document,
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage
        )
      case sticker: Sticker =>
        telegramStickerReply.reply(
          reply = sticker,
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage
        )
    }

    given telegramMp3Reply: TelegramReply[Mp3File] = new TelegramReply[Mp3File] {
      override def reply[F[_]: Async: LogWriter: Api](
          reply: Mp3File,
          msg: Message,
          repository: Repository[F],
          dbLayer: DBLayer[F],
          replyToMessage: Boolean
      )(using botId: SBotId): F[List[Message]] = {
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
    }

    given telegramGifReply: TelegramReply[GifFile] = new TelegramReply[GifFile] {
      override def reply[F[_]: Async: LogWriter: Api](
          reply: GifFile,
          msg: Message,
          repository: Repository[F],
          dbLayer: DBLayer[F],
          replyToMessage: Boolean
      )(using botId: SBotId): F[List[Message]] = {
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
    }

    given telegramPhotoReply: TelegramReply[PhotoFile] = new TelegramReply[PhotoFile] {
      override def reply[F[_]: Async: LogWriter: Api](
          reply: PhotoFile,
          msg: Message,
          repository: Repository[F],
          dbLayer: DBLayer[F],
          replyToMessage: Boolean
      )(using botId: SBotId): F[List[Message]] = {
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
    }

    given telegramVideoReply: TelegramReply[VideoFile] = new TelegramReply[VideoFile] {
      override def reply[F[_]: Async: LogWriter: Api](
          reply: VideoFile,
          msg: Message,
          repository: Repository[F],
          dbLayer: DBLayer[F],
          replyToMessage: Boolean
      )(using botId: SBotId): F[List[Message]] = {
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
    }

    given telegramDocumentReply: TelegramReply[Document] = new TelegramReply[Document] {
      override def reply[F[_]: Async: LogWriter: Api](
          reply: Document,
          msg: Message,
          repository: Repository[F],
          dbLayer: DBLayer[F],
          replyToMessage: Boolean
      )(using botId: SBotId): F[List[Message]] = {
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
    }

    given telegramStickerReply: TelegramReply[Sticker] = new TelegramReply[Sticker] {
      override def reply[F[_]: Async: LogWriter: Api](
          reply: Sticker,
          msg: Message,
          repository: Repository[F],
          dbLayer: DBLayer[F],
          replyToMessage: Boolean
      )(using botId: SBotId): F[List[Message]] = {
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
  }
}
