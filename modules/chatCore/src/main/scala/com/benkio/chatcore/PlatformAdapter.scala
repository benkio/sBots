package com.benkio.chatcore

import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.model.SubscriptionFlowKey

import scala.annotation.unused

/** The side of a platform (Telegram, IRC, Discord, Slack): the [[BotEngine]] knows nothing about it but this trait.
  *
  *   - inbound: the adapter maps what it receives from the platform to [[com.benkio.chatcore.model.Message]] and hands
  *     it to the engine, once it is started with [[run]]
  *   - outbound: the engine sends and computes the replies through [[PlatformReplies]]
  *   - [[capabilities]] tell what the platform can send, see [[com.benkio.chatcore.model.reply.ReplyTransformer]]
  *   - the media resources are read through `repository.usableFor(capabilities)`, which leaves out the file references
  *     the platform cannot use
  */
trait PlatformAdapter[F[_]] extends PlatformReplies[F] {

  def capabilities: PlatformCapabilities

  def platform: Platform = capabilities.platform

  /** Connects to the platform: from then on the inbound events are given to the engine. It completes when the
    * connection ends.
    */
  def run: F[Unit]

  /** Refreshes the idle timer of the prompt of a guided subscription draft. Adapters that show prompts keep their
    * handle alive on every flow answer, including an answer that is rejected and keeps the user on the same step; the
    * others have no prompt to refresh.
    */
  def touchFlowPrompt(@unused key: SubscriptionFlowKey)(using cats.Applicative[F]): F[Unit] = cats.Applicative[F].unit

  /** Drops the stored prompt handle of a guided subscription draft once the flow reached a terminal outcome. The next
    * flow for the same key should send a fresh prompt instead of editing a stale message buried in the chat.
    */
  def clearFlowPrompt(@unused key: SubscriptionFlowKey)(using cats.Applicative[F]): F[Unit] = cats.Applicative[F].unit
}
