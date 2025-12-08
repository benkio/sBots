package com.benkio.telegrambotinfrastructure.http.telegramreply

import cats.*
import cats.data.EitherT
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.http.ErrorFallbackWorkaround
import com.benkio.telegrambotinfrastructure.model.media.toTelegramApi
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.reply.EffectfulKey
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyValue
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import log.effect.LogWriter
import telegramium.bots.client.Method
import telegramium.bots.high.*
import telegramium.bots.high.implicits.*
import telegramium.bots.ChatId
import telegramium.bots.ChatIntId
import telegramium.bots.IFile
import telegramium.bots.Message
import telegramium.bots.ReplyParameters

object TelegramReply {

  def telegramFileReplyPattern[F[_]: Async: LogWriter: Api](
      msg: Message,
      repository: Repository[F],
      chatAction: String,
      mediaFile: MediaFile,
      replyToMessage: Boolean,
      sendFileAPIMethod: (ChatId, IFile, Option[ReplyParameters]) => Method[Message]
  ): F[List[Message]] = {
    val chatId: ChatId                                                    = ChatIntId(msg.chat.id)
    def computeMediaResource(mediaResource: MediaResource[F]): F[Message] =
      mediaResource.toTelegramApi.use(iFile =>
        sendFileAPIMethod(
          chatId,
          iFile,
          Option.when(replyToMessage)(ReplyParameters(msg.messageId))
        ).exec
      )
    val result: EitherT[F, Throwable, List[Message]] = for {
      _       <- Methods.sendChatAction(chatId, chatAction).exec.attemptT
      message <-
        repository
          .getResourceFile(mediaFile)
          .use[List[Message]](mediaResources =>
            Async[F]
              .fromEither(mediaResources)
              .flatMap(
                _.reduceLeftTo(computeMediaResource(_))((prevExec, nextRes) =>
                  prevExec.handleErrorWith(e =>
                    LogWriter.error(
                      s"[TelegramReply] ERROR while executing media resource for $mediaFile with $e. Fallback to $nextRes"
                    ) >>
                      computeMediaResource(nextRes)
                  )
                )
              )
              .map(List(_))
          )
          .onError(e =>
            LogWriter.error(
              s"[TelegramReply:71:63]] ERROR when replying to $chatId with $mediaFile: $e"
            ) >> ErrorFallbackWorkaround.errorHandling[F](msg, mediaFile, e)
          )
          .attemptT
    } yield message
    result.getOrElse(List.empty)
  }

  def sendReplyValue[F[_]: Async: LogWriter: Api](
      reply: ReplyValue,
      msg: Message,
      repository: Repository[F],
      dbLayer: DBLayer[F],
      backgroundJobManager: BackgroundJobManager[F],
      replyToMessage: Boolean
  ): F[List[Message]] = reply match {
    case mediaFile: MediaFile =>
      MediaFileReply.sendMediaFile(
        reply = mediaFile,
        msg = msg,
        repository = repository,
        replyToMessage = replyToMessage
      )
    case text: Text =>
      TextReply.sendText(
        reply = text,
        msg = msg,
        replyToMessage = replyToMessage
      )
    case key: EffectfulKey =>
      EffectfulKeyReply.sendEffectfulKey(
        reply = key,
        msg = msg,
        repository = repository,
        dbLayer = dbLayer,
        replyToMessage = replyToMessage,
        backgroundJobManager = backgroundJobManager
      )
  }
}
