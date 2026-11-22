package com.benkio.chatcore.patterns

import cats.effect.Async
import cats.implicits.*
import cats.MonadThrow
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.Markdown
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.show.Show
import com.benkio.chatcore.model.show.ShowCalendar
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.SubscribeArguments
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionRules
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.model.SubscriptionsTable
import com.benkio.chatcore.model.UnsubscribeArguments
import com.benkio.chatcore.repository.db.*
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.SubscriptionKey
import cron4s.expr.CronExpr
import cron4s.lib.javatime.*
import cron4s.syntax.all.*

import java.time.LocalDateTime
import scala.concurrent.duration.*

/** Commands that manage the bot subscriptions, including the calendar subscription. */
object SubscriptionCommands {

  object CalendarCommands {
    // Every day at 09:00, server time
    val calendarCron: String = "0 0 9 * * ?"

    private[patterns] val loadCalendarCommandDescriptionIta: String =
      s"'/loadcalendar': Documentazione: ${CommandDocs.chatCoreCommandDocLink("loadcalendar", "it")}"
    private[patterns] val loadCalendarCommandDescriptionEng: String =
      s"'/loadcalendar': Documentation: ${CommandDocs.chatCoreCommandDocLink("loadcalendar", "en")}"
    private[patterns] val showCalendarCommandDescriptionIta: String =
      s"'/showcalendar': Documentazione: ${CommandDocs.chatCoreCommandDocLink("showcalendar", "it")}"
    private[patterns] val showCalendarCommandDescriptionEng: String =
      s"'/showcalendar': Documentation: ${CommandDocs.chatCoreCommandDocLink("showcalendar", "en")}"

    val noCalendarMessage: String = "no calendar"

    private def botShows[F[_]: MonadThrow](dbShow: DBShow[F], sBotInfo: SBotInfo): F[List[Show]] =
      dbShow.getShows(sBotInfo.botId).flatMap(_.traverse(Show[F](_)))

    // The calendar of a bot is empty when no show is a candidate for any day
    private def hasCalendar(shows: List[Show]): Boolean = ShowCalendar.orderedCandidates(shows).nonEmpty

    def calendarFileLink(sBotInfo: SBotInfo): String =
      s"${CommandDocs.commandsDocsBaseUrl}modules/bots/${sBotInfo.botName.value}/${sBotInfo.botId.value}_calendar.ics"

    def loadCalendarCommandLogic[F[_]: Async](
        backgroundJobManager: BackgroundJobManager[F],
        dbShow: DBShow[F],
        m: Message,
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration],
        commandSyntax: CommandSyntax = CommandSyntax.Slash
    ): F[ReplyValue] = {
      val scheduleCalendar: F[ReplyValue] =
        for {
          subscription <- Subscription(
            m.chatId,
            sBotInfo.botId,
            calendarCron,
            SubscriptionType.Calendar,
            None
          )
          _ <- backgroundJobManager.scheduleSubscription(subscription)
        } yield Markdown(
          s"Calendar successfully scheduled: every day at 09:00 (server time). Refer to this subscription with the ID: `${subscription.id}`. Use `/unsubscribe ${subscription.id}` to remove it",
          ttl
        )
      val computation: String => F[ReplyValue] = (_: String) =>
        botShows(dbShow, sBotInfo).flatMap(shows =>
          Option
            .when(hasCalendar(shows))(scheduleCalendar)
            .getOrElse(Text(noCalendarMessage, ttl).pure[F])
        )
      CommandPatterns.handleCommandWithInput[F](
        commandSyntax = commandSyntax,
        msg = m,
        command = CommandKey.LoadCalendar.asString,
        sBotInfo = sBotInfo,
        computation = computation,
        ttl = ttl,
        defaultReply = "",
        allowEmptyString = true
      )
    }

