package com.benkio.chatcore

import cats.*
import cats.effect.*
import cats.effect.std.Random
import cats.implicits.*
import com.benkio.chatcore.model.media.Media
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.Markdown
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.ReplyTransformer
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.show.CalendarMessage
import com.benkio.chatcore.model.show.Show
import com.benkio.chatcore.model.show.ShowCalendar
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.PlatformCapabilities
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionId
import com.benkio.chatcore.model.SubscriptionRules
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.patterns.CommandPatterns
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.db.DBSubscriptionData
import eu.timepit.fs2cron.cron4s.Cron4sScheduler
import fs2.concurrent.SignallingRef
import fs2.Stream
import log.effect.LogWriter

import java.time.Instant
import java.time.MonthDay
import scala.collection.mutable.Map as MMap
import scala.concurrent.duration.FiniteDuration

/** The [[BackgroundJobManager]] that fires the subscriptions of a bot on a cron schedule, whatever the platform: what is
  * sent goes through the injected `send`, adapted to the `capabilities` of the platform.
  */
object CronBackgroundJobManager {

  /** Sends a value to a chat. The boolean tells whether it is attached to the chat message (for the platforms with such
    * a thing): a subscription has no message to answer, it is up to the platform to deal with it.
    */
  type SendToChat[F[_]] = (ChatId, ReplyValue, Boolean) => F[Unit]

  final case class SubscriptionIdNotFound(subscriptionId: SubscriptionId)
      extends Throwable(s"Subscription Id is not found: $subscriptionId")
  final case class SubscriptionChatIdNotFound(chatId: ChatId)
      extends Throwable(s"Subscription chat Id is not found: $chatId")
  final case class StaleSubscription(subscription: Subscription)
      extends Throwable(
        s"Subscription is stale: no longer present in DB after awake. Operation Aborted. id: ${subscription.id.value}"
      )
  final case class SubscriptionAlreadyExists(subscription: Subscription)
      extends Throwable(
        s"Subscription already exists: already present in memory while subscribing to it. id: ${subscription.id.value} - ${subscription.chatId}"
      )

  /** @param calendarMessage
    *   the daily calendar of the shows (the bot name and the shows of the day): the platform can size it for its limits
    */
  def apply[F[_]: Async: Random](
      dbLayer: DBLayer[F],
      sBotInfo: SBotInfo,
      ttl: Option[FiniteDuration],
      capabilities: PlatformCapabilities,
      send: SendToChat[F],
      calendarMessage: (String, List[Show]) => Option[Markdown] = (botName, shows) =>
        CalendarMessage.render(botName, shows)
  )(using log: LogWriter[F]): F[BackgroundJobManager[F]] =
    for {
      backgroundJobManager <- Async[F].pure(
        new BackgroundJobManagerImpl(
          dbLayer = dbLayer,
          sBotInfo = sBotInfo,
          ttl = ttl,
          capabilities = capabilities,
          send = send,
          calendarMessage = calendarMessage
        )
      )
      _ <- backgroundJobManager.loadSubscriptions()
    } yield backgroundJobManager

  final class BackgroundJobManagerImpl[F[_]: Async: Random](
      dbLayer: DBLayer[F],
      sBotInfo: SBotInfo,
      ttl: Option[FiniteDuration],
      capabilities: PlatformCapabilities,
      send: SendToChat[F],
      calendarMessage: (String, List[Show]) => Option[Markdown]
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
        chatSubscriptions <- dbLayer.dbSubscription.getSubscriptions(sBotInfo.botId, Some(subscription.chatId))
        _                 <- Async[F]
          .raiseError(SubscriptionRules.SubscriptionLimitReached(subscription.chatId))
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
        _ <- dbLayer.dbSubscription.deleteSubscriptions(chatId, sBotInfo.botId)
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
        _ <- dbLayer.dbSubscription.deleteSubscriptionsByType(chatId, sBotInfo.botId, subscriptionType)
        _ <- optCancellingBooleans.values.toList.traverse_(cancelSignal => cancelSignal.set(true))
      } yield scheduleReferences = onGoingScheduleReferences
    }

    // What a fired subscription sends: the value and whether it is attached to the message of the chat
    private final case class ScheduledReply(value: ReplyValue, replyToMessage: Boolean)

    // The reply a trigger subscription sends: the one a message with the text of the target would get, or, with no
    // text, a random reply of the bot (as a show subscription with no target sends a random show). Only the matching is
    // applied: no timeout filter, no post computation, and neither the ignore prefix nor the forward filter of the bot,
    // as the text is not a message of the chat.
    private def triggerReply(subscription: Subscription): F[Option[ReplyValue]] = {
      val replies                               = dbLayer.dbMessageReplies.messageReplies
      val bundle: F[Option[ReplyBundleMessage]] =
        subscription.target.map(_.trim).filter(_.nonEmpty) match {
          case None =>
            Random[F].shuffleList(replies).map(_.headOption)
          case Some(text) =>
            ReplyBundleMessage.selectReplyBundleByText(text, subscription.chatId, replies).pure[F]
        }
      for {
        replyBundle <- bundle
        replyValue  <- replyBundle.traverse(selected => BotEngine.runPlainReply[F](selected.reply))
        _           <- Async[F].whenA(replyValue.isEmpty)(
          log.info(s"[BackgroundJobManager] trigger subscription ${subscription.id.value} has no reply to send")
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
          } yield calendarMessage(sBotInfo.botName.value, ShowCalendar.forDay(shows, today))
            .map(calendar => ScheduledReply(calendar, replyToMessage = true))
        case SubscriptionType.Trigger =>
          triggerReply(subscription)
            .map(_.map(reply => ScheduledReply(reply, replyToMessage = false)))
            // a failing trigger must not stop the schedule of the subscription
            .handleErrorWith(e =>
              log.error(s"[BackgroundJobManager] trigger subscription failed: $subscription. Error: $e").as(None)
            )
      }

    // What the platform can send: a media it cannot send becomes the link of the media, when it has one
    private def adaptToPlatform(value: ReplyValue): F[List[ReplyValue]] = value match {
      case mediaFile: MediaFile if !capabilities.supports(mediaFile) =>
        dbLayer.dbMedia
          .getMedia(mediaFile.filepath)
          .map(_.flatMap(data => Media(data).toOption).flatMap(_.getLink))
          .map(link => ReplyTransformer.transform(capabilities, _ => link)(mediaFile))
      case other => ReplyTransformer.transform(capabilities)(other).pure[F]
    }

    override def runSubscription(
        subscription: Subscription
    )(using
        log: LogWriter[F]
    ): F[(Stream[F, Instant], SignallingRef[F, Boolean])] = {
      val cancelF = SignallingRef[F, Boolean](false)

      val action: F[Instant] = for {
        now      <- Async[F].realTimeInstant
        _        <- log.info(s"[BackgroundJobManager] $now - fire subscription: $subscription")
        replyOpt <- subscriptionReply(subscription)
        _        <- replyOpt.traverse_ { case ScheduledReply(reply, replyToMessage) =>
          log.info(s"[BackgroundJobManager] reply: $reply") *>
            adaptToPlatform(reply).flatMap(_.traverse_(send(subscription.chatId, _, replyToMessage)))
        }
      } yield now // For testing purposes

      for cancel <- cancelF
      yield (Stream.eval(action).interruptWhen(cancel), cancel)
    }

    override def getScheduledSubscriptions(): List[SubscriptionKey] =
      scheduleReferences.keys.toList
  }
}
