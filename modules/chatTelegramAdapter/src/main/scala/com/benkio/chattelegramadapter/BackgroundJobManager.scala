package com.benkio.chattelegramadapter

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionId
import com.benkio.chatcore.model.SubscriptionRules
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.patterns.CommandPatterns
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.db.DBSubscriptionData
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.SubscriptionKey
import com.benkio.chattelegramadapter.http.telegramreply.messagereply.TelegramMessageReply
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.concurrent.SignallingRef
import fs2.Stream
import log.effect.LogWriter
import telegramium.bots.high.Api

import java.time.Instant
import scala.collection.mutable.Map as MMap
import scala.concurrent.duration.FiniteDuration

object TelegramBackgroundJobManager {

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
      repository: Repository[F],
      dbLayer: DBLayer[F],
      sBotInfo: SBotInfo,
      ttl: Option[FiniteDuration]
  )(using log: LogWriter[F]): F[BackgroundJobManager[F]] =
    for {
      backgroundJobManager <- Async[F].pure(
        new BackgroundJobManagerImpl(
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = ttl
        )
      )
      _ <- backgroundJobManager.loadSubscriptions()
    } yield backgroundJobManager

  final class BackgroundJobManagerImpl[F[_]: Async: Api](
      repository: Repository[F],
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
        // A stored row that cannot be decoded must not prevent the bot from starting: log and skip it
        subscriptions <- subscriptionsData.flatTraverse(s =>
          Subscription(s).fold(
            e =>
              log
                .error(s"[BackgroundJobManager] Skipping invalid stored subscription ${s.id}: ${e.getMessage}")
                .as(List.empty[Subscription]),
            subscription => List(subscription).pure[F]
          )
        )
        cancelSignal <- subscriptions.traverse(subscription =>
          runSubscription(subscription).map { case (stream, cancel) =>
            (
              (SubscriptionKey(subscription.id, subscription.chatId, subscription.subscriptionType), cancel),
              (subscription.cron, stream)
            )
          }
        )
        _ <- Async[F]
          .start(
            cronScheduler
              .schedule(
                cancelSignal.map(_._2)
              )
              .compile
              .drain
          )
          .void
      } yield scheduleReferences = MMap.from(cancelSignal.map(_._1))

    override def scheduleSubscription(subscription: Subscription): F[Unit] =
      for {
        _ <- log.info(s"[BackgroundJobManager] Schedule subscription: $subscription")
        _ <- Async[F]
          .raiseError(SubscriptionAlreadyExists(subscription))
          .whenA(scheduleReferences.keys.exists(_.subscriptionId == subscription.id))
        chatSubscriptions <- dbLayer.dbSubscription.getSubscriptions(sBotInfo.botId, Some(subscription.chatId.value))
        _                 <- Async[F]
          .raiseError(SubscriptionRules.SubscriptionLimitReached(subscription.chatId.value))
          .whenA(chatSubscriptions.size >= SubscriptionRules.maxSubscriptionsPerChat)
        _               <- dbLayer.dbSubscription.insertSubscription(DBSubscriptionData(subscription))
        streamAndCancel <- runSubscription(subscription)
        (stream, cancel) = streamAndCancel
        cronStream       = cronScheduler.awakeEvery(subscription.cron) >> stream
        _ <- Async[F].start(cronStream.interruptWhen(cancel).compile.drain).void
      } yield scheduleReferences += SubscriptionKey(
        subscription.id,
        subscription.chatId,
        subscription.subscriptionType
      ) -> cancel

    override def cancelSubscription(subscriptionId: SubscriptionId): F[Unit] = {
      val (optCancellingBoolean, onGoingScheduleReferences) = scheduleReferences.partition {
        case (SubscriptionKey(sId, _, _), _) => sId == subscriptionId
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
        case (SubscriptionKey(_, cId, _), _) => cId == chatId
      }
      for {
        _ <- log.info(s"[BackgroundJobManager] Cancel subscriptions for: $chatId")
        _ <- dbLayer.dbSubscription.deleteSubscriptions(chatId.value)
        _ <- optCancellingBooleans.values.toList.traverse_(cancelSignal => cancelSignal.set(true))
      } yield scheduleReferences = onGoingScheduleReferences
    }

    override def cancelSubscriptionByType(chatId: ChatId, subscriptionType: SubscriptionType): F[Unit] = {
      val (optCancellingBooleans, onGoingScheduleReferences) = scheduleReferences.partition {
        case (SubscriptionKey(_, cId, sType), _) => cId == chatId && sType == subscriptionType
      }
      for {
        _ <- log.info(
          s"[BackgroundJobManager] Cancel ${subscriptionType.asString} subscriptions for: $chatId"
        )
        _ <- dbLayer.dbSubscription.deleteSubscriptionsByType(chatId.value, sBotInfo.botId, subscriptionType)
        _ <- optCancellingBooleans.values.toList.traverse_(cancelSignal => cancelSignal.set(true))
      } yield scheduleReferences = onGoingScheduleReferences
    }

    // The reply a subscription sends when it fires, if any
    private def subscriptionReply(subscription: Subscription): F[Option[ReplyValue]] =
      subscription.subscriptionType match {
        case SubscriptionType.Show =>
          CommandPatterns.SearchShowCommand
            .selectShowByIdOrKeyword[F](subscription.target.getOrElse(""), dbLayer.dbShow, sBotInfo, ttl)
            .map(Some(_))
        case other =>
          log
            .warn(s"[BackgroundJobManager] ${other.asString} subscriptions are not supported yet: $subscription")
            .as(None)
      }

    override def runSubscription(
        subscription: Subscription
    )(using
        log: LogWriter[F]
    ): F[(Stream[F, Instant], SignallingRef[F, Boolean])] = {
      val message = Message(
        messageId = 0,
        date = 0L,
        chatId = subscription.chatId,
        chatType = "private"
      ) // Only the chat id & message id matters here
      val cancelF = SignallingRef[F, Boolean](false)

      val action: F[Instant] = for {
        now      <- Async[F].realTimeInstant
        _        <- log.info(s"[BackgroundJobManager] $now - fire subscription: $subscription")
        replyOpt <- subscriptionReply(subscription)
        _        <- replyOpt.traverse_ { reply =>
          log.info(s"[BackgroundJobManager] reply: $reply") *>
            TelegramMessageReply
              .sendReplyValue[F](
                replyValue = reply,
                msg = message,
                repository = repository,
                replyToMessage = true
              )
              .void
        }
      } yield now // For testing purposes

      for cancel <- cancelF
      yield (Stream.eval(action).interruptWhen(cancel), cancel)
    }

    override def getScheduledSubscriptions(): List[SubscriptionKey] =
      scheduleReferences.keys.toList
  }
}
