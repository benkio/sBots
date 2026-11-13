package com.benkio.chatdiscordadapter

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.ComputeReply
import com.benkio.chatcore.Notifier
import com.benkio.chatcore.PlatformAdapter
import com.benkio.chatdiscordadapter.model.DiscordCapabilities
import com.benkio.chatdiscordadapter.outbound.DiscordApi
import com.benkio.chatdiscordadapter.outbound.DiscordReplies
import log.effect.LogWriter

import scala.concurrent.duration.FiniteDuration

/** Discord as a platform of the engine: the replies are sent through the Discord API.
  *
  * @param discordApi
  *   gives the API once the gateway connection is up: the replies are only sent after the bot received a message, so
  *   after the connection
  * @param startGateway
  *   connects to the gateway and delivers the messages to the engine, until the connection ends
  */
class DiscordPlatformAdapter[F[_]: Async: LogWriter: Notifier](
    discordApi: F[DiscordApi[F]],
    repository: Repository[F],
    backgroundJobManager: BackgroundJobManager[F],
    effectfulCallbacks: Map[String, Message => F[ReplyValue]],
    dbLayer: DBLayer[F],
    ttl: Option[FiniteDuration],
    startGateway: F[Unit]
) extends PlatformAdapter[F] {

  override val capabilities: PlatformCapabilities = DiscordCapabilities.value

  private val usableRepository: Repository[F] = repository.usableFor(capabilities)

  override def run: F[Unit] = startGateway

  override def send(replyValue: ReplyValue, message: Message, replyToMessage: Boolean): F[Unit] =
    discordApi.flatMap(api =>
      DiscordReplies[F](api, usableRepository, dbLayer, capabilities).send(replyValue, message, replyToMessage)
    )

  override def runEffectful(reply: EffectfulReply, message: Message): F[ReplyValue] =
    ComputeReply.runEffectfulReply[F](
      reply = reply,
      msg = message,
      backgroundJobManager = backgroundJobManager,
      dbLayer = dbLayer,
      effectfulCallbacks = effectfulCallbacks,
      ttl = ttl,
      commandSyntax = capabilities.commandSyntax
    )
}
