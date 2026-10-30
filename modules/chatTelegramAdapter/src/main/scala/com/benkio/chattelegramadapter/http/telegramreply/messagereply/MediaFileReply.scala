package com.benkio.chattelegramadapter.http.telegramreply.messagereply

import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.model.reply.GifFile
import com.benkio.chatcore.model.reply.MarkdownFile
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PhotoFile
import com.benkio.chatcore.model.reply.Sticker
import com.benkio.chatcore.model.reply.VideoFile
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.repository.Repository
import com.benkio.chattelegramadapter.conversions.MessageConversions.*
import log.effect.LogWriter
import telegramium.bots.high.*
import telegramium.bots.high.implicits.*
import telegramium.bots.InputLinkFile
import telegramium.bots.InputPartFile
import telegramium.bots.InputRichMessage
import telegramium.bots.Message as TMessage

import java.nio.file.Path

object MediaFileReply {

  def sendMediaFile[F[_]: Async: LogWriter: Api](
      reply: MediaFile,
      msg: Message,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] = reply match {
    case mp3: Mp3File =>
      LogWriter.trace(s"Send Mp3: $mp3") >>
        sendMp3(
          reply = mp3,
          msg = msg.toTelegramium,
          repository = repository,
          replyToMessage = replyToMessage
        )
    case gif: GifFile =>
      LogWriter.trace(s"Send Gif: $gif") >>
        sendGif(
          reply = gif,
          msg = msg.toTelegramium,
          repository = repository,
          replyToMessage = replyToMessage
        )
    case photo: PhotoFile =>
      LogWriter.trace(s"Send Photo: $photo") >>
        sendPhoto(
          reply = photo,
          msg = msg.toTelegramium,
          repository = repository,
          replyToMessage = replyToMessage
        )
    case video: VideoFile =>
      LogWriter.trace(s"Send Video: $video") >>
        sendVideo(
          reply = video,
          msg = msg.toTelegramium,
          repository = repository,
          replyToMessage = replyToMessage
        )
    case document: Document =>
      LogWriter.trace(s"Send document: $document") >>
        sendDocument(
          reply = document,
          msg = msg.toTelegramium,
          repository = repository,
          replyToMessage = replyToMessage
        )
    case sticker: Sticker =>
      LogWriter.trace(s"Send sticker: $sticker") >>
        sendSticker(
          reply = sticker,
          msg = msg.toTelegramium,
          repository = repository,
          replyToMessage = replyToMessage
        )
    case markdown: MarkdownFile =>
      LogWriter.trace(s"Send Markdown: $markdown") >>
        sendMarkdown(
          reply = markdown,
          msg = msg.toTelegramium,
          repository = repository,
          replyToMessage = replyToMessage
        )
  }

  def sendMarkdown[F[_]: Async: LogWriter: Api](
      reply: MarkdownFile,
      msg: TMessage,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] = {
    TelegramMessageReply.telegramFileReplyPattern[F](
      msg = msg,
      repository = repository,
      chatAction = "typing",
      mediaFile = reply,
      replyToMessage = replyToMessage,
      sendFileAPIMethod = (_, ifile, _) =>
        ifile match {
          case InputPartFile(file) =>
            Repository
              .fileToString[F](Path.of(file.getAbsolutePath))
              .use(content => {
                RichTextReply
                  .sendRichText[F](
                    richMessage = InputRichMessage(markdown = content.some),
                    msg = msg.toModel,
                    replyToMessage = replyToMessage
                  )
                  .flatMap {
                    case message :: _ => Async[F].pure(message)
                    case Nil          =>
                      Async[F].raiseError(
                        new RuntimeException(
                          s"[MediaFileReply] No Telegram message returned while sending markdown ${reply.filepath}"
                        )
                      )
                  }
              })
          case InputLinkFile(fileIdOrUrl) =>
            Async[F].raiseError(
              new UnsupportedOperationException(
                s"[MediaFileReply] Markdown does not support MediaResourceIFile source: $fileIdOrUrl"
              )
            )
        }
    )
  }

  def sendMp3[F[_]: Async: LogWriter: Api](
      reply: Mp3File,
      msg: TMessage,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] = {
    TelegramMessageReply.telegramFileReplyPattern[F](
      msg = msg,
      repository = repository,
      chatAction = "upload_voice",
      mediaFile = reply,
      replyToMessage = replyToMessage,
      sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
        Methods
          .sendAudio(
            chatId = chatId,
            audio = ifile,
            replyParameters = replyToMessageId
          )
          .exec
    )
  }

  def sendGif[F[_]: Async: LogWriter: Api](
      reply: GifFile,
      msg: TMessage,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] = {
    TelegramMessageReply.telegramFileReplyPattern[F](
      msg = msg,
      repository = repository,
      chatAction = "upload_document",
      mediaFile = reply,
      replyToMessage = replyToMessage,
      sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
        Methods
          .sendAnimation(
            chatId = chatId,
            animation = ifile,
            replyParameters = replyToMessageId
          )
          .exec
    )
  }

  def sendPhoto[F[_]: Async: LogWriter: Api](
      reply: PhotoFile,
      msg: TMessage,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] = {
    TelegramMessageReply.telegramFileReplyPattern[F](
      msg = msg,
      repository = repository,
      chatAction = "upload_photo",
      mediaFile = reply,
      replyToMessage = replyToMessage,
      sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
        Methods
          .sendPhoto(
            chatId = chatId,
            photo = ifile,
            replyParameters = replyToMessageId
          )
          .exec
    )
  }

  def sendVideo[F[_]: Async: LogWriter: Api](
      reply: VideoFile,
      msg: TMessage,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] = {
    TelegramMessageReply.telegramFileReplyPattern[F](
      msg = msg,
      repository = repository,
      chatAction = "upload_video",
      mediaFile = reply,
      replyToMessage = replyToMessage,
      sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
        Methods
          .sendVideo(
            chatId = chatId,
            video = ifile,
            replyParameters = replyToMessageId
          )
          .exec
    )
  }

  def sendDocument[F[_]: Async: LogWriter: Api](
      reply: Document,
      msg: TMessage,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] = {
    TelegramMessageReply.telegramFileReplyPattern[F](
      msg = msg,
      repository = repository,
      chatAction = "upload_video",
      mediaFile = reply,
      replyToMessage = replyToMessage,
      sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
        Methods
          .sendDocument(
            chatId = chatId,
            document = ifile,
            replyParameters = replyToMessageId
          )
          .exec
    )
  }

  def sendSticker[F[_]: Async: LogWriter: Api](
      reply: Sticker,
      msg: TMessage,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] = {
    TelegramMessageReply.telegramFileReplyPattern[F](
      msg = msg,
      repository = repository,
      chatAction = "upload_video",
      mediaFile = reply,
      replyToMessage = replyToMessage,
      sendFileAPIMethod = (chatId, ifile, replyToMessageId) =>
        Methods
          .sendSticker(
            chatId = chatId,
            sticker = ifile,
            replyParameters = replyToMessageId
          )
          .exec
    )
  }
}
