package com.benkio.telegrambotinfrastructure

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.model.Subscription
import com.benkio.telegrambotinfrastructure.model.TextReply
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBSubscription
import com.benkio.telegrambotinfrastructure.resources.db.DBSubscriptionData
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

  var memSubscriptions: MMap[BackgroundJobManager.SubscriptionKey, SignallingRef[F, Boolean]]

  def scheduleSubscription(subscription: Subscription): F[Unit]
  def loadSubscriptions(): F[Unit]
  def cancelSubscription(subscriptionId: UUID): F[Unit]
  def cancelSubscriptions(chatId: Long): F[Unit]
}

object BackgroundJobManager {

  final case class SubscriptionKey(subscriptionId: UUID, chatId: Long)

  final case class SubscriptionIdNotFound(subscriptionId: UUID)
      extends Throwable(s"Subscription Id is not found: $subscriptionId")
  final case class SubscriptionChatIdNotFound(chatId: Long)
      extends Throwable(s"Subscription chat Id is not found: $chatId")

  def apply[F[_]: Async](
      dbSubscription: DBSubscription[F],
      resourceAccess: ResourceAccess[F],
      youtubeLinkSources: String
  )(implicit replyAction: Action[F], log: LogWriter[F]): F[BackgroundJobManager[F]] = for {
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
  )(implicit replyAction: Action[F], log: LogWriter[F])
      extends BackgroundJobManager[F] {

    var memSubscriptions: MMap[SubscriptionKey, SignallingRef[F, Boolean]] = MMap()
    val cronScheduler                                                      = Cron4sScheduler.utc[F]

    override def scheduleSubscription(subscription: Subscription): F[Unit] = for {
      subscriptionReference <- BackgroundJobManager.runSubscription(
        subscription,
        cronScheduler,
        resourceAccess,
        youtubeLinkSources
      )
      _ <- dbSubscription.insertSubscription(DBSubscriptionData(subscription))
    } yield memSubscriptions += subscriptionReference

    override def loadSubscriptions(): F[Unit] = for {
      subscriptionsData <- dbSubscription.getSubscriptions()
      subscriptions     <- subscriptionsData.traverse(s => MonadThrow[F].fromEither(Subscription(s)))
      subscriptionReferences <- subscriptions.traverse(s =>
        BackgroundJobManager.runSubscription(s, cronScheduler, resourceAccess, youtubeLinkSources)
      )
    } yield memSubscriptions = MMap.from(subscriptionReferences)

    override def cancelSubscription(subscriptionId: UUID): F[Unit] = for {
      optCancellingBoolean <- Async[F].pure(
        memSubscriptions.find { case (SubscriptionKey(sId, _), _) => sId == subscriptionId }.map {
          case (_, cancelJob) => cancelJob
        }
      )
      _ <- optCancellingBoolean.fold[F[Unit]](Async[F].raiseError(SubscriptionIdNotFound(subscriptionId)))(_.set(true))
      _ <- dbSubscription.deleteSubscription(subscriptionId)
    } yield memSubscriptions = memSubscriptions.filterNot { case (SubscriptionKey(sId, _), _) => sId == subscriptionId }

    override def cancelSubscriptions(chatId: Long): F[Unit] = for {
      optCancellingBooleans <- Async[F].pure(
        memSubscriptions
          .filter { case (SubscriptionKey(_, cId), _) => cId == chatId }
          .map { case (_, cancelJob) => cancelJob }
          .toList
      )
      _ <- optCancellingBooleans.traverse(_.set(true))
      _ <- dbSubscription.deleteSubscriptions(chatId)
    } yield memSubscriptions = memSubscriptions.filterNot { case (SubscriptionKey(_, cId), _) => cId == chatId }
  }

  def runSubscription[F[_]: Async](
      subscription: Subscription,
      cronScheduler: Scheduler[F, CronExpr],
      resourceAccess: ResourceAccess[F],
      youtubeLinkSources: String
  )(implicit replyAction: Action[F], log: LogWriter[F]): F[(SubscriptionKey, SignallingRef[F, Boolean])] = {
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
    } yield (SubscriptionKey(subscription.id, subscription.chatId), c)
  }
}
