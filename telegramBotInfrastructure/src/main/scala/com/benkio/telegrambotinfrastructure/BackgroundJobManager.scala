package com.benkio.telegrambotinfrastructure

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model.Subscription
import com.benkio.telegrambotinfrastructure.resources.db.DBSubscription
import cron4s.expr.CronExpr
import eu.timepit.fs2cron.Scheduler
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.Stream
import fs2.concurrent.Signal
import fs2.concurrent.SignallingRef

import java.util.UUID
import scala.collection.mutable.{ Map => MMap }

trait BackgroundJobManager[F[_]] {

  var memSubscriptions: MMap[UUID, SignallingRef[F, Boolean]]

  def scheduleSubscription(subscription: Subscription): F[Unit]
  def loadSubscriptions(): F[Unit]
  def cancelSubscription(chatId: Int): F[Unit]
}

// TODO: Implement and Test
object BackgroundJobManager {

  def apply[F[_]](): F[BackgroundJobManager[F]] = ???

  class BackgroundJobManagerImpl[F[_]: Async](dbSubscription: DBSubscription[F]) extends BackgroundJobManager[F] {
    var memSubscriptions: MMap[UUID, SignallingRef[F, Boolean]] = MMap()

    def scheduleSubscription(subscription: Subscription): F[Unit] = ???

    def loadSubscriptions(): F[Unit] = for {
      subscriptionsData <- dbSubscription.getSubscriptions()
      subscriptions     <- subscriptionsData.traverse(s => MonadThrow[F].fromEither(Subscription(s)))
      cronScheduler = Cron4sScheduler.utc[F]
      subscriptionReferences <- subscriptions.traverse(s => BackgroundJobManager.runSubscription(s, cronScheduler))
    } yield memSubscriptions = MMap.from(subscriptionReferences)

    def cancelSubscription(chatId: Int): F[Unit] = ???
  }

  def runSubscription[F[_]: Async](
      subscription: Subscription,
      cronScheduler: Scheduler[F, CronExpr]
  ): F[(UUID, SignallingRef[F, Boolean])] = {
    val scheduled: Stream[F, Unit] = cronScheduler.awakeEvery(subscription.cron) >> ???
    val cancel                     = SignallingRef[F, Boolean](false)

    for {
      c <- cancel
      _ <- Async[F].start(scheduled.interruptWhen(c: Signal[F, Boolean]).repeat.compile.drain)
    } yield (subscription.id, c)
  }
}
