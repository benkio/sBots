package com.benkio.chattelegramadapter.http.telegramreply.messagereply

import cats.effect.*
import cats.syntax.all.*
import com.benkio.chatcore.messagefiltering.RandomSelection
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.EffectfulKeyRunner
import com.benkio.chatcore.Notifier
import com.benkio.chattelegramadapter.model.TelegramCapabilities
import log.effect.LogWriter
import telegramium.bots.high.Api
import telegramium.bots.Message as TMessage

import scala.concurrent.duration.FiniteDuration

object EffectfulKeyReply {

  def sendEffectfulKey[F[_]: Async: LogWriter: Api: Notifier](
      reply: EffectfulKey,
      msg: Message,
      repository: Repository[F],
      dbLayer: DBLayer[F],
      backgroundJobManager: BackgroundJobManager[F],
      effectfulCallbacks: Map[String, Message => F[ReplyValue]],
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
        ttl,
        TelegramCapabilities.value.commandSyntax
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
