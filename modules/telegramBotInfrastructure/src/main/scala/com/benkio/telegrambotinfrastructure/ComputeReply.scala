package com.benkio.telegrambotinfrastructure

import scala.concurrent.duration.FiniteDuration
import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.http.telegramreply.TelegramReply
import com.benkio.telegrambotinfrastructure.messagefiltering.RandomSelection
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.Repository
import log.effect.LogWriter
import telegramium.bots.high.Api
import telegramium.bots.Message

object ComputeReply {

  def execute[F[_]: Async: LogWriter: Api](
      replyBundle: ReplyBundle,
      message: Message,
      repository: Repository[F],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, Message => F[List[Text]]],
    dbLayer: DBLayer[F],
          ttl: Option[FiniteDuration]
  ): F[List[Message]] = for {
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
