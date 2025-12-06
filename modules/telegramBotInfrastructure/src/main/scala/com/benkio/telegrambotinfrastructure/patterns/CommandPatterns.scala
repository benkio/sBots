package com.benkio.telegrambotinfrastructure.patterns

import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.reply.EffectfulKey
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.EffectfulReply
import cats.effect.Async
import cats.implicits.*
import cats.ApplicativeThrow
import cats.MonadThrow
import com.benkio.telegrambotinfrastructure.given
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.model.media.Media
import com.benkio.telegrambotinfrastructure.model.reply.toText
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.model.reply.TextReply
import com.benkio.telegrambotinfrastructure.model.show.RandomQuery
import com.benkio.telegrambotinfrastructure.model.show.Show
import com.benkio.telegrambotinfrastructure.model.show.ShowQuery
import com.benkio.telegrambotinfrastructure.model.show.ShowQueryKeyword
import com.benkio.telegrambotinfrastructure.model.toEng
import com.benkio.telegrambotinfrastructure.model.toIta
import com.benkio.telegrambotinfrastructure.model.ChatId
import com.benkio.telegrambotinfrastructure.model.CommandInstructionData
import com.benkio.telegrambotinfrastructure.model.CommandTrigger
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotName
import com.benkio.telegrambotinfrastructure.model.Subscription
import com.benkio.telegrambotinfrastructure.model.SubscriptionId
import com.benkio.telegrambotinfrastructure.model.Timeout
import com.benkio.telegrambotinfrastructure.model.Trigger
import com.benkio.telegrambotinfrastructure.repository.db.*
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.SubscriptionKey
import cron4s.lib.javatime.*
import cron4s.syntax.all.*
import log.effect.LogWriter
import org.http4s.Uri
import telegramium.bots.Message

import java.time.LocalDateTime
import java.util.UUID
import scala.util.Try

object CommandPatterns {

  object MediaByKindCommand {

    def mediaCommandByKindLogic[F[_]: Async](
        dbMedia: DBMedia[F],
        commandName: String,
        kind: Option[String],
        sBotInfo: SBotInfo
    )(using log: LogWriter[F]): F[List[MediaFile]] =
      for {
        _            <- log.debug(s"[MediaCommandByKind] Fetching DBMediaData for $kind")
        dbMediaDatas <- dbMedia.getMediaByKind(kind = kind.getOrElse(commandName), botId = sBotInfo.botId)
        _            <- log.debug("[MediaCommandByKind] Convert to Media")
        medias       <- dbMediaDatas.traverse(dbMediaData => Async[F].fromEither(Media(dbMediaData)))
      } yield medias.map(media => MediaFile.fromMimeType(media))

    def mediaCommandByKind(
        commandName: String,
      instruction: CommandInstructionData,
      sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandTrigger(commandName),
        reply = EffectfulReply(
          key = EffectfulKey.Custom(commandName,sBotInfo)
        )// MediaReply(
        //   mediaFiles = mediaCommandByKindLogic(dbMedia = dbMedia, commandName = commandName, kind = kind, botId = botId)
        // )
        ,
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
        dbMediaDataOpt <- dbMedia.getRandomMedia(botId)
        _              <- LogWriter.debug("[RandomCommand] Convert DBMediaData to Media")
        media          <- dbMediaDataOpt.fold(Async[F].raiseError(RandomMediaNotFound))(dbMediaData =>
          Async[F].fromEither(Media(dbMediaData))
        )
      } yield MediaFile.fromMimeType(media)

