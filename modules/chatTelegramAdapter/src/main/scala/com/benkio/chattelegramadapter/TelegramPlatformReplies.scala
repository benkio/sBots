package com.benkio.chattelegramadapter

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.PlatformReplies
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.TelegramMessageReply
import log.effect.LogWriter
import telegramium.bots.high.Api

import scala.concurrent.duration.FiniteDuration

/** The replies of the engine sent through the Telegram API. */
class TelegramPlatformReplies[F[_]: Async: LogWriter: Api](
    repository: Repository[F],
    backgroundJobManager: BackgroundJobManager[F],
    effectfulCallbacks: Map[String, Message => F[ReplyValue]],
    dbLayer: DBLayer[F],
    ttl: Option[FiniteDuration]
) extends PlatformReplies[F] {

  override def send(replyValue: ReplyValue, message: Message, replyToMessage: Boolean): F[Unit] =
    TelegramMessageReply
      .sendReplyValue[F](
        replyValue = replyValue,
        msg = message,
        repository = repository,
        replyToMessage = replyToMessage
      )
      .void

  override def runEffectful(reply: EffectfulReply, message: Message): F[ReplyValue] =
    ComputeReply.runEffectfulReply[F](
      reply = reply,
      msg = message,
      backgroundJobManager = backgroundJobManager,
      dbLayer = dbLayer,
      effectfulCallbacks = effectfulCallbacks,
      ttl = ttl
    )
}
