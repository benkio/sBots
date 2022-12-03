package com.benkio.telegrambotinfrastructure

import cats._
import cats.effect.Fiber
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.model.Subscription
import com.benkio.telegrambotinfrastructure.model.TextReply
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns
import com.benkio.telegrambotinfrastructure.resources.db.DBShow
import com.benkio.telegrambotinfrastructure.resources.db.DBSubscription
import com.benkio.telegrambotinfrastructure.resources.db.DBSubscriptionData
import cron4s.expr.CronExpr
import eu.timepit.fs2cron.Scheduler
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.Stream
import log.effect.LogWriter
import telegramium.bots.Chat
import telegramium.bots.Message

import java.util.UUID
import scala.collection.mutable.{ Map => MMap }

trait BackgroundJobManager[F[_]] {

  var memSubscriptions: MMap[BackgroundJobManager.SubscriptionKey, Fiber[F, Throwable, Unit]]
  val botName: String
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
  final case class StaleSubscription(subscription: Subscription)
      extends Throwable(
        s"Subscription is stale: no longer present in DB after awake. Operation Aborted. id: ${subscription.id}"
      )
  final case class SubscriptionAlreadyExists(subscription: Subscription)
      extends Throwable(
        s"Subscription already exists: already present in memory while subscribing to it. id: ${subscription.id} - ${subscription.chatId}"
      )

  def apply[F[_]: Async](
      dbSubscription: DBSubscription[F],
      dbShow: DBShow[F],
      botName: String
  )(implicit replyAction: Action[F], log: LogWriter[F]): F[BackgroundJobManager[F]] = for {
    backgroundJobManager <- Async[F].pure(
      new BackgroundJobManagerImpl(
        dbSubscription = dbSubscription,
        dbShow = dbShow,
        botName = botName
      )
    )
    _ <- backgroundJobManager.loadSubscriptions()
  } yield backgroundJobManager

  class BackgroundJobManagerImpl[F[_]: Async](
      dbSubscription: DBSubscription[F],
      dbShow: DBShow[F],
      val botName: String
  )(implicit replyAction: Action[F], log: LogWriter[F])
      extends BackgroundJobManager[F] {

    var memSubscriptions: MMap[SubscriptionKey, Fiber[F, Throwable, Unit]] = MMap()
    val cronScheduler                                                      = Cron4sScheduler.utc[F]

    override def scheduleSubscription(subscription: Subscription): F[Unit] = for {
      _ <- log.info(s"Schedule subscription: $subscription")
      _ <- Async[F]
        .raiseError(SubscriptionAlreadyExists(subscription))
        .whenA(memSubscriptions.contains(SubscriptionKey(subscription.id, subscription.chatId)))
      subscriptionReference <- BackgroundJobManager.runSubscription(
        subscription,
        cronScheduler,
        dbShow,
        dbSubscription,
        botName
      )
      _ <- dbSubscription.insertSubscription(DBSubscriptionData(subscription))
    } yield memSubscriptions += subscriptionReference

    override def loadSubscriptions(): F[Unit] = for {
      subscriptionsData <- dbSubscription.getSubscriptions(botName)
      subscriptions     <- subscriptionsData.traverse(s => MonadThrow[F].fromEither(Subscription(s)))
      subscriptionReferences <- subscriptions.traverse(s =>
        BackgroundJobManager.runSubscription(s, cronScheduler, dbShow, dbSubscription, botName)
      )
      _ <- memSubscriptions.values.toList.traverse_(_.cancel)
    } yield memSubscriptions = MMap.from(subscriptionReferences)

    override def cancelSubscription(subscriptionId: UUID): F[Unit] = for {
      optCancellingBoolean <- Async[F].pure(
        memSubscriptions.find { case (SubscriptionKey(sId, _), _) => sId == subscriptionId }.map {
          case (_, cancelJob) => cancelJob
        }
      )
      _ <- optCancellingBoolean.fold[F[Unit]](Async[F].raiseError(SubscriptionIdNotFound(subscriptionId)))(_.cancel)
      _ <- dbSubscription.deleteSubscription(subscriptionId)
    } yield memSubscriptions = memSubscriptions.filterNot { case (SubscriptionKey(sId, _), _) => sId == subscriptionId }

    override def cancelSubscriptions(chatId: Long): F[Unit] = for {
      optCancellingBooleans <- Async[F].pure(
        memSubscriptions
          .filter { case (SubscriptionKey(_, cId), _) => cId == chatId }
          .map { case (_, cancelJob) => cancelJob }
          .toList
      )
      _ <- optCancellingBooleans.traverse(_.cancel)
      _ <- dbSubscription.deleteSubscriptions(chatId)
    } yield memSubscriptions = memSubscriptions.filterNot { case (SubscriptionKey(_, cId), _) => cId == chatId }
  }

  def runSubscription[F[_]: Async](
      subscription: Subscription,
      cronScheduler: Scheduler[F, CronExpr],
      dbShow: DBShow[F],
      dbSubscription: DBSubscription[F],
      botName: String
  )(implicit replyAction: Action[F], log: LogWriter[F]): F[(SubscriptionKey, Fiber[F, Throwable, Unit])] = {
    val scheduled: Stream[F, Unit] = for {
      _      <- cronScheduler.awakeEvery(subscription.cron)
      _      <- Stream.eval(log.info(s"Executing the Scheduled subscription: $subscription"))
      subOpt <- Stream.eval(dbSubscription.getSubscription(subscription.id.toString))
      _      <- Stream.eval(Async[F].fromOption(subOpt, StaleSubscription(subscription)))
      message = Message(
        messageId = 0,
        date = 0,
        chat = Chat(id = subscription.chatId, `type` = "private")
      ) // Only the chat id matters here
      reply = TextReply[F](_ =>
        CommandPatterns.RandomLinkCommand
          .selectRandomLinkByKeyword[F]("", dbShow, botName)
      )
      _ <- Stream.eval(replyAction(reply)(message))
    } yield ()

    Async[F]
      .start(scheduled.repeat.compile.drain)
      .map(fiber => (SubscriptionKey(subscription.id, subscription.chatId), fiber))
  }
}
