package com.benkio.chatcore.patterns

import cats.effect.Async
import cats.implicits.*
import cats.ApplicativeThrow
import cats.MonadThrow
import com.benkio.chatcore.messagefiltering.MessageMatches
import com.benkio.chatcore.messagefiltering.RandomSelection
import com.benkio.chatcore.model.media.Media
import com.benkio.chatcore.model.reply.EffectfulKey
import com.benkio.chatcore.model.reply.EffectfulReply
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.Text
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.show.addTimestamp
import com.benkio.chatcore.model.show.RandomQuery
import com.benkio.chatcore.model.show.Show
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
import com.benkio.chatcore.model.Subscription
import com.benkio.chatcore.model.SubscriptionId
import com.benkio.chatcore.model.Timeout
import com.benkio.chatcore.model.Trigger
import com.benkio.chatcore.repository.db.*
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.SubscriptionKey
import cron4s.lib.javatime.*
import cron4s.syntax.all.*
import log.effect.LogWriter
import org.http4s.Uri

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.duration.*
import scala.util.Try

object CommandPatterns {

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
      """'/random': Invia un contenuto casuale (testo/foto/audio/video) del personaggio del bot."""
    private[patterns] val randomDataCommandEng: String =
      """'/random': Send a random content (text/photo/audio/video) about the bot character."""

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
      """'/searchshow [query]': Cerca uno show e invia un link.
        |Senza input: show casuale.
        |Filtri supportati: title=, description=, caption=, minduration=, maxduration=, mindate=YYYYMMDD, maxdate=YYYYMMDD.
        |Esempio: /searchshow title=paul+gilbert&minduration=300""".stripMargin
    private[patterns] val searchShowCommandEng: String =
      """'/searchshow [query]': Search shows and send a link.
        |No input: random show.
        |Supported filters: title=, description=, caption=, minduration=, maxduration=, mindate=YYYYMMDD, maxdate=YYYYMMDD.
        |Example: /searchshow title=paul+gilbert&minduration=300""".stripMargin

    def searchShowCommandLogic[F[_]: Async: LogWriter](
        msg: Message,
        dbLayer: DBLayer[F],
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration]
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
              ttl = ttl
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

