package com.benkio.chattelegramadapter

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.PlatformAdapter
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.TelegramMessageReply
import com.benkio.chattelegramadapter.model.TelegramCapabilities
import log.effect.LogWriter
import telegramium.bots.high.Api

import scala.concurrent.duration.FiniteDuration

/** Telegram as a platform of the engine: the replies are sent through the Telegram API.
  *
  * @param startUpdates
  *   how Telegram delivers the updates of the bot: the long polling, or nothing to start for a webhook bot (the updates
  *   arrive on the HTTP server that composes the webhook bots)
  */
class TelegramPlatformAdapter[F[_]: Async: LogWriter: Api](
    repository: Repository[F],
    backgroundJobManager: BackgroundJobManager[F],
    effectfulCallbacks: Map[String, Message => F[ReplyValue]],
    dbLayer: DBLayer[F],
    ttl: Option[FiniteDuration],
    startUpdates: F[Unit]
) extends PlatformAdapter[F] {

  override val capabilities: PlatformCapabilities = TelegramCapabilities.value

  override def run: F[Unit] = startUpdates

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
