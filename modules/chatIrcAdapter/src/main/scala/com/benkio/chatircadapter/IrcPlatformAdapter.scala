package com.benkio.chatircadapter

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.ComputeReply
import com.benkio.chatcore.PlatformAdapter
import com.benkio.chatircadapter.model.IrcCapabilities
import com.benkio.chatircadapter.outbound.IrcApi
import com.benkio.chatircadapter.outbound.IrcReplies
import log.effect.LogWriter

import scala.concurrent.duration.FiniteDuration

/** IRC as a platform of the engine: the replies are sent as messages to the channel or to the nick.
  *
  * @param ircApi
  *   gives the API once the connection is up: the replies are only sent after the bot received a message, so after the
  *   connection
  * @param startConnection
  *   connects to the server and delivers the messages to the engine, until the connection ends
  */
class IrcPlatformAdapter[F[_]: Async: LogWriter](
    ircApi: F[IrcApi[F]],
    backgroundJobManager: BackgroundJobManager[F],
    effectfulCallbacks: Map[String, Message => F[ReplyValue]],
    dbLayer: DBLayer[F],
    ttl: Option[FiniteDuration],
    startConnection: F[Unit]
) extends PlatformAdapter[F] {

  override val capabilities: PlatformCapabilities = IrcCapabilities.value

  override def run: F[Unit] = startConnection

  override def send(replyValue: ReplyValue, message: Message, replyToMessage: Boolean): F[Unit] =
    ircApi.flatMap(api => IrcReplies[F](api, capabilities).send(replyValue, message, replyToMessage))

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
