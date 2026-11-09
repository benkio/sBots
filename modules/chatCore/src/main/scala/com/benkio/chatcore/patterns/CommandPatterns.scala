package com.benkio.chatcore.patterns

import cats.data.NonEmptyList
import cats.effect.std.Random
import cats.effect.Async
import cats.implicits.*
import cats.ApplicativeThrow
import cats.MonadThrow
import com.benkio.chatcore.messagefiltering.MessageMatches
import com.benkio.chatcore.messagefiltering.RandomSelection
import com.benkio.chatcore.model.media.Media
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.Markdown
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.show.addTimestamp
import com.benkio.chatcore.model.show.RandomQuery
import com.benkio.chatcore.model.show.Show
import com.benkio.chatcore.model.show.ShowCalendar
import com.benkio.chatcore.model.show.ShowQuery
import com.benkio.chatcore.model.show.ShowQueryKeyword
import com.benkio.chatcore.model.show.SimpleShowQuery
import com.benkio.chatcore.model.toEng
import com.benkio.chatcore.model.toIta
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.SubscribeArguments
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionRules
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.model.Timeout
import com.benkio.chatcore.model.Trigger
import com.benkio.chatcore.model.UnsubscribeArguments
import com.benkio.chatcore.repository.db.*
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.SubscriptionKey
import cron4s.lib.javatime.*
import cron4s.syntax.all.*
import log.effect.LogWriter
import org.http4s.Uri

import java.time.LocalDateTime
import scala.concurrent.duration.*

object CommandPatterns {
  private val commandsDocsBaseUrl                                = "https://github.com/benkio/sBots/blob/main/"
  private def commandDocLink(path: String, lang: String): String =
    s"$commandsDocsBaseUrl$path#$lang"
  private def chatCoreCommandDocLink(command: String, lang: String): String =
    commandDocLink(s"modules/chatCore/CommandsDocumentation/$command.md", lang)

  object CalendarCommands {
    // Every day at 09:00, server time
    val calendarCron: String = "0 0 9 * * ?"

    private[patterns] val loadCalendarCommandDescriptionIta: String =
      s"'/loadcalendar': Documentazione: ${chatCoreCommandDocLink("loadcalendar", "it")}"
    private[patterns] val loadCalendarCommandDescriptionEng: String =
      s"'/loadcalendar': Documentation: ${chatCoreCommandDocLink("loadcalendar", "en")}"
    private[patterns] val showCalendarCommandDescriptionIta: String =
      s"'/showcalendar': Documentazione: ${chatCoreCommandDocLink("showcalendar", "it")}"
    private[patterns] val showCalendarCommandDescriptionEng: String =
      s"'/showcalendar': Documentation: ${chatCoreCommandDocLink("showcalendar", "en")}"

    val noCalendarMessage: String = "no calendar"

    private def botShows[F[_]: MonadThrow](dbShow: DBShow[F], sBotInfo: SBotInfo): F[List[Show]] =
      dbShow.getShows(sBotInfo.botId).flatMap(_.traverse(Show[F](_)))

    // The calendar of a bot is empty when no show is a candidate for any day
    private def hasCalendar(shows: List[Show]): Boolean = ShowCalendar.orderedCandidates(shows).nonEmpty

    def calendarFileLink(sBotInfo: SBotInfo): String =
      s"${commandsDocsBaseUrl}modules/bots/${sBotInfo.botName.value}/${sBotInfo.botId.value}_calendar.ics"

