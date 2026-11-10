package com.benkio.chattelegramadapter

import cats.*
import cats.effect.*
import cats.effect.std.Random
import cats.implicits.*
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.show.Show
import com.benkio.chatcore.model.show.ShowCalendar
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
import com.benkio.chattelegramadapter.model.TelegramCalendarMessage
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.concurrent.SignallingRef
import fs2.Stream
import log.effect.LogWriter
import telegramium.bots.high.Api

import java.time.Instant
import java.time.MonthDay
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

  def apply[F[_]: Async: Api: Random](
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

  final class BackgroundJobManagerImpl[F[_]: Async: Api: Random](
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
        _ <- Async[F]
          .raiseError(SubscriptionRules.CalendarAlreadyActive)
          .whenA(
            subscription.subscriptionType == SubscriptionType.Calendar &&
              chatSubscriptions.exists(_.subscription_type == SubscriptionType.Calendar.asString)
          )
        // The partial unique index is the safety net for concurrent loads of the calendar
        _ <- dbLayer.dbSubscription
          .insertSubscription(DBSubscriptionData(subscription))
          .adaptError {
            case e: java.sql.SQLException
                if subscription.subscriptionType == SubscriptionType.Calendar &&
                  Option(e.getMessage).exists(_.contains("UNIQUE constraint failed")) =>
              SubscriptionRules.CalendarAlreadyActive
          }
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

    // What a fired subscription sends: the value and whether it is attached to the (synthetic) message of the chat
    private final case class ScheduledReply(value: ReplyValue, replyToMessage: Boolean)

    // The reply a trigger subscription sends: the one a live message with the text of the target would get. Only the
    // matching is applied: no timeout filter, no post computation, and neither the ignore prefix nor the forward filter
    // of the bot, as the text is not a message of the chat. The message is dated now, so it passes `FilteringOlder`.
    private def triggerReply(subscription: Subscription): F[Option[ReplyValue]] =
      subscription.target.map(_.trim).filter(_.nonEmpty) match {
        case None =>
          log
            .warn(s"[BackgroundJobManager] trigger subscription without text, nothing to fire: $subscription")
            .as(None)
        case Some(text) =>
          for {
            now <- Async[F].realTimeInstant
            triggerMessage = Message(
              messageId = 0,
              date = now.getEpochSecond,
              chatId = subscription.chatId,
              chatType = "private",
              text = Some(text)
            )
            replyBundle = ReplyBundleMessage.selectReplyBundle(
              msg = triggerMessage,
              messageRepliesData = dbLayer.dbMessageReplies.messageReplies,
              ignoreMessagePrefix = None,
              disableForward = false
            )
            replyValue <- replyBundle.traverse(bundle => ComputeReply.runPlainReply[F](bundle.reply))
            _          <- Async[F].whenA(replyValue.isEmpty)(
              log.info(s"[BackgroundJobManager] trigger `$text` matches no reply bundle, nothing sent")
            )
          } yield replyValue
      }

    // The reply a subscription sends when it fires, if any
    private def subscriptionReply(subscription: Subscription): F[Option[ScheduledReply]] =
      subscription.subscriptionType match {
        case SubscriptionType.Show =>
          CommandPatterns.SearchShowCommand
            .selectShowByIdOrKeyword[F](subscription.target.getOrElse(""), dbLayer.dbShow, sBotInfo, ttl)
            .map(show => Some(ScheduledReply(show, replyToMessage = true)))
        case SubscriptionType.Calendar =>
          for {
            shows <- dbLayer.dbShow.getShows(sBotInfo.botId).flatMap(_.traverse(Show[F](_)))
            today <- Async[F].delay(MonthDay.now())
          } yield TelegramCalendarMessage
            .render(sBotInfo.botName.value, ShowCalendar.forDay(shows, today))
            .map(calendar => ScheduledReply(calendar, replyToMessage = true))
        case SubscriptionType.Trigger =>
          triggerReply(subscription)
            .map(_.map(reply => ScheduledReply(reply, replyToMessage = false)))
            // a failing trigger must not stop the schedule of the subscription
            .handleErrorWith(e =>
              log.error(s"[BackgroundJobManager] trigger subscription failed: $subscription. Error: $e").as(None)
            )
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
        _        <- replyOpt.traverse_ { case ScheduledReply(reply, replyToMessage) =>
          log.info(s"[BackgroundJobManager] reply: $reply") *>
            TelegramMessageReply
              .sendReplyValue[F](
                replyValue = reply,
                msg = message,
                repository = repository,
                replyToMessage = replyToMessage
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