    def selectLinkByKeyword[F[_]: Async](
        keywords: String,
        dbShow: DBShow[F],
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration]
    )(using log: LogWriter[F]): F[ReplyValue] = {
      val query: ShowQuery            = ShowQuery(keywords)
      val dbCall: F[List[DBShowData]] = query match {
        case RandomQuery         => dbShow.getRandomShow(sBotInfo.botId).map(_.toList)
        case q: SimpleShowQuery  => dbShow.getShowBySimpleShowQuery(q, sBotInfo.botId)
        case q: ShowQueryKeyword => dbShow.getShowByShowQuery(q, sBotInfo.botId)
      }

      for {
        _ <- log.info(s"Select random Show: ${sBotInfo.botId} - $keywords - $query")
        // TODO 814: eventually show all and allow the user to choose
        results     <- dbCall
        maybeResult <- results.headOption.traverse(Show.apply[F](_))
        maybeResultWithTimestamp = maybeResult.map(_.addTimestamp(query))
        result                   = maybeResultWithTimestamp
          .map(_.show)
          .fold(
            Text(
              value = s"Nessuna puntata/show contenente '$keywords' è stata trovata",
              timeToLive = ttl
            )
          )(Text(_))
      } yield result
    }
  }

  object TriggerListCommand {

    private[patterns] val triggerListCommandDescriptionIta: String =
      "'/triggerlist': Mostra il link al file con tutti i trigger automatici (inclusi quelli regex)."
    private[patterns] val triggerListCommandDescriptionEng: String =
      "'/triggerlist': Show the link to the file with all automatic triggers (including regex ones)."

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
      "'/triggersearch <testo>': Cerca se una parola o frase corrisponde a un trigger."
    private[patterns] val triggerSearchCommandDescriptionEng: String =
      "'/triggersearch <text>': Check whether a word or phrase matches a trigger."

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
      """'/subscribe <cron time>': Iscrive questa chat all'invio casuale di una puntata.
        |Formato cron (6 campi): secondi minuti ore giorno mese giorno-settimana.
        |Tra giorno e giorno-settimana, uno deve essere '?'.
        |Esempi: /subscribe 0 * * ? * * | /subscribe 0 30 9 * * ? | /subscribe 0 0 18 ? * MON
        |Riferimento sintassi cron4s: https://github.com/alonsodomin/cron4s""".stripMargin
    private[patterns] val subscribeCommandDescriptionEng: String =
      """'/subscribe <cron time>': Subscribe this chat to random show messages at a chosen interval.
        |Cron format (6 fields): seconds minutes hours day month day-of-week.
        |Between day and day-of-week, one must be '?'.
        |Examples: /subscribe 0 * * ? * * | /subscribe 0 30 9 * * ? | /subscribe 0 0 18 ? * MON
        |cron4s syntax reference: https://github.com/alonsodomin/cron4s""".stripMargin
    private[patterns] val unsubscribeCommandDescriptionIta: String =
      "'/unsubscribe [uuid]': Disiscrive la chat corrente dall'invio di puntate. Con UUID rimuove solo quella iscrizione, senza input rimuove tutte le iscrizioni della chat."
    private[patterns] val unsubscribeCommandDescriptionEng: String =
      "'/unsubscribe [uuid]': Unsubscribe the current chat from random shows. With a UUID it removes only that subscription, with no input it removes all subscriptions for the chat."
    private[patterns] val subscriptionsCommandDescriptionIta: String =
      "'/subscriptions': Mostra tutte le iscrizioni correnti della chat (salvate e schedulate)."
    private[patterns] val subscriptionsCommandDescriptionEng: String =
      "'/subscriptions': Show all current subscriptions for this chat (stored and scheduled)."

    def subscribeCommandLogic[F[_]: Async](
        backgroundJobManager: BackgroundJobManager[F],
        m: Message,
        sBotInfo: SBotInfo,
        ttl: Option[FiniteDuration]
    ): F[ReplyValue] = {
      val computation: String => F[ReplyValue] = (cronInput: String) =>
        for {
          subscription <- Subscription(m.chatId.value, sBotInfo.botId, cronInput)
          nextOccurrence = subscription.cron
            .next(LocalDateTime.now)
            .fold("`Unknown next occurrence`")(date => s"`${date.toString}`")
          _ <- backgroundJobManager.scheduleSubscription(subscription)
        } yield Text(
          s"Subscription successfully scheduled. Next occurrence of subscription is $nextOccurrence. Refer to this subscription with the ID: ${subscription.id}"
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
      val computation: String => F[ReplyValue] = (subscriptionIdInput: String) => {
        if subscriptionIdInput.isEmpty then for {
          _ <- backgroundJobManager.cancelSubscriptions(m.chatId)
        } yield Text(value = "All Subscriptions for current chat successfully cancelled", timeToLive = ttl)
        else
          for {
            subscriptionId <- Async[F].fromTry(Try(UUID.fromString(subscriptionIdInput))).map(SubscriptionId(_))
            _              <- backgroundJobManager.cancelSubscription(subscriptionId)
          } yield Text(value = "Subscription successfully cancelled", timeToLive = ttl)
      }
      handleCommandWithInput[F](
        msg = m,
        command = CommandKey.Unsubscribe.asString,
        sBotInfo = sBotInfo,
        computation = computation,
        ttl = ttl,
        defaultReply =
          "Input Required: insert a valid 〈UUID〉or no input to unsubscribe completely for this chat. Check the instructions",
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
      memChatSubscriptions = memSubscriptions.filter { case SubscriptionKey(_, cid) => cid.value == m.chatId.value }
    } yield Text(
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
      "'/toptwenty': Mostra i file piu inviati, ordinati per frequenza."
    private[patterns] val topTwentyTriggersCommandDescriptionEng: String =
      "'/toptwenty': Show the most sent files, ordered by frequency."

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
      "'/settimeout [HH:MM:SS]': Imposta il tempo minimo tra le risposte del bot in questa chat. Senza input il timeout viene rimosso."
    private[patterns] val setTimeoutCommandDescriptionEng: String =
      "'/settimeout [HH:MM:SS]': Set the minimum time between bot replies in this chat. With no input, timeout is removed."

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
      "'/gettimeout': Mostra il timeout attivo per la chat corrente."
    private[patterns] val getTimeoutCommandDescriptionEng: String =
      "'/gettimeout': Show the active timeout for the current chat."

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
