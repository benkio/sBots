package com.benkio.telegrambotinfrastructure

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.model.Reply
import com.benkio.telegrambotinfrastructure.model.Subscription
import com.benkio.telegrambotinfrastructure.model.TextReply
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBSubscription
import cron4s.expr.CronExpr
import eu.timepit.fs2cron.Scheduler
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.Stream
import fs2.concurrent.Signal
import fs2.concurrent.SignallingRef
import log.effect.LogWriter
import telegramium.bots.Chat
import telegramium.bots.Message

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

  def apply[F[_]: Async](
      dbSubscription: DBSubscription[F],
      resourceAccess: ResourceAccess[F],
      youtubeLinkSources: String
  )(implicit replyAction: Action[Reply, F], log: LogWriter[F]): F[BackgroundJobManager[F]] = for {
    backgroundJobManager <- Async[F].pure(
      new BackgroundJobManagerImpl(
        dbSubscription = dbSubscription,
        resourceAccess = resourceAccess,
        youtubeLinkSources = youtubeLinkSources
      )
    )
    _ <- backgroundJobManager.loadSubscriptions()
  } yield backgroundJobManager

  class BackgroundJobManagerImpl[F[_]: Async](
      dbSubscription: DBSubscription[F],
      resourceAccess: ResourceAccess[F],
      youtubeLinkSources: String
  )(implicit replyAction: Action[Reply, F], log: LogWriter[F])
      extends BackgroundJobManager[F] {
    var memSubscriptions: MMap[UUID, SignallingRef[F, Boolean]] = MMap()

    def scheduleSubscription(subscription: Subscription): F[Unit] = ???

    def loadSubscriptions(): F[Unit] = for {
      subscriptionsData <- dbSubscription.getSubscriptions()
      subscriptions     <- subscriptionsData.traverse(s => MonadThrow[F].fromEither(Subscription(s)))
      cronScheduler = Cron4sScheduler.utc[F]
      subscriptionReferences <- subscriptions.traverse(s =>
        BackgroundJobManager.runSubscription(s, cronScheduler, resourceAccess, youtubeLinkSources)
      )
    } yield memSubscriptions = MMap.from(subscriptionReferences)

    def cancelSubscription(chatId: Int): F[Unit] = ???
  }

  def runSubscription[F[_]: Async](
      subscription: Subscription,
      cronScheduler: Scheduler[F, CronExpr],
      resourceAccess: ResourceAccess[F],
      youtubeLinkSources: String
  )(implicit replyAction: Action[Reply, F], log: LogWriter[F]): F[(UUID, SignallingRef[F, Boolean])] = {
    val scheduled: Stream[F, Unit] = for {
      _ <- cronScheduler.awakeEvery(subscription.cron)
      _ <- Stream.eval(log.info(s"Executing the Scheduled subscription: $subscription"))
      message = Message(
        messageId = 0,
        date = 0,
        chat = Chat(id = subscription.chatId, `type` = "private")
      ) // Only the chat id matters here
      reply = TextReply[F](_ =>
        CommandPatterns.RandomLinkCommand
          .selectRandomLinkByKeyword[F]("", resourceAccess, youtubeLinkSources)
          .use(optMessage => Applicative[F].pure(optMessage.toList))
      )
      _ <- Stream.eval(replyAction(reply)(message))
    } yield ()
    val cancel = SignallingRef[F, Boolean](false)

    for {
      c <- cancel
      _ <- Async[F].start(scheduled.interruptWhen(c: Signal[F, Boolean]).repeat.compile.drain)
    } yield (subscription.id, c)
  }
}
