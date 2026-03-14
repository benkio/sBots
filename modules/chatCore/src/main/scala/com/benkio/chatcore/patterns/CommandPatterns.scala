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

    private val randomDataCommandIta: String =
      """'/random': Restituisce un dato(audio/video/testo/foto) casuale riguardante il personaggio del bot"""
    private val randomDataCommandEng: String =
      """'/random': Returns a random data (photo/video/audio/text) about the bot character"""

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

    private val searchShowCommandIta: String =
      """'/searchshow 《testo》': Restituisce un link di uno show/video riguardante il personaggio del bot e contenente il testo specificato.
Input come query string:
  - No input: restituisce uno show random
  - 'title=keyword: restituisce uno show contenente la keyword nel titolo. Il campo può essere specificato più volte, si cercherà uno show contenente tutte le keywords. Esempio: 'title=Paul+Gilbert&title=dissacrazione'
  - 'description=keyword: restituisce uno show contenente la keyword nella descrizione. Il campo può essere specificato più volte, si cercherà uno show contenente tutte le keywords.  Esempio: 'description=Cris+Impellitteri&description=ramarro'
  - 'caption=keyword: restituisce uno show contenente la keyword nella caption automatica. Il campo può essere specificato più volte, si cercherà uno show contenente tutte le keywords.  Esempio: 'caption=Cris+Impellitteri&caption=ramarro'
  - 'minduration=X': restituisce uno show di durata minima pari a X secondi. Esempio: 'minduration=300'
  - 'maxduration=X': restituisce uno show di durata massima pari a X secondi. Esempio: 'maxduration=1000'
  - 'mindate=YYYYMMDD': restituisce uno show più recente della data specificata. Esempio: 'mindate=20200101'
  - 'maxdate=YYYYMMDD': restituisce uno show più vecchio della data specificata. Esempio: 'mandate=20220101'
  In caso di input non riconosciuto, verrà considerato come titolo, o descrizione, o caption.
  I campi possono essere concatenati. Esempio: 'title=Cocktail+Micidiale&description=steve+vai&minduration=300'"""
    private val searchShowCommandEng: String =
      """'/searchshow 《text》': Return a link of a show/video about the specific bot's character and containing the specified keyword.
Input as query string:
  - No input: returns a random show
  - 'title=keyword: returns a show with the keyword in the title. The field can be specified multiple times, the show will contain all the keywords. Example: 'title=Paul+Gilbert&title=dissacrazione'
  - 'description=keyword: returns a show with the keyword in the description. The field can be specified multiple times, the show will contain all the keywords.  Example: 'description=Cris+Impellitteri&description=ramarro'
  - 'caption=keyword: returns a show with the keyword in the caption. The field can be specified multiple times, the show will contain all the keywords.  Example: 'caption=Cris+Impellitteri&caption=ramarro'
  - 'minduration=X': returns a show with minimal duration of X seconds.  Example: 'minduration=300'
  - 'maxduration=X': returns a show with maximal duration of X seconds.  Example: 'maxduration=1000'
  - 'mindate=YYYYMMDD': returns a show newer than the specified date.  Example: 'mindate=20200101'
  - 'maxdate=YYYYMMDD': returns a show older than the specified date.  Example: 'mandate=20220101'
  If the input is not recognized it will be considered as a title, or description, or caption.
  Fields can be concatenated. Example: 'title=Cocktail+Micidiale&description=steve+vai&minduration=300'"""

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
        _       <- log.info(s"Select random Show: ${sBotInfo.botId} - $keywords - $query")
        results <- dbCall
        result  <-
          results.headOption
            .traverse(Show.apply[F](_).map(_.show))
            .map(
              _.fold(
                Text(
                  value = s"Nessuna puntata/show contenente '$keywords' è stata trovata",
                  timeToLive = ttl
                )
              )(Text(_))
            )
      } yield result
    }
  }

  object TriggerListCommand {

    private val triggerListCommandDescriptionIta: String =
      "'/triggerlist': Restituisce un link ad un file contenente tutti i trigger a cui il bot risponderà automaticamente. Alcuni di questi sono in formato Regex"
    private val triggerListCommandDescriptionEng: String =
      "'/triggerlist': Return a link to a file containing all the triggers used by the bot. Bot will reply automatically to these ones. Some of them are Regex"

    def triggerListLogic(triggerFileUri: Uri): Text =
      Text(s"Puoi trovare la lista dei trigger al seguente URL: $triggerFileUri")

    private[patterns] def triggerListReplyBundleCommand[F[_]](
        triggerFileUri: Uri
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.TriggerList.trigger,
        reply = TextReply(List(triggerListLogic(triggerFileUri)), replyToMessage = true),
        instruction = CommandInstructionData.Instructions(
          ita = triggerListCommandDescriptionIta,
          eng = triggerListCommandDescriptionEng
        )
      )
  }

  object TriggerSearchCommand {

    private val triggerSearchCommandDescriptionIta: String =
      "'/triggersearch 《testo》': Consente di cercare se una parola o frase fa parte di un trigger"
    private val triggerSearchCommandDescriptionEng: String =
      "'/triggersearch 《text》': Allow you to search if a specific word or phrase is part of a trigger"

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

    private val subscribeCommandDescriptionIta: String =
      "'/subscribe 《cron time》': Iscrizione all'invio randomico di una puntata alla frequenza specificato nella chat corrente. Per il formato dell'input utilizzare questo codice come riferimento: https://scastie.scala-lang.org/ir5llpyPS5SmzU0zd46uLA oppure questo sito: https://www.freeformatter.com/cron-expression-generator-quartz.html#cronexpressionexamples Attenzione, la libreria usata richiede anche i secondi come riportato nella documentazione: https://www.alonsodomin.me/cron4s/userguide/index.html"
    private val subscribeCommandDescriptionEng: String =
      "'/subscribe 《cron time》': Subscribe to a random show at the specified frequency in the current chat. For the input format check the following code snippet: https://scastie.scala-lang.org/ir5llpyPS5SmzU0zd46uLA oppure questo sito: https://www.freeformatter.com/cron-expression-generator-quartz.html#cronexpressionexamples You can find the docs here: https://www.alonsodomin.me/cron4s/userguide/index.html"
    private val unsubscribeCommandDescriptionIta: String =
      "'/unsubscribe': Disiscrizione della chat corrente dall'invio di puntate. Disiscriviti da una sola iscrizione inviando l'UUID relativo o da tutte le sottoscrizioni per la chat corrente se non viene inviato nessun input"
    private val unsubscribeCommandDescriptionEng: String =
      "'/unsubscribe': Unsubscribe the current chat from random shows. With a UUID as input, the specific subscription will be deleted. With no input, all the subscriptions for the current chat will be deleted"
    private val subscriptionsCommandDescriptionIta: String =
      "'/subscriptions': Restituisce la lista delle iscrizioni correnti per la chat corrente"
    private val subscriptionsCommandDescriptionEng: String =
      "'/subscriptions': Return the amout of subscriptions for the current chat"

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

    private val topTwentyTriggersCommandDescriptionIta: String =
      "'/toptwenty': Restituisce una lista di file e il loro numero totale in invii"
    private val topTwentyTriggersCommandDescriptionEng: String =
      "'/toptwenty': Return a list of files and theirs send frequency"

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

  object TimeoutCommand {

    private val timeoutCommandDescriptionIta: String =
      "'/timeout 《intervallo》': Consente di impostare un limite di tempo tra una risposta e l'altra nella specifica chat. Formato dell'input: 00:00:00. Senza input il timeout verrà rimosso"
    private val timeoutCommandDescriptionEng: String =
      "'/timeout 《time》': Allow you to set a timeout between bot's replies in the specific chat. input time format: 00:00:00. Without input the timeout will be removed"

    def timeoutLogic[F[_]: MonadThrow: LogWriter](
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
                      s"Timeout set failed: wrong input format for $input, the input must be in the form '/timeout 00:00:00'",
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
        command = CommandKey.Timeout.asString,
        sBotInfo = sBotInfo,
        computation = computation,
        allowEmptyString = true,
        ttl = ttl,
        defaultReply = """Input Required: the input must be in the form '/timeout 00:00:00' or empty"""
      )
    }

    private[patterns] def timeoutReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandKey.Timeout.trigger,
        reply = EffectfulReply(
          key = EffectfulKey.Timeout(sBotInfo),
          replyToMessage = true
        ),
        instruction = CommandInstructionData.Instructions(
          ita = timeoutCommandDescriptionIta,
          eng = timeoutCommandDescriptionEng
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
