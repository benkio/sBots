package com.benkio.telegrambotinfrastructure

import telegramium.bots.high.Api
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.telegram.TelegramReply
import scala.concurrent.duration.FiniteDuration
import java.time.Duration
import scala.util.Try
import java.time.LocalDateTime
import cats.*

import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.model.Subscription
import com.benkio.telegrambotinfrastructure.model.Text
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns
import com.benkio.telegrambotinfrastructure.resources.db.DBShow
import com.benkio.telegrambotinfrastructure.resources.db.DBSubscription
import com.benkio.telegrambotinfrastructure.resources.db.DBSubscriptionData
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

  given showInstance: Show[SubscriptionKey] =
    Show.show(s => s"Subscription Id: ${s.subscriptionId} - chat id: ${s.chatId}")

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

  def apply[F[_]: Async: Api](
      dbSubscription: DBSubscription[F],
      dbShow: DBShow[F],
      resourceAccess: ResourceAccess[F],
      botName: String
  )(using textTelegramReply: TelegramReply[Text], log: LogWriter[F]): F[BackgroundJobManager[F]] = for {
    backgroundJobManager <- Async[F].pure(
      new BackgroundJobManagerImpl(
        dbSubscription = dbSubscription,
        dbShow = dbShow,
        resourceAccess: ResourceAccess[F],
        botName = botName
      )
    )
    _ <- backgroundJobManager.loadSubscriptions()
  } yield backgroundJobManager

  class BackgroundJobManagerImpl[F[_]: Async: Api](
      dbSubscription: DBSubscription[F],
      dbShow: DBShow[F],
      resourceAccess: ResourceAccess[F],
      val botName: String
  )(using textTelegramReply: TelegramReply[Text], log: LogWriter[F])
      extends BackgroundJobManager[F] {

    var memSubscriptions: MMap[SubscriptionKey, Fiber[F, Throwable, Unit]] = MMap()

    override def scheduleSubscription(subscription: Subscription): F[Unit] = for {
      _ <- log.info(s"Schedule subscription: $subscription")
      _ <- Async[F]
        .raiseError(SubscriptionAlreadyExists(subscription))
        .whenA(memSubscriptions.contains(SubscriptionKey(subscription.id, subscription.chatId)))
      subscriptionReference <- BackgroundJobManager.runSubscription(
        subscription,
        dbShow,
        dbSubscription,
        botName,
        resourceAccess
      )
      _ <- dbSubscription.insertSubscription(DBSubscriptionData(subscription))
    } yield memSubscriptions += subscriptionReference

    override def loadSubscriptions(): F[Unit] = for {
      subscriptionsData <- dbSubscription.getSubscriptions(botName)
      subscriptions     <- subscriptionsData.traverse(s => MonadThrow[F].fromEither(Subscription(s)))
      subscriptionReferences <- subscriptions.traverse(s =>
        BackgroundJobManager.runSubscription(s, dbShow, dbSubscription, botName, resourceAccess)
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

  def runSubscription[F[_]: Async: Api](
      subscription: Subscription,
      dbShow: DBShow[F],
      dbSubscription: DBSubscription[F],
      botName: String,
      resourceAccess: ResourceAccess[F]
  )(using
      textTelegramReply: TelegramReply[Text],
      log: LogWriter[F]
  ): F[(SubscriptionKey, Fiber[F, Throwable, Unit])] = {
    val scheduled: Stream[F, Unit] = for {
      nextTime <- Stream.fromOption(subscription.cronScheduler.next())
      now = LocalDateTime.now()
      duration <- Stream.eval(Async[F].fromTry(Try(Duration.between(now, nextTime))))
      _        <- Stream.sleep(FiniteDuration(duration.getSeconds(), "seconds"))
      _        <- Stream.eval(log.info(s"Executing the Scheduled subscription: $subscription"))
      subOpt   <- Stream.eval(dbSubscription.getSubscription(subscription.id.toString))
      _        <- Stream.eval(Async[F].fromOption(subOpt, StaleSubscription(subscription)))
      message = Message(
        messageId = 0,
        date = 0,
        chat = Chat(id = subscription.chatId, `type` = "private")
      ) // Only the chat id matters here
      reply <- Stream.evalSeq(
        CommandPatterns.RandomLinkCommand
          .selectRandomLinkByKeyword[F]("", dbShow, botName)
      )
      _ <- Stream
        .eval(
          textTelegramReply.reply(
            reply = Text(reply),
            msg = message,
            resourceAccess = resourceAccess,
            replyToMessage = true
          )
        )
        .drain
    } yield ()

    Async[F]
      .start(scheduled.repeat.compile.drain)
      .map(fiber => (SubscriptionKey(subscription.id, subscription.chatId), fiber))
  }
}
