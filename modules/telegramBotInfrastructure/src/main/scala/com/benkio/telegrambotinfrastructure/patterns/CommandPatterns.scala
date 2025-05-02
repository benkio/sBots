package com.benkio.telegrambotinfrastructure.patterns

import cats.effect.Async
import cats.implicits.*
import cats.Applicative
import cats.ApplicativeThrow
import cats.MonadThrow
import com.benkio.telegrambotinfrastructure.given
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.model.media.Media
import com.benkio.telegrambotinfrastructure.model.reply.toText
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.MediaReply
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.model.reply.TextReply
import com.benkio.telegrambotinfrastructure.model.reply.TextReplyM
import com.benkio.telegrambotinfrastructure.model.show.RandomQuery
import com.benkio.telegrambotinfrastructure.model.show.Show
import com.benkio.telegrambotinfrastructure.model.show.ShowQuery
import com.benkio.telegrambotinfrastructure.model.show.ShowQueryKeyword
import com.benkio.telegrambotinfrastructure.model.ChatId
import com.benkio.telegrambotinfrastructure.model.CommandTrigger
import com.benkio.telegrambotinfrastructure.model.Subscription
import com.benkio.telegrambotinfrastructure.model.SubscriptionId
import com.benkio.telegrambotinfrastructure.model.Timeout
import com.benkio.telegrambotinfrastructure.model.Trigger
import com.benkio.telegrambotinfrastructure.resources.db.*
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

    def mediaCommandByKind[F[_]: Async](
        dbMedia: DBMedia[F],
        botName: String,
        commandName: String,
        kind: Option[String]
    )(using log: LogWriter[F]): ReplyBundleCommand[F] =
      ReplyBundleCommand[F](
        trigger = CommandTrigger(commandName),
        reply = MediaReply[F](
          mediaFiles = for
            dbMediaDatas <- dbMedia.getMediaByKind(kind = kind.getOrElse(commandName))
            medias       <- dbMediaDatas.traverse(dbMediaData => Async[F].fromEither(Media(dbMediaData)))
          yield medias.map(media => MediaFile.fromMimeType(media))
        )
      )
  }

  object RandomDataCommand {

    val randomDataCommandIta: String =
      """'/random': Restituisce un dato(audio/video/testo/foto) casuale riguardante il personaggio del bot"""
    val randomDataCommandEng: String =
      """'/random': Returns a data (photo/video/audio/text) random about the bot character"""

    def randomDataReplyBundleCommand[F[_]: Async](
        dbMedia: DBMedia[F],
        botPrefix: String
    )(using log: LogWriter[F]): ReplyBundleCommand[F] =
      ReplyBundleCommand[F](
        trigger = CommandTrigger("random"),
        reply = MediaReply[F](
          mediaFiles = for
            dbMediaData <- dbMedia.getRandomMedia(botPrefix)
            media       <- Async[F].fromEither(Media(dbMediaData))
          yield List(MediaFile.fromMimeType(media))
        )
      )
  }

  object SearchShowCommand {

    val searchShowCommandIta: String =
      """'/searchshow 《testo》': Restituisce un link di uno show/video riguardante il personaggio del bot e contenente il testo specificato.
Input come query string:
  - No input: restituisce uno show random
  - 'title=keyword: restituisce uno show contenente la keyword nel titolo. Il campo può essere specificato più volte, si cercherà uno show contenente tutte le keywords. Esempio: 'title=Paul+Gilbert&title=dissacrazione'
  - 'description=keyword: restituisce uno show contenente la keyword nella descrizione. Il campo può essere specificato più volte, si cercherà uno show contenente tutte le keywords.  Esempio: 'description=Cris+Impellitteri&description=ramarro'
  - 'minduration=X': restituisce uno show di durata minima pari a X secondi. Esempio: 'minduration=300'
  - 'maxduration=X': restituisce uno show di durata massima pari a X secondi. Esempio: 'maxduration=1000'
  - 'mindate=YYYYMMDD': restituisce uno show più recente della data specificata. Esempio: 'mindate=20200101'
  - 'maxdate=YYYYMMDD': restituisce uno show più vecchio della data specificata. Esempio: 'mandate=20220101'
  In caso di input non riconosciuto, verrà considerato come titolo.
  I campi possono essere concatenati. Esempio: 'title=Cocktail+Micidiale&description=steve+vai&minduration=300'"""
    val searchShowCommandEng: String =
      """'/searchshow 《text》': Return a link of a show/video about the specific bot's character and containing the specified keyword.
Input as query string:
  - No input: returns a random show
  - 'title=keyword: returns a show with the keyword in the title. The field can be specified multiple times, the show will contain all the keywords. Example: 'title=Paul+Gilbert&title=dissacrazione'
  - 'description=keyword: returns a show with the keyword in the description. The field can be specified multiple times, the show will contain all the keywords.  Example: 'description=Cris+Impellitteri&description=ramarro'
  - 'minduration=X': returns a show with minimal duration of X seconds.  Example: 'minduration=300'
  - 'maxduration=X': returns a show with maximal duration of X seconds.  Example: 'maxduration=1000'
  - 'mindate=YYYYMMDD': returns a show newer than the specified date.  Example: 'mindate=20200101'
  - 'maxdate=YYYYMMDD': returns a show older than the specified date.  Example: 'mandate=20220101'
  If the input is not recognized it will be considered as a title.
  Fields can be concatenated. Example: 'title=Cocktail+Micidiale&description=steve+vai&minduration=300'"""

    def searchShowReplyBundleCommand[F[_]: Async](
        dbShow: DBShow[F],
        botName: String
    )(using log: LogWriter[F]): ReplyBundleCommand[F] =
      ReplyBundleCommand[F](
        trigger = CommandTrigger("searchshow"),
        reply = TextReplyM[F](
          m =>
            handleCommandWithInput[F](
              m,
              "searchshow",
              botName,
              keywords =>
                SearchShowCommand
                  .selectLinkByKeyword[F](
                    keywords,
                    dbShow,
                    botName
                  )
                  .map(List(_)),
              "Input non riconosciuto. Controlla le instruzioni per i dettagli",
              allowEmptyString = true
            ),
          true
        )
      )

    def selectLinkByKeyword[F[_]: Async](
        keywords: String,
        dbShow: DBShow[F],
        botName: String
    )(using log: LogWriter[F]): F[String] = {
      val query: ShowQuery = ShowQuery(keywords)
      val dbCall: F[List[DBShowData]] = query match {
        case RandomQuery         => dbShow.getRandomShow(botName).map(_.toList)
        case q: ShowQueryKeyword => dbShow.getShowByShowQuery(q, botName)
      }

      for {
        _       <- log.info(s"Select random Show: $botName - $keywords - $query")
        results <- dbCall
        result <-
          results.headOption
            .traverse(Show.apply[F](_).map(_.show))
            .map(_.getOrElse(s"Nessuna puntata/show contenente '$keywords' è stata trovata"))
      } yield result
    }
  }

  object TriggerListCommand {

    val triggerListCommandDescriptionIta: String =
      "'/triggerlist': Restituisce un link ad un file contenente tutti i trigger a cui il bot risponderà automaticamente. Alcuni di questi sono in formato Regex"
    val triggerListCommandDescriptionEng: String =
      "'/triggerlist': Return a link to a file containing all the triggers used by the bot. Bot will reply automatically to these ones. Some of them are Regex"

    def triggerListReplyBundleCommand[F[_]: Applicative](triggerFileUri: Uri): ReplyBundleCommand[F] =
      ReplyBundleCommand(
        trigger = CommandTrigger("triggerlist"),
        reply = TextReply.fromList(s"Puoi trovare la lista dei trigger al seguente URL: $triggerFileUri")(true)
      )
  }

  object TriggerSearchCommand {

    val triggerSearchCommandDescriptionIta: String =
      "'/triggersearch 《testo》': Consente di cercare se una parola o frase fa parte di un trigger"
    val triggerSearchCommandDescriptionEng: String =
      "'/triggersearch 《text》': Allow you to search if a specific word or phrase is part of a trigger"

    private[patterns] def searchTriggerLogic[F[_]: ApplicativeThrow](
        mdr: List[ReplyBundleMessage[F]],
        m: Message,
        ignoreMessagePrefix: Option[String]
    ): String => F[List[String]] =
      t =>
        val matches = mdr
          .mapFilter(MessageMatches.doesMatch(_, m, ignoreMessagePrefix))
          .sortBy(_._1)(Trigger.orderingInstance.reverse)
        if matches.isEmpty
        then List(s"No matching trigger for $t").pure[F]
        else matches.traverse { case (_, rbm) => rbm.prettyPrint() }

    // TODO: Return the closest match on failure
    def triggerSearchReplyBundleCommand[F[_]: ApplicativeThrow](
        botName: String,
        ignoreMessagePrefix: Option[String],
        mdr: List[ReplyBundleMessage[F]]
    ): ReplyBundleCommand[F] =
      ReplyBundleCommand(
        trigger = CommandTrigger("triggersearch"),
        reply = TextReplyM[F](m =>
          handleCommandWithInput[F](
            m,
            "triggersearch",
            botName,
            searchTriggerLogic(mdr, m, ignoreMessagePrefix),
            """Input Required: Insert the test keyword to check if it's in some bot trigger"""
          )
        )
      )

  }

  object InstructionsCommand {

    private def instructionMessageIta(
        botName: String,
        ignoreMessagePrefix: Option[String],
        commandDescriptions: List[String]
    ) = s"""
---- Instruzioni Per $botName ----

Per segnalare problemi, scrivere a: https://t.me/Benkio

I comandi del bot sono:

${commandDescriptions.mkString("- ", "\n- ", "")}

${ignoreMessagePrefix
        .map(s =>
          s"Se si vuole disabilitare il bot per un particolare messaggio impedendo\nche interagisca, è possibile farlo iniziando il messaggio con il\ncarattere: `$s`\n\n$s Messaggio"
        )
        .getOrElse("")}
"""

    def instructionMessageEng(
        botName: String,
        ignoreMessagePrefix: Option[String],
        commandDescriptions: List[String]
    ): String = s"""
---- Instructions for $botName ----

to report issues, write to: https://t.me/Benkio

Bot commands are:

${commandDescriptions.mkString("- ", "\n- ", "")}

${ignoreMessagePrefix
        .map(s =>
          s"if you wish to disable the bot for a specific message, blocking its reply/interaction, you can do adding the following character as prefix\ncharacter: `$s`\n\n$s Message"
        )
        .getOrElse("")}
"""

    def instructionsReplyBundleCommand[F[_]: Applicative](
        botName: String,
        ignoreMessagePrefix: Option[String],
        commandDescriptionsIta: List[String],
        commandDescriptionsEng: List[String]
    ): ReplyBundleCommand[F] =
      ReplyBundleCommand(
        trigger = CommandTrigger("instructions"),
        reply = TextReply.fromList[F](
          instructionMessageIta(
            botName = botName,
            ignoreMessagePrefix = ignoreMessagePrefix,
            commandDescriptions = commandDescriptionsIta
          ),
          instructionMessageEng(
            botName = botName,
            ignoreMessagePrefix = ignoreMessagePrefix,
            commandDescriptions = commandDescriptionsEng
          )
        )(false)
      )
  }

  object SubscribeUnsubscribeCommand {

    val subscribeCommandDescriptionIta: String =
      "'/subscribe 《cron time》': Iscrizione all'invio randomico di una puntata alla frequenza specificato nella chat corrente. Per il formato dell'input utilizzare questo codice come riferimento: https://scastie.scala-lang.org/ir5llpyPS5SmzU0zd46uLA oppure questo sito: https://www.freeformatter.com/cron-expression-generator-quartz.html#cronexpressionexamples Attenzione, la libreria usata richiede anche i secondi come riportato nella documentazione: https://www.alonsodomin.me/cron4s/userguide/index.html"
    val subscribeCommandDescriptionEng: String =
      "'/subscribe 《cron time》': Subscribe to a random show at the specified frequency in the current chat. For the input format check the following code snippet: https://scastie.scala-lang.org/ir5llpyPS5SmzU0zd46uLA oppure questo sito: https://www.freeformatter.com/cron-expression-generator-quartz.html#cronexpressionexamples You can find the docs here: https://www.alonsodomin.me/cron4s/userguide/index.html"
    val unsubscribeCommandDescriptionIta: String =
      "'/unsubscribe': Disiscrizione della chat corrente dall'invio di puntate. Disiscriviti da una sola iscrizione inviando l'UUID relativo o da tutte le sottoscrizioni per la chat corrente se non viene inviato nessun input"
    val unsubscribeCommandDescriptionEng: String =
      "'/unsubscribe': Unsubscribe the current chat from random shows. With a UUID as input, the specific subscription will be deleted. With no input, all the subscriptions for the current chat will be deleted"
    val subscriptionsCommandDescriptionIta: String =
      "'/subscriptions': Restituisce la lista delle iscrizioni correnti per la chat corrente"
    val subscriptionsCommandDescriptionEng: String =
      "'/subscriptions': Return the amout of subscriptions for the current chat"

    def subscribeCommandLogic[F[_]: Async](
        cronInput: String,
        backgroundJobManager: BackgroundJobManager[F],
        m: Message,
        botName: String
    ) =
      for
        subscription <- Subscription(m.chat.id, botName, cronInput)
        nextOccurrence = subscription.cron
          .next(LocalDateTime.now)
          .fold("`Unknown next occurrence`")(date => s"`${date.toString}`")
        _ <- backgroundJobManager.scheduleSubscription(subscription)
      yield List(
        s"Subscription successfully scheduled. Next occurrence of subscription is $nextOccurrence. Refer to this subscription with the ID: ${subscription.id}"
      )

    def subscribeReplyBundleCommand[F[_]: Async](
        backgroundJobManager: BackgroundJobManager[F],
        botName: String
    ): ReplyBundleCommand[F] =
      ReplyBundleCommand[F](
        trigger = CommandTrigger("subscribe"),
        reply = TextReplyM[F](
          m =>
            handleCommandWithInput[F](
              m,
              "subscribe",
              botName,
              subscribeCommandLogic(_, backgroundJobManager, m, botName),
              "Input Required: insert a valid 〈cron time〉. Check the instructions"
            ),
          true
        )
      )

    def unsubcribeCommandLogic[F[_]: Async](
        subscriptionIdInput: String,
        backgroundJobManager: BackgroundJobManager[F],
        m: Message
    ): F[String] = {
      if subscriptionIdInput.isEmpty then
        for {
          _ <- backgroundJobManager.cancelSubscriptions(ChatId(m.chat.id))
        } yield "All Subscriptions for current chat successfully cancelled"
      else
        for {
          subscriptionId <- Async[F].fromTry(Try(UUID.fromString(subscriptionIdInput))).map(SubscriptionId(_))
          _              <- backgroundJobManager.cancelSubscription(subscriptionId)
        } yield "Subscription successfully cancelled"
    }

    def unsubscribeReplyBundleCommand[F[_]: Async](
        backgroundJobManager: BackgroundJobManager[F],
        botName: String
    ): ReplyBundleCommand[F] =
      ReplyBundleCommand[F](
        trigger = CommandTrigger("unsubscribe"),
        reply = TextReplyM[F](
          m =>
            handleCommandWithInput[F](
              msg = m,
              command = "unsubscribe",
              botName = botName,
              computation = unsubcribeCommandLogic(_, backgroundJobManager, m).map(List(_)),
              defaultReply =
                "Input Required: insert a valid 〈UUID〉or no input to unsubscribe completely for this chat. Check the instructions",
              allowEmptyString = true
            ),
          true
        )
      )

    def subscriptionsCommandLogic[F[_]: Async](
        dbSubscription: DBSubscription[F],
        backgroundJobManager: BackgroundJobManager[F],
        botName: String,
        m: Message
    ): F[String] = for {
      subscriptionsData <- dbSubscription.getSubscriptions(botName, Some(m.chat.id))
      subscriptions     <- subscriptionsData.traverse(sd => Async[F].fromEither(Subscription(sd)))
      memSubscriptions     = backgroundJobManager.getScheduledSubscriptions()
      memChatSubscriptions = memSubscriptions.filter { case SubscriptionKey(_, cid) => cid.value == m.chat.id }
    } yield s"There are ${subscriptions.length} stored subscriptions for this chat:\n" ++ subscriptions
      .map(_.show)
      .mkString("\n") ++
      s"\nThere are ${memChatSubscriptions.size}/${memSubscriptions.size} scheduled subscriptions for this chat:\n" ++
      memChatSubscriptions.map(_.show).mkString("\n")

    def subscriptionsReplyBundleCommand[F[_]: Async](
        dbSubscription: DBSubscription[F],
        backgroundJobManager: BackgroundJobManager[F],
        botName: String
    ): ReplyBundleCommand[F] =
      ReplyBundleCommand[F](
        trigger = CommandTrigger("subscriptions"),
        reply = TextReplyM[F](
          m => subscriptionsCommandLogic(dbSubscription, backgroundJobManager, botName, m).map(List(_).toText),
          true
        )
      )
  }

  object StatisticsCommands {

    val topTwentyTriggersCommandDescriptionIta: String =
      "'/topTwentyTriggers': Restituisce una lista di file e il loro numero totale in invii"
    val topTwentyTriggersCommandDescriptionEng: String =
      "'/topTwentyTriggers': Return a list of files and theirs send frequency"

    def topTwentyReplyBundleCommand[F[_]: MonadThrow](
        botPrefix: String,
        dbMedia: DBMedia[F]
    ): ReplyBundleCommand[F] =
      ReplyBundleCommand(
        trigger = CommandTrigger("toptwenty"),
        reply = TextReplyM[F](
          _ =>
            for {
              dbMedias <- dbMedia.getMediaByMediaCount(mediaNamePrefix = botPrefix.some)
              medias   <- MonadThrow[F].fromEither(dbMedias.traverse(Media.apply))
            } yield List(Media.mediaListToString(medias)).toText,
          true
        )
      )

  }

  object TimeoutCommand {

    val timeoutCommandDescriptionIta: String =
      "'/timeout 《intervallo》': Consente di impostare un limite di tempo tra una risposta e l'altra nella specifica chat. Formato dell'input: 00:00:00. Senza input il timeout verrà rimosso"
    val timeoutCommandDescriptionEng: String =
      "'/timeout 《time》': Allow you to set a timeout between bot's replies in the specific chat. input time format: 00:00:00. Without input the timeout will be removed"

    def timeoutLogic[F[_]: MonadThrow](
        input: String,
        msg: Message,
        dbTimeout: DBTimeout[F],
        botName: String,
        log: LogWriter[F]
    ): F[String] =
      if input.isEmpty then dbTimeout.removeTimeout(msg.chat.id, botName) *> "Timeout removed".pure[F]
      else
        Timeout(ChatId(msg.chat.id), botName, input)
          .fold(
            error =>
              log.info(
                s"[ERROR] While parsing the timeout input: $error"
              ) *> s"Timeout set failed: wrong input format for $input, the input must be in the form '\timeout 00:00:00'"
                .pure[F],
            timeout =>
              dbTimeout.setTimeout(
                DBTimeoutData(timeout)
              ) *> s"Timeout set successfully to ${Timeout.formatTimeout(timeout)}".pure[F]
          )

    def timeoutReplyBundleCommand[F[_]: MonadThrow](
        botName: String,
        dbTimeout: DBTimeout[F],
        log: LogWriter[F]
    ): ReplyBundleCommand[F] =
      ReplyBundleCommand(
        trigger = CommandTrigger("timeout"),
        reply = TextReplyM[F](
          msg =>
            handleCommandWithInput[F](
              msg,
              "timeout",
              botName,
              timeoutLogic(_, msg, dbTimeout, botName, log).map(List(_)),
              """Input Required: the input must be in the form '\timeout 00:00:00' or empty"""
            ),
          true
        )
      )
  }

  def handleCommandWithInput[F[_]: ApplicativeThrow](
      msg: Message,
      command: String,
      botName: String,
      computation: String => F[List[String]],
      defaultReply: String,
      allowEmptyString: Boolean = false
  ): F[List[Text]] =
    msg.text
      .filter(t =>
        val (inputCommand, rest) = t.trim.span(_ != ' ')
        val restCheck            = allowEmptyString || (rest.trim.nonEmpty && !allowEmptyString)
        val commandCheck         = inputCommand == s"/$command" || inputCommand == s"/$command@$botName"
        commandCheck && restCheck
      )
      .map(t => computation(t.dropWhile(_ != ' ').tail.trim))
      .getOrElse(List(defaultReply).pure[F])
      .handleErrorWith(e =>
        List(
          s"""An error occurred processing the command: $command
             | message text: ${msg.text.orElse(msg.caption).getOrElse("")}
             | bot: $botName
             | error: ${e.getMessage}""".stripMargin
        ).pure[F]
      )
      .map(_.toText)
}
