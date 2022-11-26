package com.benkio.telegrambotinfrastructure.patterns

import cats.effect.Async
import cats.effect.Resource
import cats.implicits._
import cats.Applicative
import cats.ApplicativeThrow
import cats.MonadThrow
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.model.CommandTrigger
import com.benkio.telegrambotinfrastructure.model.Media
import com.benkio.telegrambotinfrastructure.model.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.Subscription
import com.benkio.telegrambotinfrastructure.model.TextReply
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import com.benkio.telegrambotinfrastructure.model.TextTriggerValue
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia
import cron4s._
import cron4s.lib.javatime._
import log.effect.LogWriter
import org.http4s.Uri
import telegramium.bots.Message

import java.time.LocalDateTime
import java.util.UUID
import scala.util.Random
import scala.util.Try

object CommandPatterns {

  object RandomLinkCommand {

    val randomLinkCommandDescriptionIta: String =
      "'/randomshow': Restituisce un link di uno show/video riguardante il personaggio del bot"
    val randomLinkCommandDescriptionEng: String =
      "'/randomshow': Return the link of one show/video about the bot's character"
    val randomLinkKeywordCommandIta: String =
      "'/searchrandomshowkeyword 《testo》': Restituisce un link di uno show/video riguardante il personaggio del bot e contenente il testo specificato"
    val randomLinkKeywordCommandEng: String =
      "'/searchrandomshowkeyword 《text》': Return a link of a show/video about the specific bot's character and containing the specified keyword"

    lazy val random = new Random()

    def selectRandomLinkReplyBundleCommand[F[_]: Async](
        resourceAccess: ResourceAccess[F],
        youtubeLinkSources: String
    )(implicit log: LogWriter[F]): ReplyBundleCommand[F] =
      ReplyBundleCommand(
        trigger = CommandTrigger("randomshow"),
        text = Some(
          TextReply[F](
            _ =>
              selectRandomLinkByKeyword[F](
                "",
                resourceAccess,
                youtubeLinkSources
              )
                .use(optMessage => Applicative[F].pure(optMessage.toList)),
            true
          )
        ),
      )

    def selectRandomLinkByKeywordsReplyBundleCommand[F[_]: Async](
        resourceAccess: ResourceAccess[F],
        botName: String,
        youtubeLinkSources: String
    )(implicit log: LogWriter[F]): ReplyBundleCommand[F] =
      ReplyBundleCommand[F](
        trigger = CommandTrigger("searchrandomshowkeyword"),
        text = Some(
          TextReply[F](
            m =>
              handleCommandWithInput[F](
                m,
                "searchrandomshowkeyword",
                botName,
                keywords =>
                  RandomLinkCommand
                    .selectRandomLinkByKeyword[F](
                      keywords,
                      resourceAccess,
                      youtubeLinkSources
                    )
                    .use(_.foldl(List(s"Nessuna puntata/show contenente '$keywords' è stata trovata")) { case (_, v) =>
                      List(v)
                    }.pure[F]),
                s"Inserisci una keyword da cercare tra le puntate/shows"
              ),
            true
          )
        ),
      )

    def selectRandomLinkByKeyword[F[_]: Async](
        keywords: String,
        resourceAccess: ResourceAccess[F],
        youtubeLinkSources: String
    )(implicit log: LogWriter[F]): Resource[F, Option[String]] = ???
  }

  object TriggerListCommand {

    val triggerListCommandDescriptionIta: String =
      "'/triggerlist': Restituisce un link ad un file contenente tutti i trigger a cui il bot risponderà automaticamente. Alcuni di questi sono in formato Regex"
    val triggerListCommandDescriptionEng: String =
      "'/triggerlist': Return a link to a file containing all the triggers used by the bot. Bot will reply automatically to these ones. Some of them are Regex"

