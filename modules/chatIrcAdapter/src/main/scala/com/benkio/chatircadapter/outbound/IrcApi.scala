package com.benkio.chatircadapter.outbound

/** What the adapter does on IRC: [[IrcReplies]] is written against it, and [[KittehIrcApi]] is the real one. */
trait IrcApi[F[_]] {

  /** Sends a message to a channel or a nick. It is one line: no line break, and short enough for the protocol. */
  def sendMessage(target: String, text: String): F[Unit]
}
