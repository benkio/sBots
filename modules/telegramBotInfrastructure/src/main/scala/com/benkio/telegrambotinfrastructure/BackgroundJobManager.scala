package com.benkio.telegrambotinfrastructure

import cats.*
import cats.effect.*
import cats.effect.implicits.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.http.telegramreply.TextReply
import com.benkio.telegrambotinfrastructure.model.ChatId
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.Subscription
import com.benkio.telegrambotinfrastructure.model.SubscriptionId
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.db.DBSubscriptionData
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.concurrent.SignallingRef
import fs2.Stream
import log.effect.LogWriter
import telegramium.bots.high.Api
import telegramium.bots.Chat
import telegramium.bots.Message

import java.time.Instant
import scala.collection.mutable.Map as MMap
import scala.concurrent.duration.FiniteDuration

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
given showInstance: Show[SubscriptionKey] =
  Show.show(s => s"Subscription Id: ${s.subscriptionId} - chat id: ${s.chatId}")

object BackgroundJobManager {

  final case class SubscriptionIdNotFound(subscriptionId: SubscriptionId)
      extends Throwable(s"Subscription Id is not found: $subscriptionId")
  final case class SubscriptionChatIdNotFound(chatId: Long)
      extends Throwable(s"Subscription chat Id is not found: $chatId")
  final case class StaleSubscription(subscription: Subscription)
      extends Throwable(
        s"Subscription is stale: no longer present in DB after awake. Operation Aborted. id: ${subscription.id.value}"
      )
  final case class SubscriptionAlreadyExists(subscription: Subscription)
      extends Throwable(
        s"Subscription already exists: already present in memory while subscribing to it. id: ${subscription.id.value} - ${subscription.chatId.value}"
      )

  def apply[F[_]: Async: Api](
      dbLayer: DBLayer[F],
      sBotInfo: SBotInfo,
      ttl: Option[FiniteDuration]
  )(using log: LogWriter[F]): F[BackgroundJobManager[F]] =
    for {
      backgroundJobManager <- Async[F].pure(
        new BackgroundJobManagerImpl(
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = ttl
        )
      )
      _ <- backgroundJobManager.loadSubscriptions()
    } yield backgroundJobManager

  class BackgroundJobManagerImpl[F[_]: Async: Api](
      dbLayer: DBLayer[F],
      sBotInfo: SBotInfo,
      ttl: Option[FiniteDuration]
  )(using log: LogWriter[F])
      extends BackgroundJobManager[F] {

    val cronScheduler                                                        = Cron4sScheduler.systemDefault[F]
    var scheduleReferences: MMap[SubscriptionKey, SignallingRef[F, Boolean]] = MMap.empty

    def loadSubscriptions(): F[Unit] =
      for {
        subscriptionsData <- dbLayer.dbSubscription.getSubscriptions(sBotInfo.botId)
        subscriptions     <- subscriptionsData.traverse(s => MonadThrow[F].fromEither(Subscription(s)))
        cancelSignal      <- subscriptions.traverse(subscription =>
          runSubscription(subscription).map { case (stream, cancel) =>
            ((SubscriptionKey(subscription.id, subscription.chatId), cancel), (subscription.cron, stream))
          }
        )
        _ <- cronScheduler
          .schedule(
            cancelSignal.map(_._2)
          )
          .compile
          .drain
          .start
      } yield scheduleReferences = MMap.from(cancelSignal.map(_._1))

    override def scheduleSubscription(subscription: Subscription): F[Unit] =
      for {
        _ <- log.info(s"[BackgroundJobManager] Schedule subscription: $subscription")
        _ <- Async[F]
          .raiseError(SubscriptionAlreadyExists(subscription))
          .whenA(scheduleReferences.contains(SubscriptionKey(subscription.id, subscription.chatId)))
        _                <- dbLayer.dbSubscription.insertSubscription(DBSubscriptionData(subscription))
        (stream, cancel) <- runSubscription(subscription)
        cronStream = cronScheduler.awakeEvery(subscription.cron) >> stream
        _ <- cronStream.interruptWhen(cancel).compile.drain.start
      } yield scheduleReferences += SubscriptionKey(subscription.id, subscription.chatId) -> cancel

    override def cancelSubscription(subscriptionId: SubscriptionId): F[Unit] = {
      val (optCancellingBoolean, onGoingScheduleReferences) = scheduleReferences.partition {
        case (SubscriptionKey(sId, _), _) => sId == subscriptionId
      }
      for {
        _ <-
          if optCancellingBoolean.isEmpty
          then Async[F].raiseError(SubscriptionIdNotFound(subscriptionId))
          else optCancellingBoolean.values.toList.traverse_(cancelSignal => cancelSignal.set(true))
        _ <- dbLayer.dbSubscription.deleteSubscription(subscriptionId.value)
      } yield scheduleReferences = onGoingScheduleReferences
    }

    override def cancelSubscriptions(chatId: ChatId): F[Unit] = {
      val (optCancellingBooleans, onGoingScheduleReferences) = scheduleReferences.partition {
        case (SubscriptionKey(_, cId), _) => cId == chatId
      }
      for {
        _ <- log.info(s"[BackgroundJobManager] Cancel subscriptions for: $chatId")
        _ <- dbLayer.dbSubscription.deleteSubscriptions(chatId.value)
        _ <- optCancellingBooleans.values.toList.traverse_(cancelSignal => cancelSignal.set(true))
      } yield scheduleReferences = onGoingScheduleReferences
    }

    override def runSubscription(
        subscription: Subscription
    )(using
        log: LogWriter[F]
    ): F[(Stream[F, Instant], SignallingRef[F, Boolean])] = {
      val message = Message(
        messageId = 0,
        date = 0,
        chat = Chat(id = subscription.chatId.value, `type` = "private")
      ) // Only the chat id matters here
      val cancelF = SignallingRef[F, Boolean](false)

      val action: F[Instant] = for {
        now   <- Async[F].realTimeInstant
        _     <- log.info(s"[BackgroundJobManager] $now - fire subscription: $subscription")
        reply <- CommandPatterns.SearchShowCommand.selectLinkByKeyword[F]("", dbLayer.dbShow, sBotInfo, ttl)
        _     <- log.info(s"[BackgroundJobManager] reply: $reply")
        _     <- TextReply.sendText[F](
          reply = reply,
          msg = message,
          replyToMessage = true
        )
      } yield now // For testing purposes

      for cancel <- cancelF
      yield (Stream.eval(action).interruptWhen(cancel), cancel)
    }

    override def getScheduledSubscriptions(): List[SubscriptionKey] =
      scheduleReferences.keys.toList
  }
}
