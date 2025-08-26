package com.benkio.telegrambotinfrastructure

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.messagefiltering.RandomSelection
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundle
import com.benkio.telegrambotinfrastructure.model.reply.ReplyValue
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.telegram.TelegramReply
import log.effect.LogWriter
import telegramium.bots.high.Api
import telegramium.bots.Message

object ComputeReply:

  def execute[F[_]: Async: LogWriter: Api](
      replyBundle: ReplyBundle[F],
      message: Message,
      filter: F[Boolean],
      repository: Repository[F]
  )(using telegramReply: TelegramReply[ReplyValue]): F[List[Message]] = for {
    dataToReply <- Async[F].ifM(filter)(
      ifTrue = Async[F].pure(replyBundle.reply),
      ifFalse = Async[F].raiseError(new Exception(s"No replies for the given message: $message"))
    )
    replies <- RandomSelection.select(dataToReply, message)
    result  <- replies.traverse[F, List[Message]](reply =>
      telegramReply.reply[F](
        reply = reply,
        msg = message,
        repository = repository,
        replyToMessage = replyBundle.reply.replyToMessage
      )
    )
  } yield result.flatten