    def loadCalendarCommandLogic[F[_]: Async](
        backgroundJobManager: BackgroundJobManager[F],
        dbShow: DBShow[F],
        m: Message,
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration]
    ): F[ReplyValue] = {
      val scheduleCalendar: F[ReplyValue] =
        for {
          subscription <- Subscription(
            m.chatId.value,
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
      handleCommandWithInput[F](
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

  object MediaByKindCommand {

    def mediaCommandByKindLogic[F[_]: Async](
        dbMedia: DBMedia[F],
        commandName: String,
        sBotInfo: SBotInfo
    )(using log: LogWriter[F]): F[ReplyValue] =
      for {
        _            <- log.debug(s"[MediaCommandByKind] Fetching DBMediaData for $commandName")
        dbMediaDatas <- dbMedia.getMediaByKind(kind = commandName, botId = sBotInfo.botId)
        _            <- log.debug("[MediaCommandByKind] Convert to Media")
        medias       <- dbMediaDatas.traverse(dbMediaData => Async[F].fromEither(Media(dbMediaData)))
        mediaFiles = medias.map(media => MediaFile.fromMimeType(media))
        mediaFile <- RandomSelection.select(replies = mediaFiles)
      } yield mediaFile

    def mediaCommandByKind(
        commandName: String,
        instruction: CommandInstructionData,
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandTrigger(commandName),
        reply = EffectfulReply(
          key = EffectfulKey.MediaByKind(commandName, sBotInfo)
        ),
        instruction = instruction
      )
  }

  object RandomDataCommand {

    case object RandomMediaNotFound
        extends Throwable(
          "[CommandPatterns] RandomDataCommand En error occurred when fetching random media. None Was returned"
        )

    private[patterns] val randomDataCommandIta: String =
      s"'/random': Documentazione: ${chatCoreCommandDocLink("random", "it")}"
    private[patterns] val randomDataCommandEng: String =
      s"'/random': Documentation: ${chatCoreCommandDocLink("random", "en")}"

    def randomCommandLogic[F[_]: Async: LogWriter](dbMedia: DBMedia[F], sBotInfo: SBotInfo): F[MediaFile] =
      for {
        _              <- LogWriter.debug(s"[RandomCommand] Fetching random media for ${sBotInfo.botId}")
        dbMediaDataOpt <- dbMedia.getRandomMedia(sBotInfo.botId)
        _              <- LogWriter.debug("[RandomCommand] Convert DBMediaData to Media")
        media          <- dbMediaDataOpt.fold(Async[F].raiseError(RandomMediaNotFound))(dbMediaData =>
          Async[F].fromEither(Media(dbMediaData))
        )
      } yield MediaFile.fromMimeType(media)

    def randomDataReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.Random.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.Random(sBotInfo)
        ),
        instruction = CommandInstructionData.Instructions(
          ita = randomDataCommandIta,
          eng = randomDataCommandEng
        )
      )
  }

  object SearchShowCommand {

    private[patterns] val searchShowCommandIta: String =
      s"'/searchshow': Documentazione: ${chatCoreCommandDocLink("searchshow", "it")}"
    private[patterns] val searchShowCommandEng: String =
      s"'/searchshow': Documentation: ${chatCoreCommandDocLink("searchshow", "en")}"

    def searchShowCommandLogic[F[_]: MonadThrow: LogWriter](
        msg: Message,
        dbLayer: DBLayer[F],
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration],
        showTransformation: String => NonEmptyList[Show] => F[ReplyValue]
    ): F[ReplyValue] =
      handleCommandWithInput[F](
        msg = msg,
        command = CommandKey.SearchShow.asString,
        sBotInfo = sBotInfo,
        ttl = ttl,
        computation = keywords =>
          SearchShowCommand
            .selectLinkByKeyword[F](
              keywords = keywords,
              dbShow = dbLayer.dbShow,
              sBotInfo = sBotInfo,
              ttl = ttl,
              showTransformation = showTransformation(keywords)
            ): F[ReplyValue],
        defaultReply = "Input non riconosciuto. Controlla le instruzioni per i dettagli",
        allowEmptyString = true
      )

    private[patterns] def searchShowReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.SearchShow.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.SearchShow(sBotInfo),
          replyToMessage = true
        ),
        instruction = CommandInstructionData.Instructions(
          ita = searchShowCommandIta,
          eng = searchShowCommandEng
        )
      )

    def defaultShowTransformation[F[_]: MonadThrow: Random](shows: NonEmptyList[Show]): F[ReplyValue] =
      Random[F].elementOf(shows.toList).map(show => Text(show.show): ReplyValue)