    private[patterns] def loadCalendarReplyBundleCommand(sBotInfo: SBotInfo): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.LoadCalendar.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.LoadCalendar(sBotInfo),
          replyToMessage = true
        ),
        instruction = CommandInstructionData.Instructions(
          ita = loadCalendarCommandDescriptionIta,
          eng = loadCalendarCommandDescriptionEng
        )
      )

    def showCalendarCommandLogic[F[_]: MonadThrow](
        dbShow: DBShow[F],
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration]
    ): F[ReplyValue] =
      botShows(dbShow, sBotInfo).map(shows =>
        Text(Option.when(hasCalendar(shows))(calendarFileLink(sBotInfo)).getOrElse(noCalendarMessage), ttl)
      )

    private[patterns] def showCalendarReplyBundleCommand(sBotInfo: SBotInfo): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.ShowCalendar.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.ShowCalendar(sBotInfo),
          replyToMessage = true
        ),
        instruction = CommandInstructionData.Instructions(
          ita = showCalendarCommandDescriptionIta,
          eng = showCalendarCommandDescriptionEng
        )
      )
  }

  object SubscribeUnsubscribeCommand {

    private[patterns] val subscribeCommandDescriptionIta: String =
      s"'/subscribe': Documentazione: ${CommandDocs.chatCoreCommandDocLink("subscribe", "it")}"
    private[patterns] val subscribeCommandDescriptionEng: String =
      s"'/subscribe': Documentation: ${CommandDocs.chatCoreCommandDocLink("subscribe", "en")}"
    private[patterns] val unsubscribeCommandDescriptionIta: String =
      s"'/unsubscribe': Documentazione: ${CommandDocs.chatCoreCommandDocLink("unsubscribe", "it")}"
    private[patterns] val unsubscribeCommandDescriptionEng: String =
      s"'/unsubscribe': Documentation: ${CommandDocs.chatCoreCommandDocLink("unsubscribe", "en")}"
    private[patterns] val subscriptionsCommandDescriptionIta: String =
      s"'/subscriptions': Documentazione: ${CommandDocs.chatCoreCommandDocLink("subscriptions", "it")}"
    private[patterns] val subscriptionsCommandDescriptionEng: String =
      s"'/subscriptions': Documentation: ${CommandDocs.chatCoreCommandDocLink("subscriptions", "en")}"

    // A trigger subscription is accepted only if it would send something: its text selects a reply of the bot, as a live
    // message would, or, with no text (a random reply), the bot has at least one reply.
    private def validateTrigger[F[_]: Async](
        arguments: SubscribeArguments,
        dbMessageReplies: DBMessageReplies,
        m: Message
    ): F[Unit] =
      if arguments.subscriptionType != SubscriptionType.Trigger then Async[F].unit
      else
        arguments.target.fold(
          Async[F]
            .raiseWhen(dbMessageReplies.messageReplies.isEmpty)(SubscribeArguments.NoReplyToTrigger(trigger = None))
        )(text =>
          Async[F].realTimeInstant.flatMap(now =>
            Async[F].raiseUnless(
              ReplyBundleMessage
                .selectReplyBundle(
                  msg = m.copy(date = now.getEpochSecond, text = Some(text), caption = None, isForward = false),
                  messageRepliesData = dbMessageReplies.messageReplies,
                  ignoreMessagePrefix = None,
                  disableForward = false
                )
                .isDefined
            )(SubscribeArguments.NoReplyToTrigger(trigger = Some(text)))
          )
        )

    /** The same text of the next occurrence for the one-shot command and the confirmation step of the guided flow. */
    private[chatcore] def nextOccurrence(cron: CronExpr): String =
      cron.next(LocalDateTime.now).fold("`Unknown next occurrence`")(date => s"`${date.toString}`")

    /** The checks that both the one-shot command and the guided confirmation must pass before creating a subscription.
      */
    private[chatcore] def validateSubscriptionArguments(
        arguments: SubscribeArguments
    ): Either[Throwable, Unit] =
      for {
        _ <- Either.cond(
          arguments.subscriptionType == SubscriptionType.Show || arguments.subscriptionType == SubscriptionType.Trigger,
          (),
          SubscriptionType.UnknownSubscriptionType(arguments.subscriptionType.asString)
        )
        _ <- SubscriptionRules.validateTarget(arguments.target)
      } yield ()

    /** Creates a subscription and returns the standard success reply, or the failure that a guided flow can explain.
      * Nothing is written to the DB or scheduled when a check fails.
      */
    private[chatcore] def createSubscriptionReply[F[_]: Async](
        backgroundJobManager: BackgroundJobManager[F],
        dbMessageReplies: DBMessageReplies,
        m: Message,
        sBotInfo: SBotInfo,
        arguments: SubscribeArguments
    ): F[Either[Throwable, ReplyValue]] =
      (for {
        _            <- Async[F].fromEither(validateSubscriptionArguments(arguments))
        _            <- validateTrigger[F](arguments = arguments, dbMessageReplies = dbMessageReplies, m = m)
        subscription <- Subscription(
          m.chatId,
          sBotInfo.botId,
          arguments.cron,
          arguments.subscriptionType,
          arguments.target
        )
        _ <- Async[F].fromEither(SubscriptionRules.validateInterval(subscription.cron, LocalDateTime.now))
        _ <- backgroundJobManager.scheduleSubscription(subscription)
        next = nextOccurrence(subscription.cron)
      } yield Markdown(
        s"Subscription successfully scheduled. Next occurrence of subscription is $next. Refer to this subscription with the ID: `${subscription.id}`"
      ): ReplyValue).attempt

    def subscribeCommandLogic[F[_]: Async](
        backgroundJobManager: BackgroundJobManager[F],
        dbMessageReplies: DBMessageReplies,
        m: Message,
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration],
        commandSyntax: CommandSyntax = CommandSyntax.Slash
    ): F[ReplyValue] = {
      val computation: String => F[ReplyValue] = (input: String) =>
        for {
          arguments <- Async[F].fromEither(SubscribeArguments.parse(input))
          result    <- createSubscriptionReply(
            backgroundJobManager = backgroundJobManager,
            dbMessageReplies = dbMessageReplies,
            m = m,
            sBotInfo = sBotInfo,
            arguments = arguments
          )
          replyValue <- Async[F].fromEither(result)
        } yield replyValue
      CommandPatterns.handleCommandWithInput[F](
        commandSyntax = commandSyntax,
        msg = m,
        command = CommandKey.Subscribe.asString,
        sBotInfo = sBotInfo,
        computation = computation,
        ttl = ttl,
        defaultReply = "Input Required: insert a valid 〈cron time〉. Check the instructions"
      )
    }

    private[patterns] def subscribeReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.Subscribe.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.Subscribe(sBotInfo)
        ),
        instruction = CommandInstructionData.Instructions(
          ita = subscribeCommandDescriptionIta,
          eng = subscribeCommandDescriptionEng
        )
      )

    def unsubcribeCommandLogic[F[_]: Async](
        backgroundJobManager: BackgroundJobManager[F],
        m: Message,
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration],
        commandSyntax: CommandSyntax = CommandSyntax.Slash
    ): F[ReplyValue] = {
      val computation: String => F[ReplyValue] = (input: String) =>
        Async[F].fromEither(UnsubscribeArguments.parse(input)).flatMap {
          case UnsubscribeArguments.All =>
            backgroundJobManager
              .cancelSubscriptions(m.chatId)
              .as(Text(value = "All Subscriptions for current chat successfully cancelled", timeToLive = ttl))
          case UnsubscribeArguments.ById(subscriptionId) =>
            backgroundJobManager
              .cancelSubscription(m.chatId, subscriptionId)
              .as(Text(value = "Subscription successfully cancelled", timeToLive = ttl))
          case UnsubscribeArguments.ByType(subscriptionType) =>
            backgroundJobManager
              .cancelSubscriptionByType(m.chatId, subscriptionType)
              .as(
                Text(
                  value = s"All ${subscriptionType.asString} subscriptions for current chat successfully cancelled",
                  timeToLive = ttl
                )
              )
        }
      CommandPatterns.handleCommandWithInput[F](
        commandSyntax = commandSyntax,
        msg = m,
        command = CommandKey.Unsubscribe.asString,
        sBotInfo = sBotInfo,
        computation = computation,
        ttl = ttl,
        defaultReply =
          "Input Required: insert a valid 〈UUID〉, a subscription type (show, trigger, calendar) or no input to unsubscribe completely for this chat. Check the instructions",
        allowEmptyString = true
      )
    }

    private[patterns] def unsubscribeReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.Unsubscribe.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.Unsubscribe(sBotInfo)
        ),
        instruction = CommandInstructionData.Instructions(
          ita = unsubscribeCommandDescriptionIta,
          eng = unsubscribeCommandDescriptionEng
        )
      )

    def subscriptionsCommandLogic[F[_]: Async](
        dbSubscription: DBSubscription[F],
        backgroundJobManager: BackgroundJobManager[F],
        sBotInfo: SBotInfo,
        m: Message
    ): F[ReplyValue] = for {
      subscriptionsData <- dbSubscription.getSubscriptions(sBotInfo.botId, Some(m.chatId))
      subscriptions     <- subscriptionsData.traverse(sd => Async[F].fromEither(Subscription(sd)))
      now               <- Async[F].delay(LocalDateTime.now())
      scheduledIds = backgroundJobManager
        .getScheduledSubscriptions()
        .collect { case SubscriptionKey(subscriptionId, cid, _) if cid == m.chatId => subscriptionId }
      storedIds = subscriptions.map(_.id).toSet
    } yield com.benkio.chatcore.model.reply.Markdown(
      SubscriptionsTable.render(
        subscriptions = subscriptions,
        scheduled = scheduledIds.toSet,
        now = now,
        scheduledOnly = scheduledIds.filterNot(storedIds.contains)
      )
    )

    private[patterns] def subscriptionsReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.Subscriptions.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.Subscriptions(sBotInfo),
          replyToMessage = true
        ),
        instruction = CommandInstructionData.Instructions(
          ita = subscriptionsCommandDescriptionIta,
          eng = subscriptionsCommandDescriptionEng
        )
      )
  }
}
