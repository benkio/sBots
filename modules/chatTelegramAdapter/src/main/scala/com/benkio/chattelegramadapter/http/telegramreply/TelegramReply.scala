package com.benkio.chattelegramadapter.http.telegramreply

import cats.*
import cats.data.EitherT
import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chattelegramadapter.conversions.MediaResourceConversions.*
import com.benkio.chattelegramadapter.http.ErrorFallbackWorkaround
import log.effect.LogWriter
import telegramium.bots.client.Method
import telegramium.bots.high.*
import telegramium.bots.high.implicits.*
import telegramium.bots.ChatId
import telegramium.bots.ChatIntId
import telegramium.bots.IFile
import telegramium.bots.Message as TMessage
import telegramium.bots.ReplyParameters

import scala.concurrent.duration.FiniteDuration

object TelegramReply {

  def telegramFileReplyPattern[F[_]: Async: LogWriter: Api](
      msg: TMessage,
      repository: Repository[F],
      chatAction: String,
      mediaFile: MediaFile,
      replyToMessage: Boolean,
      sendFileAPIMethod: (ChatId, IFile, Option[ReplyParameters]) => Method[TMessage]
  ): F[List[TMessage]] = {
    val chatId: ChatId                                                     = ChatIntId(msg.chat.id)
    def computeMediaResource(mediaResource: MediaResource[F]): F[TMessage] =
      mediaResource.toTelegramApi.use(iFile =>
        sendFileAPIMethod(
          chatId,
          iFile,
          Option.when(replyToMessage)(ReplyParameters(msg.messageId))
        ).exec
      )
    val result: EitherT[F, Throwable, List[TMessage]] = for {
      _       <- Methods.sendChatAction(chatId, chatAction).exec.attemptT
      message <-
        repository
          .getResourceFile(mediaFile)
          .use[List[TMessage]](mediaResources =>
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
      effectfulCallbacks: Map[String, Message => F[List[Text]]],
      replyToMessage: Boolean,
      ttl: Option[FiniteDuration]
  ): F[List[TMessage]] = reply match {
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
        effectfulCallbacks = effectfulCallbacks,
        backgroundJobManager = backgroundJobManager,
        ttl = ttl
      )
  }
}
