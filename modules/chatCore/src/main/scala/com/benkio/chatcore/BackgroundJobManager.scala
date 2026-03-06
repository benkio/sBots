package com.benkio.chatcore

import cats.Show
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionId
import fs2.concurrent.SignallingRef
import fs2.Stream
import log.effect.LogWriter

import java.time.Instant

trait BackgroundJobManager[F[_]] {
  def scheduleSubscription(subscription: Subscription): F[Unit]
  def getScheduledSubscriptions(): List[SubscriptionKey]
  def cancelSubscription(subscriptionId: SubscriptionId): F[Unit]
  def cancelSubscriptions(chatId: ChatId): F[Unit]

  // For Testing Purposes
  def runSubscription(
      subscription: Subscription
  )(using log: LogWriter[F]): F[(Stream[F, Instant], SignallingRef[F, Boolean])]
}

final case class SubscriptionKey(subscriptionId: SubscriptionId, chatId: ChatId)
object SubscriptionKey {
  given showInstance: Show[SubscriptionKey] =
    Show.show(s => s"Subscription Id: ${s.subscriptionId} - chat id: ${s.chatId}")
}