    def selectLinkByKeyword[F[_]: MonadThrow](
        keywords: String,
        dbShow: DBShow[F],
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration],
        showTransformation: NonEmptyList[Show] => F[ReplyValue]
    )(using log: LogWriter[F]): F[ReplyValue] = {
      val query: ShowQuery            = ShowQuery(keywords)
      val dbCall: F[List[DBShowData]] = query match {
        case RandomQuery         => dbShow.getRandomShow(sBotInfo.botId).map(_.toList)
        case q: SimpleShowQuery  => dbShow.getShowBySimpleShowQuery(q, sBotInfo.botId)
        case q: ShowQueryKeyword => dbShow.getShowByShowQuery(q, sBotInfo.botId)
      }

      for {
        _           <- log.info(s"Select random Show: ${sBotInfo.botId} - $keywords - $query")
        dbResults   <- dbCall
        showResults <- dbResults.traverse(Show.apply[F](_).map(_.addTimestamp(query)))
        output      <- NonEmptyList
          .fromList(showResults)
          .fold(
            MonadThrow[F].pure(
              Text(
                value = s"Nessuna puntata/show contenente '$keywords' è stata trovata",
                timeToLive = ttl
              )
            )
          )(showTransformation(_))
      } yield output
    }

    /** The show of the bot with the given id, otherwise the result of the keyword search (a random show when the target
      * is empty).
      */
    def selectShowByIdOrKeyword[F[_]: MonadThrow: Random](
        target: String,
        dbShow: DBShow[F],
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration]
    )(using log: LogWriter[F]): F[ReplyValue] = {
      val trimmedTarget = target.trim
      for {
        byId <- Option.when(trimmedTarget.nonEmpty)(trimmedTarget).flatTraverse(dbShow.getShowById)
        // a show id of another bot is not a match
        ownShow = byId.filter(_.bot_id == sBotInfo.botId.value)
        reply <- ownShow.fold(
          selectLinkByKeyword[F](
            keywords = trimmedTarget,
            dbShow = dbShow,
            sBotInfo = sBotInfo,
            ttl = ttl,
            showTransformation = defaultShowTransformation[F]
          ): F[ReplyValue]
        )(dbShowData => Show.apply[F](dbShowData).map(show => Text(show.show): ReplyValue))
      } yield reply
    }
  }

  object TriggerListCommand {

    private[patterns] val triggerListCommandDescriptionIta: String =
      s"'/triggerlist': Documentazione: ${chatCoreCommandDocLink("triggerlist", "it")}"
    private[patterns] val triggerListCommandDescriptionEng: String =
      s"'/triggerlist': Documentation: ${chatCoreCommandDocLink("triggerlist", "en")}"

    def triggerListLogic(triggerFileUri: Uri): Text =
      Text(s"Puoi trovare la lista dei trigger al seguente URL: $triggerFileUri")

    private[patterns] def triggerListReplyBundleCommand[F[_]](
        triggerFileUri: Uri
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.TriggerList.trigger,
        reply = TextReply(Set(triggerListLogic(triggerFileUri)), replyToMessage = true),
        instruction = CommandInstructionData.Instructions(
          ita = triggerListCommandDescriptionIta,
          eng = triggerListCommandDescriptionEng
        )
      )
  }

  object TriggerSearchCommand {

    private[patterns] val triggerSearchCommandDescriptionIta: String =
      s"'/triggersearch': Documentazione: ${chatCoreCommandDocLink("triggersearch", "it")}"
    private[patterns] val triggerSearchCommandDescriptionEng: String =
      s"'/triggersearch': Documentation: ${chatCoreCommandDocLink("triggersearch", "en")}"

    def searchTriggerLogic[F[_]: ApplicativeThrow](
        mdr: List[ReplyBundleMessage],
        m: Message,
        ignoreMessagePrefix: Option[String],
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration],
        replyBundleTransformation: ReplyBundleMessage => ReplyValue = replyBundleMessage =>
          Text(replyBundleMessage.prettyPrint())
    ): F[ReplyValue] = {

      handleCommandWithInput[F](
        msg = m,
        command = CommandKey.TriggerSearch.asString,
        sBotInfo = sBotInfo,
        ttl = ttl,
        computation = t => {
          val matches = mdr
            .mapFilter(MessageMatches.doesMatch(_, m, ignoreMessagePrefix))
            .sortBy(_._1)(using Trigger.orderingInstance.reverse)
            .headOption
          matches.fold(Text(value = s"No matching trigger for $t", timeToLive = ttl).pure[F]) { case (_, rbm) =>
            replyBundleTransformation(rbm).pure[F]
          }
        },
        defaultReply = """Input Required: Insert the test keyword to check if it's in some bot trigger"""
      )
    }

    // TODO: #782 Return the closest match on failure
    private[patterns] def triggerSearchReplyBundleCommand(
        sBotInfo: SBotInfo,
        replyBundleMessage: List[ReplyBundleMessage],
        ignoreMessagePrefix: Option[String]
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.TriggerSearch.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.TriggerSearch(sBotInfo, replyBundleMessage, ignoreMessagePrefix)
        ),
        instruction = CommandInstructionData.Instructions(
          ita = triggerSearchCommandDescriptionIta,
          eng = triggerSearchCommandDescriptionEng
        )
      )

  }

  object InstructionsCommand {
    private def instructionMessageIta(
        sBotInfo: SBotInfo,
        ignoreMessagePrefix: Option[String],
        commandDescriptions: List[String]
    ) = s"""
---- Instruzioni Per ${sBotInfo.botName} ----

Per segnalare problemi, scrivere a: https://t.me/Benkio

I comandi del bot sono:

${commandDescriptions.mkString(start = "- ", sep = "\n- ", end = "")}

${ignoreMessagePrefix
        .map(s =>
          s"Se si vuole disabilitare il bot per un particolare messaggio impedendo\nche interagisca, è possibile farlo iniziando il messaggio con il\ncarattere: `$s`\n\n$s Messaggio"
        )
        .getOrElse("")}
"""

    def instructionMessageEng(
        sBotInfo: SBotInfo,
        ignoreMessagePrefix: Option[String],
        commandDescriptions: List[String]
    ): String = s"""
---- Instructions for ${sBotInfo.botName} ----

to report issues, write to: https://t.me/Benkio

Bot commands are:

${commandDescriptions.mkString(start = "- ", sep = "\n- ", end = "")}

${ignoreMessagePrefix
        .map(s =>
          s"if you wish to disable the bot for a specific message, blocking its reply/interaction, you can do adding the following character as prefix\ncharacter: `$s`\n\n$s Message"
        )
        .getOrElse("")}
"""
    def instructionCommandLogic[F[_]: ApplicativeThrow](
        msg: Message,
        sBotInfo: SBotInfo,
        ignoreMessagePrefix: Option[String],
        commands: List[ReplyBundleCommand],
        ttl: Option[FiniteDuration]
    ): F[ReplyValue] = {
      val computation: String => F[ReplyValue] = (input: String) => {
        val itaMatches = List("it", "ita", "italian", "🇮🇹")
        val engMatches = List("", "en", "🇬🇧", "🇺🇸", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "eng", "english")
        val (commandDescriptionsIta, commandDescriptionsEng) =
          commands
            .unzip(using cmd => (cmd.instruction.toIta.toList, cmd.instruction.toEng.toList))
        val instructionsIta = Text(
          instructionMessageIta(
            sBotInfo = sBotInfo,
            ignoreMessagePrefix = ignoreMessagePrefix,
            commandDescriptions = commandDescriptionsIta.flatten
          )
        )
        val instructionsEng = Text(
          instructionMessageEng(
            sBotInfo = sBotInfo,
            ignoreMessagePrefix = ignoreMessagePrefix,
            commandDescriptions = commandDescriptionsEng.flatten
          )
        )
        input match {
          case v if itaMatches.contains(v) =>
            instructionsIta.pure[F]
          case v if engMatches.contains(v) =>
            instructionsEng.pure[F]
          case _ =>
            instructionsEng.pure[F]
        }
      }
      handleCommandWithInput[F](
        msg = msg,
        command = CommandKey.Instructions.asString,
        sBotInfo = sBotInfo,
        computation = computation,
        defaultReply = "",
        ttl = ttl,
        allowEmptyString = true
      )
    }

    def instructionsReplyBundleCommand(
        sBotInfo: SBotInfo,
        commands: List[ReplyBundleCommand],
        ignoreMessagePrefix: Option[String]
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.Instructions.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.Instructions(sBotInfo, ignoreMessagePrefix, commands)
        ),
        instruction = CommandInstructionData.NoInstructions
      )
  }

  object SubscribeUnsubscribeCommand {

    private[patterns] val subscribeCommandDescriptionIta: String =
      s"'/subscribe': Documentazione: ${chatCoreCommandDocLink("subscribe", "it")}"
    private[patterns] val subscribeCommandDescriptionEng: String =
      s"'/subscribe': Documentation: ${chatCoreCommandDocLink("subscribe", "en")}"
    private[patterns] val unsubscribeCommandDescriptionIta: String =
      s"'/unsubscribe': Documentazione: ${chatCoreCommandDocLink("unsubscribe", "it")}"
    private[patterns] val unsubscribeCommandDescriptionEng: String =
      s"'/unsubscribe': Documentation: ${chatCoreCommandDocLink("unsubscribe", "en")}"
    private[patterns] val subscriptionsCommandDescriptionIta: String =
      s"'/subscriptions': Documentazione: ${chatCoreCommandDocLink("subscriptions", "it")}"
    private[patterns] val subscriptionsCommandDescriptionEng: String =
      s"'/subscriptions': Documentation: ${chatCoreCommandDocLink("subscriptions", "en")}"

    def subscribeCommandLogic[F[_]: Async](
        backgroundJobManager: BackgroundJobManager[F],
        m: Message,
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration]
    ): F[ReplyValue] = {
      val computation: String => F[ReplyValue] = (input: String) =>
        for {
          arguments    <- Async[F].fromEither(SubscribeArguments.parse(input))
          subscription <- Subscription(
            m.chatId.value,
            sBotInfo.botId,
            arguments.cron,
            arguments.subscriptionType,
            arguments.target
          )
          _ <- Async[F].fromEither(SubscriptionRules.validateInterval(subscription.cron, LocalDateTime.now))
          nextOccurrence = subscription.cron
            .next(LocalDateTime.now)
            .fold("`Unknown next occurrence`")(date => s"`${date.toString}`")
          _ <- backgroundJobManager.scheduleSubscription(subscription)
        } yield Markdown(
          s"Subscription successfully scheduled. Next occurrence of subscription is $nextOccurrence. Refer to this subscription with the ID: `${subscription.id}`"
        )
      handleCommandWithInput[F](
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
        ttl: Option[FiniteDuration]
    ): F[ReplyValue] = {
      val computation: String => F[ReplyValue] = (input: String) =>
        Async[F].fromEither(UnsubscribeArguments.parse(input)).flatMap {
          case UnsubscribeArguments.All =>
            backgroundJobManager
              .cancelSubscriptions(m.chatId)
              .as(Text(value = "All Subscriptions for current chat successfully cancelled", timeToLive = ttl))
          case UnsubscribeArguments.ById(subscriptionId) =>
            backgroundJobManager
              .cancelSubscription(subscriptionId)
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
      handleCommandWithInput[F](
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
      subscriptionsData <- dbSubscription.getSubscriptions(sBotInfo.botId, Some(m.chatId.value))
      subscriptions     <- subscriptionsData.traverse(sd => Async[F].fromEither(Subscription(sd)))
      memSubscriptions     = backgroundJobManager.getScheduledSubscriptions()
      memChatSubscriptions = memSubscriptions.filter { case SubscriptionKey(_, cid, _) => cid.value == m.chatId.value }
    } yield Markdown(
      s"There are ${subscriptions.length} stored subscriptions for this chat:\n" ++ subscriptions
        .map(_.show)
        .mkString("\n") ++
        s"\nThere are ${memChatSubscriptions.size}/${memSubscriptions.size} scheduled subscriptions for this chat:\n" ++
        memChatSubscriptions.map(_.show).mkString("\n")
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

  object StatisticsCommands {

    private[patterns] val topTwentyTriggersCommandDescriptionIta: String =
      s"'/toptwenty': Documentazione: ${chatCoreCommandDocLink("toptwenty", "it")}"
    private[patterns] val topTwentyTriggersCommandDescriptionEng: String =
      s"'/toptwenty': Documentation: ${chatCoreCommandDocLink("toptwenty", "en")}"

    def topTwentyCommandLogic[F[_]: MonadThrow](sBotInfo: SBotInfo, dbMedia: DBMedia[F]): F[List[MediaFile]] =
      for {
        dbMedias <- dbMedia.getMediaByMediaCount(botId = sBotInfo.botId.some)
        medias   <- MonadThrow[F].fromEither(dbMedias.traverse(Media.apply))
      } yield medias.map(media => MediaFile.fromMimeType(media))

    private[patterns] def topTwentyReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.TopTwenty.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.TopTwenty(sBotInfo)
        ),
        instruction = CommandInstructionData.Instructions(
          ita = topTwentyTriggersCommandDescriptionIta,
          eng = topTwentyTriggersCommandDescriptionEng
        )
      )

  }

  object SetTimeoutCommand {

    private[patterns] val setTimeoutCommandDescriptionIta: String =
      s"'/settimeout': Documentazione: ${chatCoreCommandDocLink("settimeout", "it")}"
    private[patterns] val setTimeoutCommandDescriptionEng: String =
      s"'/settimeout': Documentation: ${chatCoreCommandDocLink("settimeout", "en")}"

    def setTimeoutLogic[F[_]: MonadThrow: LogWriter](
        msg: Message,
        dbTimeout: DBTimeout[F],
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration]
    ): F[ReplyValue] = {
      val computation: String => F[ReplyValue] = (input: String) => {
        if input.isEmpty
        then dbTimeout.removeTimeout(chatId = msg.chatId.value, botId = sBotInfo.botId) *>
          Text(value = "Timeout removed", timeToLive = ttl).pure[F]
        else
          Timeout(msg.chatId, sBotInfo.botId, input)
            .fold(
              error =>
                LogWriter.info(
                  s"[ERROR] While parsing the timeout input: $error"
                ) *>
                  Text(
                    value =
                      s"Timeout set failed: wrong input format for $input, the input must be in the form '/settimeout 00:00:00'",
                    timeToLive = ttl
                  )
                    .pure[F],
              timeout =>
                dbTimeout.setTimeout(
                  DBTimeoutData(timeout)
                ) *>
                  Text(
                    value = s"Timeout set successfully to ${Timeout.formatTimeout(timeout)}",
                    timeToLive = ttl
                  ).pure[F]
            )
      }
      handleCommandWithInput[F](
        msg = msg,
        command = CommandKey.SetTimeout.asString,
        sBotInfo = sBotInfo,
        computation = computation,
        allowEmptyString = true,
        ttl = ttl,
        defaultReply = """Input Required: the input must be in the form '/settimeout 00:00:00' or empty"""
      )
    }

    private[patterns] def setTimeoutReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.SetTimeout.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.SetTimeout(sBotInfo),
          replyToMessage = true
        ),
        instruction = CommandInstructionData.Instructions(
          ita = setTimeoutCommandDescriptionIta,
          eng = setTimeoutCommandDescriptionEng
        )
      )
  }

  object GetTimeoutCommand {

    private[patterns] val getTimeoutCommandDescriptionIta: String =
      s"'/gettimeout': Documentazione: ${chatCoreCommandDocLink("gettimeout", "it")}"
    private[patterns] val getTimeoutCommandDescriptionEng: String =
      s"'/gettimeout': Documentation: ${chatCoreCommandDocLink("gettimeout", "en")}"

    def getTimeoutLogic[F[_]: MonadThrow](
        msg: Message,
        dbTimeout: DBTimeout[F],
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration]
    ): F[ReplyValue] = {
      dbTimeout
        .getOrDefault(
          chatId = msg.chatId.value,
          botId = sBotInfo.botId
        )
        .flatMap((dbTimeoutData: DBTimeoutData) => MonadThrow[F].fromEither(Timeout(dbTimeoutData)))
        .map((timeout: Timeout) =>
          Text(
            value = s"The Timeout is ${Timeout.formatTimeout(timeout)}",
            timeToLive = ttl
          )
        )
        .handleError(e =>
          Text(
            value =
              s"""An error occurred when fetching the timeout. Reset the timeout with `setTimeout` and contact the bot maintainer: ${e
                  .getMessage()}""",
            timeToLive = ttl
          )
        )
    }

    private[patterns] def getTimeoutReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.GetTimeout.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.GetTimeout(sBotInfo),
          replyToMessage = true
        ),
        instruction = CommandInstructionData.Instructions(
          ita = getTimeoutCommandDescriptionIta,
          eng = getTimeoutCommandDescriptionEng
        )
      )
  }

  def handleCommandWithInput[F[_]: ApplicativeThrow](
      msg: Message,
      command: String,
      sBotInfo: SBotInfo,
      computation: String => F[ReplyValue],
      defaultReply: String,
      allowEmptyString: Boolean = false,
      ttl: Option[FiniteDuration]
  ): F[ReplyValue] =
    msg.text
      .filter(t => {
        val (inputCommand, rest) = t.trim.span(_ != ' ')
        val restCheck            = allowEmptyString || (rest.trim.nonEmpty && !allowEmptyString)
        val commandCheck         = inputCommand == s"/$command" || inputCommand == s"/$command@${sBotInfo.botName}"
        commandCheck && restCheck
      })
      .map(t => computation(t.dropWhile(_ != ' ').drop(1).trim))
      .getOrElse(Text(value = defaultReply, timeToLive = ttl).pure[F])
      .handleErrorWith(e =>
        Text(
          value = s"""An error occurred processing the command: $command
                     | message text: ${msg.text.orElse(msg.caption).getOrElse("")}
                     | bot: ${sBotInfo.botName}
                     | error: ${e.getMessage}""".stripMargin,
          timeToLive = ttl
        ).pure[F]
      )
}