    def triggerListReplyBundleCommand[F[_]: Applicative](triggerFileUri: Uri): ReplyBundleCommand[F] =
      ReplyBundleCommand(
        trigger = CommandTrigger("triggerlist"),
        text = Some(
          TextReply(
            _ => Applicative[F].pure(List(s"Puoi trovare la lista dei trigger al seguente URL: $triggerFileUri")),
            true
          )
        )
      )
  }

  object TriggerSearchCommand {

    val triggerSearchCommandDescriptionIta: String =
      "'/triggersearch 《testo》': Consente di cercare se una parola o frase fa parte di un trigger"
    val triggerSearchCommandDescriptionEng: String =
      "'/triggersearch 《text》': Allow you to search if a specific word or phrase is part of a trigger"

    // TODO: Return the closest match on failure
    def triggerSearchReplyBundleCommand[F[_]: ApplicativeThrow](
        botName: String,
        ignoreMessagePrefix: Option[String],
        mdr: List[ReplyBundleMessage[F]]
    ): ReplyBundleCommand[F] =
      ReplyBundleCommand(
        trigger = CommandTrigger("triggersearch"),
        text = Some(
          TextReply[F](
            m =>
              handleCommandWithInput[F](
                m,
                "triggersearch",
                botName,
                t =>
                  mdr
                    .collectFirstSome(replyBundle =>
                      replyBundle.trigger match {
                        case TextTrigger(textTriggers @ _*)
                            if MessageMatches.doesMatch(replyBundle, m, ignoreMessagePrefix) =>
                          Some(textTriggers.toList)
                        case _ => None
                      }
                    )
                    .fold(List(s"No matching trigger for $t"))((textTriggers: List[TextTriggerValue]) =>
                      textTriggers.map(_.toString)
                    )
                    .pure[F],
                """Input Required: Insert the test keyword to check if it's in some bot trigger"""
              ),
            false
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

I comandi del bot sono:

${commandDescriptions.mkString("- ", "\n- ", "")}

${if (ignoreMessagePrefix.isDefined) {
        s"Se si vuole disabilitare il bot per un particolare messaggio impedendo\nche interagisca, è possibile farlo iniziando il messaggio con il\ncarattere: `${ignoreMessagePrefix.get}`\n\n${ignoreMessagePrefix.get} Messaggio"
      }}
"""

    def instructionMessageEng(
        botName: String,
        ignoreMessagePrefix: Option[String],
        commandDescriptions: List[String]
    ) = s"""
---- Instructions for $botName ----

Bot commands are:

${commandDescriptions.mkString("- ", "\n- ", "")}

${if (ignoreMessagePrefix.isDefined) {
        s"if you wish to disable the bot for a specific message, blocking its reply/interaction, you can do adding the following character as prefix\ncharacter: `${ignoreMessagePrefix.get}`\n\n${ignoreMessagePrefix.get} Message"
      }}
"""

    def instructionsReplyBundleCommand[F[_]: Applicative](
        botName: String,
        ignoreMessagePrefix: Option[String],
        commandDescriptionsIta: List[String],
        commandDescriptionsEng: List[String]
    ): ReplyBundleCommand[F] =
      ReplyBundleCommand(
        trigger = CommandTrigger("instructions"),
        text = Some(
          TextReply[F](
            _ =>
              List(
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
              ).pure[F],
            false
          )
        )
      )
  }

  object SubscribeUnsubscribeCommand {

    val subscribeCommandDescriptionIta: String =
      "'/subscribe 《cron time》': Iscrizione all'invio randomico di una puntata alla frequenza specificato nella chat corrente. Per il formato dell'input utilizzare questo codice come riferimento: https://scastie.scala-lang.org/hwpZ3fvcQ7q4xlfjoTjTvw. Attenzione, la libreria usata richiede anche i secondi come riportato nella documentazione: https://www.alonsodomin.me/cron4s/userguide/index.html"
    val subscribeCommandDescriptionEng: String =
      "'/subscribe 《cron time》': Subscribe to a random show at the specified frequency in the current chat. For the input format check the following code snippet: https://scastie.scala-lang.org/hwpZ3fvcQ7q4xlfjoTjTvw. You can find the docs here: https://www.alonsodomin.me/cron4s/userguide/index.html"
    val unsubscribeCommandDescriptionIta: String =
      "'/unsubscribe': Disiscrizione della chat corrente dall'invio di puntate. Disiscriviti da una sola iscrizione inviando l'UUID relativo o da tutte le sottoscrizioni per la chat corrente se non viene inviato nessun input"
    val unsubscribeCommandDescriptionEng: String =
      "'/unsubscribe': Unsubscribe the current chat from random shows. With a UUID as input, the specific subscription will be deleted. With no input, all the subscriptions for the current chat will be deleted"

    def subscribeReplyBundleCommand[F[_]: Async](
        backgroundJobManager: BackgroundJobManager[F],
        botName: String
    ): ReplyBundleCommand[F] =
      ReplyBundleCommand[F](
        trigger = CommandTrigger("subscribe"),
        text = Some(
          TextReply[F](
            m =>
              handleCommandWithInput[F](
                m,
                "subscribe",
                botName,
                cronInput =>
                  for {
                    subscription <- Async[F].fromEither(Subscription(m.chat.id, botName, cronInput))
                    now = LocalDateTime.now()
                    nextOccurrence = subscription.cron
                      .next(now)
                      .fold("`Unknown next occurrence`")(date => s"`${date.toString}`")
                    _ <- backgroundJobManager.scheduleSubscription(subscription)
                  } yield List(
                    s"Subscription successfully scheduled. Next occurrence of subscription is $nextOccurrence. Refer to this subscription with the ID: ${subscription.id}"
                  ),
                s"Input Required: insert a valid 〈cron time〉. Check the instructions"
              ),
            true
          )
        ),
      )

    def unsubscribeReplyBundleCommand[F[_]: Async](
        backgroundJobManager: BackgroundJobManager[F],
        botName: String
    ): ReplyBundleCommand[F] =
      ReplyBundleCommand[F](
        trigger = CommandTrigger("unsubscribe"),
        text = Some(
          TextReply[F](
            m =>
              handleCommandWithInput[F](
                m,
                "subscribe",
                botName,
                subscriptionIdInput =>
                  if (subscriptionIdInput.isEmpty)
                    for {
                      _ <- backgroundJobManager.cancelSubscriptions(m.chat.id)
                    } yield List(s"All Subscriptions for current chat successfully cancelled")
                  else
                    for {
                      subscriptionId <- Async[F].fromTry(Try(UUID.fromString(subscriptionIdInput)))
                      _              <- backgroundJobManager.cancelSubscription(subscriptionId)
                    } yield List(
                      s"Subscription successfully cancelled"
                    ),
                s"Input Required: insert a valid 〈UUID〉or no input to unsubscribe completely for this chat. Check the instructions"
              ),
            true
          )
        ),
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
        text = Some(
          TextReply[F](
            _ =>
              for {
                dbMedias <- dbMedia.getMediaByMediaCount(mediaNamePrefix = botPrefix.some)
                medias   <- MonadThrow[F].fromEither(dbMedias.traverse(Media.apply))
              } yield List(Media.mediaListToString(medias)),
            true
          )
        ),
      )

  }

  def handleCommandWithInput[F[_]: ApplicativeThrow](
      msg: Message,
      command: String,
      botName: String,
      computation: String => F[List[String]],
      defaultReply: String
  ): F[List[String]] =
    msg.text
      .filterNot(t => t.trim == s"/$command" || t.trim == s"/$command@$botName")
      .map(t => computation(t.dropWhile(_ != ' ').tail))
      .getOrElse(List(defaultReply).pure[F])
      .handleErrorWith(e =>
        List(
          s"An error occurred processing the command: $command from message: $msg by bot: $botName with error: ${e.getMessage}"
        ).pure[F]
      )
}
