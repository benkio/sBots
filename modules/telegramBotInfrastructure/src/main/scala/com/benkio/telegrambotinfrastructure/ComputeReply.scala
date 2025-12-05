package com.benkio.telegrambotinfrastructure

import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.model.SBotId
import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.messagefiltering.RandomSelection
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.reply.ReplyValue
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.http.telegramreply.TelegramReply
import log.effect.LogWriter
import telegramium.bots.high.Api
import telegramium.bots.Message

object ComputeReply {

  def execute[F[_]: Async: LogWriter: Api](
      replyBundle: ReplyBundle,
      message: Message,
      filter: Boolean,
    repository: Repository[F],
          dbLayer: DBLayer[F],
  )(using telegramReply: TelegramReply[ReplyValue], botId: SBotId): F[List[Message]] = for {
    dataToReply <-
      if filter then Async[F].pure(replyBundle.reply)
      else Async[F].raiseError(new Exception(s"No replies for the given message: $message"))
    replies <- RandomSelection.select(dataToReply)
    result  <- replies.traverse[F, List[Message]](reply =>
      telegramReply.reply[F](
        reply = reply,
        msg = message,
        repository = repository,
        dbLayer = dbLayer,
        replyToMessage = replyBundle.reply.replyToMessage
      )
    )
  } yield result.flatten
}
