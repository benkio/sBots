package com.benkio.chatircadapter.outbound

import cats.effect.Async
import org.kitteh.irc.client.library.Client

/** [[IrcApi]] with Kitteh. The messages go through the queue of the client, that sends one every
  * [[com.benkio.chatircadapter.connection.IrcConfig.messageDelay]].
  */
class KittehIrcApi[F[_]: Async](client: Client) extends IrcApi[F] {
  override def sendMessage(target: String, text: String): F[Unit] =
    Async[F].delay(client.sendMessage(target, text))
}
