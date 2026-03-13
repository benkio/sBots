package com.benkio.chattelegramadapter.http.telegramreply.messagereply

import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.messagefiltering.RandomSelection
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chattelegramadapter.EffectfulKeyRunner
import log.effect.LogWriter
import telegramium.bots.high.Api
import telegramium.bots.Message as TMessage

import scala.concurrent.duration.FiniteDuration

object EffectfulKeyReply {

  def sendEffectfulKey[F[_]: Async: LogWriter: Api](
      reply: EffectfulKey,
      msg: Message,
      repository: Repository[F],
      dbLayer: DBLayer[F],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, Message => F[List[Text]]],
      replyToMessage: Boolean,
      ttl: Option[FiniteDuration]
  ): F[List[TMessage]] = {
    for {
      replyValues <- EffectfulKeyRunner.runEffectfulKey(
        reply,
        msg,
        dbLayer,
        backgroundJobManager,
        effectfulCallbacks,
        ttl
      )
      replyValue <- RandomSelection.select[F](replyValues)
      result     <- TelegramMessageReply.sendReplyValue(
        replyValue = replyValue,
        msg = msg,
        repository = repository,
        replyToMessage = replyToMessage
      )
    } yield result
  }
}
