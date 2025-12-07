package com.benkio.telegrambotinfrastructure

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.http.telegramreply.TelegramReply
import com.benkio.telegrambotinfrastructure.messagefiltering.RandomSelection
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
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
      dbLayer: DBLayer[F]
  ): F[List[Message]] = for {
    replies <- RandomSelection.select(replyBundle.reply)
    result  <- replies.traverse[F, List[Message]](reply =>
      TelegramReply.sendReplyValue[F](
        reply = reply,
        msg = message,
        repository = repository,
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        replyToMessage = replyBundle.reply.replyToMessage
      )
    )
  } yield result.flatten
}