    def randomDataReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandTrigger("random"),
        reply = EffectfulReply(
          key = EffectfulKey.Random(sBotInfo)
        )
        ,
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
  In caso di input non riconosciuto, verrà considerato come titolo.
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
  If the input is not recognized it will be considered as a title.
  Fields can be concatenated. Example: 'title=Cocktail+Micidiale&description=steve+vai&minduration=300'"""

    def searchShowCommandLogic[F[_]: Async: LogWriter](
      msg: Message,
      dbLayer: DBLayer[F],
      sBotInfo: SBotInfo,
  ): F[List[Text]] =
      handleCommandWithInput[F](
        msg = msg,
        command = "searchshow",
        sBotInfo = sBotInfo,
        computation = keywords =>
          SearchShowCommand
            .selectLinkByKeyword[F](
              keywords = keywords,
              dbShow = dbLayer.dbShow,
              sBotInfo = sBotInfo
            )
            .map(List(_)),
        defaultReply = "Input non riconosciuto. Controlla le instruzioni per i dettagli",
        allowEmptyString = true
      )

    private[patterns] def searchShowReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandTrigger("searchshow"),
        reply = EffectfulReply(
          key = EffectfulKey.SearchShow(sBotInfo),
          replyToMessage = true
        )
        ,
        instruction = CommandInstructionData.Instructions(
          ita = searchShowCommandIta,
          eng = searchShowCommandEng
        )
      )

    def selectLinkByKeyword[F[_]: Async](
        keywords: String,
        dbShow: DBShow[F],
        sBotInfo: SBotInfo
    )(using log: LogWriter[F]): F[String] = {
      val query: ShowQuery            = ShowQuery(keywords)
      val dbCall: F[List[DBShowData]] = query match {
        case RandomQuery         => dbShow.getRandomShow(sBotInfo.botId).map(_.toList)
        case q: ShowQueryKeyword => dbShow.getShowByShowQuery(q, sBotInfo.botId)
      }

      for {
        _       <- log.info(s"Select random Show: ${sBotInfo.botId} - $keywords - $query")
        results <- dbCall
        result  <-
          results.headOption
            .traverse(Show.apply[F](_).map(_.show))
            .map(_.getOrElse(s"Nessuna puntata/show contenente '$keywords' è stata trovata"))
      } yield result
    }
  }

  object TriggerListCommand {

    private val triggerListCommandDescriptionIta: String =
      "'/triggerlist': Restituisce un link ad un file contenente tutti i trigger a cui il bot risponderà automaticamente. Alcuni di questi sono in formato Regex"
    private val triggerListCommandDescriptionEng: String =
      "'/triggerlist': Return a link to a file containing all the triggers used by the bot. Bot will reply automatically to these ones. Some of them are Regex"

    def triggerListLogic(triggerFileUri: Uri): String =
      s"Puoi trovare la lista dei trigger al seguente URL: $triggerFileUri"

    private[patterns] def triggerListReplyBundleCommand[F[_]](
      triggerFileUri: Uri,
      sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandTrigger("triggerlist"),
        reply = TextReply.fromList(triggerListLogic(triggerFileUri))(true),
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

    private[patterns] def searchTriggerLogic(
        mdr: List[ReplyBundleMessage],
        m: Message,
        ignoreMessagePrefix: Option[String]
    ): String => List[String] = { t =>
      val matches = mdr
        .mapFilter(MessageMatches.doesMatch(_, m, ignoreMessagePrefix))
        .sortBy(_._1)(using Trigger.orderingInstance.reverse)
      if matches.isEmpty
      then List(s"No matching trigger for $t")
      else matches.map { case (_, rbm) => rbm.prettyPrint() }
    }

    // TODO: #782 Return the closest match on failure
    private[patterns] def triggerSearchReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandTrigger("triggersearch"),
        reply = EffectfulReply(
          key = EffectfulKey.TriggerSearch(sBotInfo)
        )// TextReplyM[F](m =>
        //   handleCommandWithInput[F](
        //     msg = m,
        //     command = "triggersearch",
        //     sBotInfo = sBotInfo,
        //     computation = searchTriggerLogic(mdr = mdr, m = m, ignoreMessagePrefix = ignoreMessagePrefix),
        //     defaultReply = """Input Required: Insert the test keyword to check if it's in some bot trigger"""
        //   )
        // )
        ,
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
    def instructionCommandLogic(
        sBotInfo: SBotInfo,
        ignoreMessagePrefix: Option[String],
        commands: List[ReplyBundleCommand]
    ): String => List[String] = input => {
      val itaMatches = List("it", "ita", "italian", "🇮🇹")
      val engMatches = List("", "en", "🇬🇧", "🇺🇸", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "eng", "english")
      val (commandDescriptionsIta, commandDescriptionsEng) =
        commands
          .unzip(using cmd => (cmd.instruction.toIta.toList, cmd.instruction.toEng.toList))
      val instructionsIta = List(
        instructionMessageIta(
          sBotInfo = sBotInfo,
          ignoreMessagePrefix = ignoreMessagePrefix,
          commandDescriptions = commandDescriptionsIta.flatten
        )
      )
      val instructionsEng = List(
        instructionMessageEng(
          sBotInfo = sBotInfo,
          ignoreMessagePrefix = ignoreMessagePrefix,
          commandDescriptions = commandDescriptionsEng.flatten
        )
      )
      input match {
        case v if itaMatches.contains(v) =>
          instructionsIta
        case v if engMatches.contains(v) =>
          instructionsEng
        case _ =>
          instructionsEng
      }
    }

