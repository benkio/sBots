package com.benkio.chattelegramadapter

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.messagefiltering.RandomSelection
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.MediaReply
import com.benkio.chatcore.model.reply.Reply
import com.benkio.chatcore.model.reply.ReplyBundle
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.TelegramMessageReply
import log.effect.LogWriter
import telegramium.bots.high.Api
import telegramium.bots.Message as TMessage

import scala.concurrent.duration.FiniteDuration

object ComputeReply {

  def execute[F[_]: Async: LogWriter: Api](
      replyBundle: ReplyBundle,
      message: Message,
      repository: Repository[F],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, Message => F[List[Text]]],
      dbLayer: DBLayer[F],
      ttl: Option[FiniteDuration]
  ): F[List[TMessage]] = for {
    replies <- runReply(
      reply = replyBundle.reply,
      msg = message,
      backgroundJobManager = backgroundJobManager,
      dbLayer = dbLayer,
      effectfulCallbacks = effectfulCallbacks,
      ttl = ttl
    )
    replyValue <- RandomSelection.select(replies)
    result     <-
      TelegramMessageReply.sendReplyValue[F](
        replyValue = replyValue,
        msg = message,
        repository = repository,
        replyToMessage = replyBundle.reply.replyToMessage
      )
  } yield result

  private def runReply[F[_]: Async: LogWriter](
      reply: Reply,
      msg: Message,
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F],
      effectfulCallbacks: Map[String, Message => F[List[Text]]],
      ttl: Option[FiniteDuration]
  ): F[List[ReplyValue]] = reply match {
    case EffectfulReply(key, _) =>
      EffectfulKeyRunner.runEffectfulKey[F](
        effectfulKey = key,
        msg = msg,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = effectfulCallbacks,
        ttl = ttl
      )
    case textReply: TextReply   => textReply.text.pure[F]
    case mediaReply: MediaReply => mediaReply.mediaFiles.pure[F]
  }

}
