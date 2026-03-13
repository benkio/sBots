package com.benkio.chattelegramadapter.http.telegramreply.messagereply

import cats.*
import cats.data.EitherT
import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.model.media.MediaResource
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.repository.Repository
import com.benkio.chattelegramadapter.conversions.MediaResourceConversions.*
import com.benkio.chattelegramadapter.http.ErrorFallbackWorkaround
import com.benkio.chattelegramadapter.model.TelegramInlineKeyboard
import log.effect.LogWriter
import telegramium.bots.client.Method
import telegramium.bots.high.*
import telegramium.bots.high.implicits.*
import telegramium.bots.ChatId
import telegramium.bots.ChatIntId
import telegramium.bots.IFile
import telegramium.bots.Message as TMessage
import telegramium.bots.ReplyParameters

object TelegramMessageReply {

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
                      s"[TelegramMessageReply] ERROR while executing media resource for $mediaFile with $e. Fallback to $nextRes"
                    ) >>
                      computeMediaResource(nextRes)
                  )
                )
              )
              .map(List(_))
          )
          .onError(e =>
            LogWriter.error(
              s"[TelegramMessageReply:71:63]] ERROR when replying to $chatId with $mediaFile: $e"
            ) >> ErrorFallbackWorkaround.errorHandling[F](msg, mediaFile, e)
          )
          .attemptT
    } yield message
    result.getOrElse(List.empty)
  }

  def sendReplyValue[F[_]: Async: LogWriter: Api](
      replyValue: ReplyValue,
      msg: Message,
      repository: Repository[F],
      replyToMessage: Boolean
  ): F[List[TMessage]] = replyValue match {
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
    case telegramInlineKeyboard: TelegramInlineKeyboard =>
      KeyboardReply.sendKeyboard(
        reply = telegramInlineKeyboard,
        msg = msg,
        replyToMessage = replyToMessage
      )
  }
}
