package com.benkio.telegrambotinfrastructure.http.telegramreply

import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import cats.*
import cats.data.EitherT
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.http.telegramreply.EffectfulKeyReply.given
import com.benkio.telegrambotinfrastructure.http.telegramreply.MediaFileReply.given
import com.benkio.telegrambotinfrastructure.http.telegramreply.TextReply.given
import com.benkio.telegrambotinfrastructure.http.ErrorFallbackWorkaround
import com.benkio.telegrambotinfrastructure.model.media.toTelegramApi
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.reply.EffectfulKey

import com.benkio.telegrambotinfrastructure.model.reply.MediaFile


import com.benkio.telegrambotinfrastructure.model.reply.ReplyValue

import com.benkio.telegrambotinfrastructure.model.reply.Text

import com.benkio.telegrambotinfrastructure.repository.Repository
import log.effect.LogWriter
import telegramium.bots.client.Method
import telegramium.bots.high.*
import telegramium.bots.high.implicits.*
import telegramium.bots.ChatId
import telegramium.bots.ChatIntId
import telegramium.bots.IFile
import telegramium.bots.Message
import telegramium.bots.ReplyParameters

trait TelegramReply[A] {
  def reply[F[_]: Async: LogWriter: Api](
      reply: A,
      msg: Message,
      repository: Repository[F],
      dbLayer: DBLayer[F],
      replyToMessage: Boolean
  )(using botId: SBotId): F[List[Message]]
}

object TelegramReply {

  @inline def apply[F](implicit instance: TelegramReply[F]): TelegramReply[F] = instance

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

  given telegramReplyValue: TelegramReply[ReplyValue] = new TelegramReply[ReplyValue] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: ReplyValue,
        msg: Message,
        repository: Repository[F],
        dbLayer: DBLayer[F],
        replyToMessage: Boolean
    )(using botId: SBotId): F[List[Message]] = reply match {
      case mediaFile: MediaFile =>
        TelegramReply[MediaFile].reply(
          reply = mediaFile,
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage
        )
      case text: Text =>
        TelegramReply[Text].reply(
          reply = text,
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage
        )
      case key: EffectfulKey =>
        TelegramReply[EffectfulKey].reply(
          reply = key,
          msg = msg,
          repository = repository,
          dbLayer = dbLayer,
          replyToMessage = replyToMessage
        )
    }
  }
}
