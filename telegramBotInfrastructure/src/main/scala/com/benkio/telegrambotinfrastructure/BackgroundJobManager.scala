package com.benkio.telegrambotinfrastructure

import com.benkio.telegrambotinfrastructure.model.Subscription
import fs2.concurrent.SignallingRef

trait BackgroundJobManager[F[_]] {

  val memSubscriptions: Map[Int, SignallingRef[F, Boolean]]

  def scheduleSubscription(subscription: Subscription): F[Unit]
  def loadSubscriptions(): F[Unit]
  def cancelSubscription(chatId: Int): F[Unit]
}

//TODO: Implement
