package com.benkio.chatcore

import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.PlatformCapabilities

/** The side of a platform (Telegram, IRC, Discord, Slack): the [[BotEngine]] knows nothing about it but this trait.
  *
  *   - inbound: the adapter maps what it receives from the platform to [[com.benkio.chatcore.model.Message]] and hands
  *     it to the engine, once it is started with [[run]]
  *   - outbound: the engine sends and computes the replies through [[PlatformReplies]]
  *   - [[capabilities]] tell what the platform can send, see [[com.benkio.chatcore.model.reply.ReplyTransformer]]
  */
trait PlatformAdapter[F[_]] extends PlatformReplies[F] {

  def capabilities: PlatformCapabilities

  def platform: Platform = capabilities.platform

  /** Connects to the platform: from then on the inbound events are given to the engine. It completes when the
    * connection ends.
    */
  def run: F[Unit]
}
