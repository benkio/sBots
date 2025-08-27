package com.benkio.telegrambotinfrastructure.telegram

import cats.*
import cats.data.EitherT
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.http.ErrorFallbackWorkaround
import com.benkio.telegrambotinfrastructure.messagefiltering.getContent
import com.benkio.telegrambotinfrastructure.model.media.toTelegramApi
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.reply.Document
import com.benkio.telegrambotinfrastructure.model.reply.GifFile
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.Mp3File
import com.benkio.telegrambotinfrastructure.model.reply.PhotoFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyValue
import com.benkio.telegrambotinfrastructure.model.reply.Sticker
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.model.reply.VideoFile
import com.benkio.telegrambotinfrastructure.repository.Repository
import log.effect.LogWriter
import telegramium.bots.client.Method
import telegramium.bots.high.*
import telegramium.bots.high.implicits.*
import telegramium.bots.ChatId
import telegramium.bots.ChatIntId
import telegramium.bots.Html
import telegramium.bots.IFile
import telegramium.bots.Markdown2
import telegramium.bots.Message
import telegramium.bots.ParseMode
import telegramium.bots.ReplyParameters

trait TelegramReply[A] {
  def reply[F[_]: Async: LogWriter: Api](
      reply: A,
      msg: Message,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[Message]]
}

object TelegramReply:

  @inline def apply[F](implicit instance: TelegramReply[F]): TelegramReply[F] = instance

  def telegramFileReplyPattern[F[_]: Async: LogWriter: Api](
      msg: Message,
      repository: Repository[F],
      chatAction: String,
      mediaFile: MediaFile,
      replyToMessage: Boolean,
      sendFileAPIMethod: (ChatId, IFile, Option[Int]) => Method[Message]
  ): F[List[Message]] = {
    val chatId: ChatId                                                    = ChatIntId(msg.chat.id)
    def computeMediaResource(mediaResource: MediaResource[F]): F[Message] =
      mediaResource.toTelegramApi.use(iFile =>
        sendFileAPIMethod(
          chatId,
          iFile,
          Option.when(replyToMessage)(msg.messageId)
        ).exec
      )
    val result: EitherT[F, Throwable, List[Message]] = for {
      _       <- Methods.sendChatAction(chatId, chatAction).exec.attemptT
      message <-
        repository
          .getResourceFile(mediaFile)
          .use[List[Message]](mediaResources =>
            mediaResources
              .map(
                _.reduceLeftTo(computeMediaResource(_))((prevExec, nextRes) =>
                  prevExec.handleErrorWith(e =>
                    LogWriter.error(
                      s"[TelegramReply] ERROR while executing media resource for $mediaFile with $e. Fallback to $nextRes"
                    ) >>
                      computeMediaResource(nextRes)
                  )
                )
              )
              .fold(
                e =>
                  TelegramReply[Text].reply(
                    reply = ErrorFallbackWorkaround.errorText(
                      s"""An Error Occurred for
                         | - msg: $msg
                         | - mediaFile: $mediaFile
                         | - error: ${e.getMessage()}
                         |""".stripMargin
                    ),
                    msg = ErrorFallbackWorkaround.supportmessage,
                    repository = repository,
                    replyToMessage = false
                  ),
                _.map(List(_))
              )
          )
          .onError(e => LogWriter.error(s"[TelegramReply:71:63]] ERROR when replying to $chatId with $mediaFile: $e"))
          .attemptT
    } yield message
    result.getOrElse(List.empty)
  }

  given telegramReplyValue: TelegramReply[ReplyValue] = new TelegramReply[ReplyValue] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: ReplyValue,
        msg: Message,
        repository: Repository[F],
        replyToMessage: Boolean
    ): F[List[Message]] = reply match {
      case mp3: Mp3File       => telegramMp3Reply.reply(mp3, msg, repository, replyToMessage)
      case gif: GifFile       => telegramGifReply.reply(gif, msg, repository, replyToMessage)
      case photo: PhotoFile   => telegramPhotoReply.reply(photo, msg, repository, replyToMessage)
      case video: VideoFile   => telegramVideoReply.reply(video, msg, repository, replyToMessage)
      case document: Document => telegramDocumentReply.reply(document, msg, repository, replyToMessage)
      case sticker: Sticker   => telegramStickerReply.reply(sticker, msg, repository, replyToMessage)
      case text: Text         => telegramTextReply.reply(text, msg, repository, replyToMessage)
    }
  }

  given telegramMp3Reply: TelegramReply[Mp3File] = new TelegramReply[Mp3File] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: Mp3File,
        msg: Message,
        repository: Repository[F],
        replyToMessage: Boolean
    ): F[List[Message]] = {
      TelegramReply.telegramFileReplyPattern[F](
        msg = msg,
        repository = repository,
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
        repository: Repository[F],
        replyToMessage: Boolean
    ): F[List[Message]] = {
      TelegramReply.telegramFileReplyPattern[F](
        msg = msg,
        repository = repository,
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
        repository: Repository[F],
        replyToMessage: Boolean
    ): F[List[Message]] = {
      TelegramReply.telegramFileReplyPattern[F](
        msg = msg,
        repository = repository,
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
        repository: Repository[F],
        replyToMessage: Boolean
    ): F[List[Message]] = {
      TelegramReply.telegramFileReplyPattern[F](
        msg = msg,
        repository = repository,
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
        repository: Repository[F],
        replyToMessage: Boolean
    ): F[List[Message]] = {
      TelegramReply.telegramFileReplyPattern[F](
        msg = msg,
        repository = repository,
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
        repository: Repository[F],
        replyToMessage: Boolean
    ): F[List[Message]] = {
      TelegramReply.telegramFileReplyPattern[F](
        msg = msg,
        repository = repository,
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
    override def reply[F[_]: Async: LogWriter: Api](
        reply: Text,
        msg: Message,
        repository: Repository[F],
        replyToMessage: Boolean
    ): F[List[Message]] = {
      val chatId: ChatId               = ChatIntId(msg.chat.id)
      val parseMode: Option[ParseMode] = reply.textType match {
        case Text.TextType.Plain    => None
        case Text.TextType.Markdown => Markdown2.some
        case Text.TextType.Html     => Html.some
      }
      val result: EitherT[F, Throwable, List[Message]] =
        for {
          _       <- EitherT.liftF(LogWriter.info(s"[TelegramReply[Text]] reply to message: ${msg.getContent}"))
          _       <- Methods.sendChatAction(chatId, "typing").exec.attemptT
          message <-
            Methods
              .sendMessage(
                chatId = chatId,
                text = reply.value,
                replyParameters = Option.when(replyToMessage)(ReplyParameters(msg.messageId)),
                parseMode = parseMode
              )
              .exec
              .attemptT
        } yield List(message)
      result.getOrElse(List.empty)
    }
  }
end TelegramReply
