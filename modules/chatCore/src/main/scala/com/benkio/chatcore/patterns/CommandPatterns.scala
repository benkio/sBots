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
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.SubscribeArguments
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionRules
import com.benkio.chatcore.model.SubscriptionType
import com.benkio.chatcore.model.SubscriptionsTable
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
      handleCommandWithInput[F](
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
        showTransformation: String => NonEmptyList[Show] => F[ReplyValue],
        commandSyntax: CommandSyntax = CommandSyntax.Slash
    ): F[ReplyValue] =
      handleCommandWithInput[F](
        commandSyntax = commandSyntax,
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
          Text(replyBundleMessage.prettyPrint()),
        commandSyntax: CommandSyntax = CommandSyntax.Slash
    ): F[ReplyValue] = {

      handleCommandWithInput[F](
        commandSyntax = commandSyntax,
        msg = m,
        command = CommandKey.TriggerSearch.asString,
        sBotInfo = sBotInfo,
        ttl = ttl,
        computation = t => {
          // match the input only, not the whole command message (it would match words of the command itself)
          val inputMessage = m.copy(text = Some(t), caption = None)
          val matches      = mdr
            .mapFilter(MessageMatches.doesMatch(_, inputMessage, ignoreMessagePrefix))
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

    def instructionsUriDefault(sBotInfo: SBotInfo): org.http4s.Uri =
      org.http4s.Uri.unsafeFromString(
        s"https://github.com/benkio/sBots/blob/main/modules/bots/${sBotInfo.botName}/${sBotInfo.botId}_instructions.md"
      )

    def instructionsLinkReply(instructionsUri: org.http4s.Uri): Text =
      Text(s"Puoi trovare le istruzioni del bot al seguente URL: $instructionsUri")

    private def formatCommandDescription(raw: String): String =
      raw.replaceFirst("^'?", "").replaceFirst("':\\s*", " - ")

    def describableCommands(
        sBotInfo: SBotInfo,
        triggerListUri: org.http4s.Uri,
        ignoreMessagePrefix: Option[String],
        messageRepliesData: List[ReplyBundleMessage],
        commandRepliesData: List[ReplyBundleCommand]
    ): List[ReplyBundleCommand] =
      commandRepliesData ++ CommandPatternsGroup.TriggerGroup.group(
        triggerFileUri = triggerListUri,
        sBotInfo = sBotInfo,
        messageRepliesData = messageRepliesData,
        ignoreMessagePrefix = ignoreMessagePrefix
      )

    def instructionsFileContent(
        sBotInfo: SBotInfo,
        ignoreMessagePrefix: Option[String],
        commands: List[ReplyBundleCommand]
    ): String = {
      val (commandDescriptionsIta, commandDescriptionsEng) =
        commands.unzip(using cmd => (cmd.instruction.toIta.toList, cmd.instruction.toEng.toList))
      val itaCommands = commandDescriptionsIta.flatten.map(d => s"- ${formatCommandDescription(d)}").mkString("\n")
      val engCommands = commandDescriptionsEng.flatten.map(d => s"- ${formatCommandDescription(d)}").mkString("\n")
      val itaIgnore   = ignoreMessagePrefix
        .map(s =>
          s"\nSe vuoi disabilitare il bot per un messaggio specifico, puoi iniziare il messaggio con il carattere: `$s`\n\n`$s Messaggio`\n"
        )
        .getOrElse("")
      val engIgnore = ignoreMessagePrefix
        .map(s =>
          s"\nIf you want to disable the bot for a specific message, you can start the message with the character: `$s`\n\n`$s Message`\n"
        )
        .getOrElse("")
      s"""# Istruzioni per ${sBotInfo.botName}
         |
         |Per segnalare problemi, scrivere a: https://t.me/Benkio
         |
         |## Comandi
         |
         |$itaCommands
         |$itaIgnore
         |# Instructions for ${sBotInfo.botName}
         |
         |To report issues, write to: https://t.me/Benkio
         |
         |## Commands
         |
         |$engCommands
         |$engIgnore""".stripMargin
    }

    def instructionCommandLogic[F[_]: ApplicativeThrow](
        msg: Message,
        sBotInfo: SBotInfo,
        instructionsUri: org.http4s.Uri,
        ttl: Option[FiniteDuration],
        commandSyntax: CommandSyntax = CommandSyntax.Slash
    ): F[ReplyValue] =
      handleCommandWithInput[F](
        commandSyntax = commandSyntax,
        msg = msg,
        command = CommandKey.Instructions.asString,
        sBotInfo = sBotInfo,
        computation = _ => instructionsLinkReply(instructionsUri).pure[F],
        defaultReply = "",
        ttl = ttl,
        allowEmptyString = true
      )

    def instructionsReplyBundleCommand(
        sBotInfo: SBotInfo,
        instructionsUri: org.http4s.Uri
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.Instructions.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.Instructions(sBotInfo, instructionsUri)
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
          arguments    <- Async[F].fromEither(SubscribeArguments.parse(input))
          _            <- validateTrigger[F](arguments = arguments, dbMessageReplies = dbMessageReplies, m = m)
          subscription <- Subscription(
            m.chatId,
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
      handleCommandWithInput[F](
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
    } yield Markdown(
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
        ttl: Option[FiniteDuration],
        commandSyntax: CommandSyntax = CommandSyntax.Slash
    ): F[ReplyValue] = {
      val computation: String => F[ReplyValue] = (input: String) => {
        if input.isEmpty
        then dbTimeout.removeTimeout(chatId = msg.chatId, botId = sBotInfo.botId) *>
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
        commandSyntax = commandSyntax,
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
          chatId = msg.chatId,
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
      ttl: Option[FiniteDuration],
      commandSyntax: CommandSyntax = CommandSyntax.Slash
  ): F[ReplyValue] =
    msg.text
      .filter(t => {
        val (inputCommand, rest) = t.trim.span(_ != ' ')
        val restCheck            = allowEmptyString || (rest.trim.nonEmpty && !allowEmptyString)
        val commandCheck         = commandSyntax.isCommandWord(inputCommand, command, sBotInfo.botName)
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
