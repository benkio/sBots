package com.benkio.chattelegramadapter

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.messagefiltering.RandomSelection
import com.benkio.chatcore.model.reply.ReplyBundle
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chattelegramadapter.http.telegramreply.TelegramReply
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
    reply  <- RandomSelection.select(replyBundle.reply)
    result <-
      TelegramReply.sendReplyValue[F](
        reply = reply,
        msg = message,
        repository = repository,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        effectfulCallbacks = effectfulCallbacks,
        replyToMessage = replyBundle.reply.replyToMessage,
        ttl = ttl
      )
  } yield result
}
