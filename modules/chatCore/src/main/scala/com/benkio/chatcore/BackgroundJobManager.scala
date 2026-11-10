package com.benkio.chatcore

import cats.Show
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionId
import com.benkio.chatcore.model.SubscriptionType
import fs2.concurrent.SignallingRef
import fs2.Stream
import log.effect.LogWriter

import java.time.Instant

trait BackgroundJobManager[F[_]] {
  def scheduleSubscription(subscription: Subscription): F[Unit]
  def getScheduledSubscriptions(): List[SubscriptionKey]
  def cancelSubscription(subscriptionId: SubscriptionId): F[Unit]
  def cancelSubscriptions(chatId: ChatId): F[Unit]
  // No-op when the chat has no subscription of the given type
  def cancelSubscriptionByType(chatId: ChatId, subscriptionType: SubscriptionType): F[Unit]

  /** Late-bound: the bot that handles the fired trigger subscriptions exists only after the manager. A trigger that
    * fires while no handler is set is logged as an error and dropped. The handler receives a synthetic message (fresh
    * date, the subscription target as text) and replies to it as if the chat had sent it.
    */
  def setTriggerHandler(handler: Message => F[Unit]): F[Unit]

  // For Testing Purposes
  def runSubscription(
      subscription: Subscription
  )(using log: LogWriter[F]): F[(Stream[F, Instant], SignallingRef[F, Boolean])]
}

final case class SubscriptionKey(
    subscriptionId: SubscriptionId,
    chatId: ChatId,
    subscriptionType: SubscriptionType
)
object SubscriptionKey {
  given showInstance: Show[SubscriptionKey] =
    Show.show(s => s"Subscription Id: `${s.subscriptionId}` - chat id: ${s.chatId}")
}