    private[telegrambotinfrastructure] def instructionsReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandTrigger("instructions"),
        reply = EffectfulReply(
          key = EffectfulKey.Instructions(sBotInfo)
        )// TextReplyM[F](m =>
        //   handleCommandWithInput[F](
        //     msg = m,
        //     command = "instructions",
        //     sBotInfo = sBotInfo,
        //     computation = instructionCommandLogic(
        //       sBotInfo = sBotInfo,
        //       ignoreMessagePrefix = ignoreMessagePrefix,
        //       commands = commands
        //     ),
        //     defaultReply = "",
        //     allowEmptyString = true
        //   )
        // )
        ,
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
        cronInput: String,
        backgroundJobManager: BackgroundJobManager[F],
        m: Message,
        sBotInfo: SBotInfo
    ) =
      for {
        subscription <- Subscription(m.chat.id, botId, cronInput)
        nextOccurrence = subscription.cron
          .next(LocalDateTime.now)
          .fold("`Unknown next occurrence`")(date => s"`${date.toString}`")
        _ <- backgroundJobManager.scheduleSubscription(subscription)
      } yield List(
        s"Subscription successfully scheduled. Next occurrence of subscription is $nextOccurrence. Refer to this subscription with the ID: ${subscription.id}"
      )

    private[patterns] def subscribeReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandTrigger("subscribe"),
        reply = EffectfulReply(
          key = EffectfulKey.Subscribe(sBotInfo)
        )// TextReplyM[F](
        //   m =>
        //     handleCommandWithInput[F](
        //       msg = m,
        //       command = "subscribe",
        //       sBotInfo = sBotInfo,
        //       computation = subscribeCommandLogic(_, backgroundJobManager, m, botId),
        //       defaultReply = "Input Required: insert a valid 〈cron time〉. Check the instructions"
        //     ),
        //   true
        // )
        ,
        instruction = CommandInstructionData.Instructions(
          ita = subscribeCommandDescriptionIta,
          eng = subscribeCommandDescriptionEng
        )
      )

    def unsubcribeCommandLogic[F[_]: Async](
        subscriptionIdInput: String,
        backgroundJobManager: BackgroundJobManager[F],
        m: Message
    ): F[String] = {
      if subscriptionIdInput.isEmpty then for {
        _ <- backgroundJobManager.cancelSubscriptions(ChatId(m.chat.id))
      } yield "All Subscriptions for current chat successfully cancelled"
      else
        for {
          subscriptionId <- Async[F].fromTry(Try(UUID.fromString(subscriptionIdInput))).map(SubscriptionId(_))
          _              <- backgroundJobManager.cancelSubscription(subscriptionId)
        } yield "Subscription successfully cancelled"
    }

    private[patterns] def unsubscribeReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandTrigger("unsubscribe"),
        reply = EffectfulReply(
          key = EffectfulKey.Unsubscribe(sBotInfo)
        )// TextReplyM[F](
        //   m =>
        //     handleCommandWithInput[F](
        //       msg = m,
        //       command = "unsubscribe",
        //       sBotInfo = sBotInfo,
        //       computation = unsubcribeCommandLogic(_, backgroundJobManager, m).map(List(_)),
        //       defaultReply =
        //         "Input Required: insert a valid 〈UUID〉or no input to unsubscribe completely for this chat. Check the instructions",
        //       allowEmptyString = true
        //     ),
        //   true
        // )
        ,
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
    ): F[String] = for {
      subscriptionsData <- dbSubscription.getSubscriptions(botId, Some(m.chat.id))
      subscriptions     <- subscriptionsData.traverse(sd => Async[F].fromEither(Subscription(sd)))
      memSubscriptions     = backgroundJobManager.getScheduledSubscriptions()
      memChatSubscriptions = memSubscriptions.filter { case SubscriptionKey(_, cid) => cid.value == m.chat.id }
    } yield s"There are ${subscriptions.length} stored subscriptions for this chat:\n" ++ subscriptions
      .map(_.show)
      .mkString("\n") ++
      s"\nThere are ${memChatSubscriptions.size}/${memSubscriptions.size} scheduled subscriptions for this chat:\n" ++
      memChatSubscriptions.map(_.show).mkString("\n")

    private[patterns] def subscriptionsReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandTrigger("subscriptions"),
        reply = EffectfulReply(
          key = EffectfulKey.Subscriptions(sBotInfo),
          replyToMessage = true
        )// TextReplyM[F](
        //   m =>
        //     subscriptionsCommandLogic(
        //       dbSubscription = dbSubscription,
        //       backgroundJobManager = backgroundJobManager,
        //       botId = botId,
        //       m = m
        //     ).map(List(_).toText),
        //   true
        // )
        ,
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

    def topTwentyCommandLogic[F[_]: MonadThrow](sBotInfo: SBotInfo, dbMedia: DBMedia[F]): F[String] =
      for {
        dbMedias <- dbMedia.getMediaByMediaCount(botId = botId.some)
        medias   <- MonadThrow[F].fromEither(dbMedias.traverse(Media.apply))
      } yield Media.mediaListToHTML(medias)

    private[patterns] def topTwentyReplyBundleCommand(
        sBotInfo: SBotInfo
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandTrigger("toptwenty"),
        reply = EffectfulReply(
          key = EffectfulKey.TopTwenty(sBotInfo)
        )// TextReplyM[F](
        //   _ => topTwentyCommandLogic(botId, dbMedia).map(List(_).map(Text(_, textType = Text.TextType.Html))),
        //   true
        // )
        ,
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

    def timeoutLogic[F[_]: MonadThrow](
        input: String,
        msg: Message,
        dbTimeout: DBTimeout[F],
        sBotInfo: SBotInfo,
        log: LogWriter[F]
    ): F[String] =
      if input.isEmpty then dbTimeout.removeTimeout(chatId = msg.chat.id, botId = botId) *> "Timeout removed".pure[F]
      else
        Timeout(ChatId(msg.chat.id), botId, input)
          .fold(
            error =>
              log.info(
                s"[ERROR] While parsing the timeout input: $error"
              ) *> s"Timeout set failed: wrong input format for $input, the input must be in the form '/timeout 00:00:00'"
                .pure[F],
            timeout =>
              dbTimeout.setTimeout(
                DBTimeoutData(timeout)
              ) *> s"Timeout set successfully to ${Timeout.formatTimeout(timeout)}".pure[F]
          )

    private[patterns] def timeoutReplyBundleCommand(
      botId : SBotId
    ): ReplyBundleCommand =
      ReplyBundleCommand(
        trigger = CommandTrigger("timeout"),
        reply = EffectfulReply(
          key = EffectfulKey.Timeout(sBotInfo),
          replyToMessage = true
        )// TextReplyM[F](
        //   msg =>
        //     handleCommandWithInput[F](
        //       msg = msg,
        //       command = "timeout",
        //       sBotInfo = sBotInfo,
        //       computation = timeoutLogic(_, msg, dbTimeout, botId, log).map(List(_)),
        //       defaultReply = """Input Required: the input must be in the form '/timeout 00:00:00' or empty"""
        //     ),
        //   true
        // )
        ,
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
      computation: String => F[List[String]],
      defaultReply: String,
      allowEmptyString: Boolean = false
  ): F[List[Text]] =
    msg.text
      .filter(t => {
        val (inputCommand, rest) = t.trim.span(_ != ' ')
        val restCheck            = allowEmptyString || (rest.trim.nonEmpty && !allowEmptyString)
        val commandCheck         = inputCommand == s"/$command" || inputCommand == s"/$command@${sBotInfo.botName}"
        commandCheck && restCheck
      })
      .map(t => computation(t.dropWhile(_ != ' ').drop(1).trim))
      .getOrElse(List(defaultReply).pure[F])
      .handleErrorWith(e =>
        List(
          s"""An error occurred processing the command: $command
             | message text: ${msg.text.orElse(msg.caption).getOrElse("")}
             | bot: ${sBotInfo.botName}
             | error: ${e.getMessage}""".stripMargin
        ).pure[F]
      )
      .map(_.toText)
}
