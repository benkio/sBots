package com.benkio.chatircadapter.inbound

import cats.effect.std.Dispatcher
import cats.effect.Async
import cats.effect.Clock
import cats.syntax.all.*
import com.benkio.chatcore.model.Message
import log.effect.LogWriter
import net.engio.mbassy.listener.Handler
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent
import org.kitteh.irc.client.library.event.user.PrivateMessageEvent

/** Hands to `onMessage` what the bot receives, mapped by [[IrcInbound]], unless the sender is over the rate limit. A
  * failure of `onMessage` is logged: it must not reach the library. It is a listener of
  * [[com.benkio.chatircadapter.connection.IrcConnection]].
  *
  * The `dispatcher` should be a parallel one: with a sequential one a slow reply holds the next messages.
  */
class IrcInboundListener[F[_]: Async: LogWriter](
    dispatcher: Dispatcher[F],
    rateLimiter: RateLimiter[F],
    onMessage: Message => F[Unit]
) {

  @Handler
  def onChannelMessage(event: ChannelMessageEvent): Unit =
    deliver(
      sender = event.getActor.getNick,
      channel = Some(event.getChannel.getName),
      text = event.getMessage,
      ownNick = event.getClient.getNick
    )

  @Handler
  def onPrivateMessage(event: PrivateMessageEvent): Unit =
    deliver(sender = event.getActor.getNick, channel = None, text = event.getMessage, ownNick = event.getClient.getNick)

  private def deliver(sender: String, channel: Option[String], text: String, ownNick: String): Unit =
    dispatcher.unsafeRunAndForget(
      Clock[F].realTime
        .map(_.toSeconds)
        .flatMap(now =>
          IrcInbound
            .message(IrcMessageEvent(sender, channel, text, now), ownNick)
            .traverse_(message =>
              rateLimiter
                .allow(sender.toLowerCase)
                .ifM(
                  onMessage(message),
                  LogWriter.info(s"[IrcInbound] $sender is over the rate limit, the message is ignored")
                )
            )
        )
        .handleErrorWith(e => LogWriter.error(s"[IrcInbound] Error while handling a message of $sender: $e"))
    )
}
