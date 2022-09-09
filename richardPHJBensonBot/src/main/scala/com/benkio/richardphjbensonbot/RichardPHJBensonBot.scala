package com.benkio.richardphjbensonbot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.richardphjbensonbot.Config
import com.benkio.telegrambotinfrastructure.botcapabilities.CommandPatterns._
import com.benkio.telegrambotinfrastructure.botcapabilities._
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.model.TextReply
import com.benkio.telegrambotinfrastructure.model.TextTriggerValue
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.BotOps
import com.benkio.telegrambotinfrastructure._
import doobie._
import log.effect.LogWriter
import org.http4s.Status
import org.http4s.client.Client
import org.http4s.ember.client._
import telegramium.bots.Message
import telegramium.bots.high._

class RichardPHJBensonBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    rAccess: ResourceAccess[F],
    val dbTimeout: DBTimeout[F]
) extends BotSkeletonPolling[F]
    with RichardPHJBensonBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = rAccess
  override def postComputation(implicit syncF: Sync[F]): Message => F[Unit] = m =>
    dbTimeout.logLastInteraction(m.chat.id)
  override def filteringMatchesMessages(implicit
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    (_, m) =>
      for {
        timeout <- dbTimeout.getOrDefault(m.chat.id)
      } yield Timeout.isExpired(timeout)
}

class RichardPHJBensonBotWebhook[F[_]: Async: Api: LogWriter](
    url: String,
    rAccess: ResourceAccess[F],
    val dbTimeout: DBTimeout[F],
    path: String = "/"
) extends BotSkeletonWebhook[F](url, path)
    with RichardPHJBensonBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = rAccess
  override def postComputation(implicit syncF: Sync[F]): Message => F[Unit] = m =>
    dbTimeout.logLastInteraction(m.chat.id)
  override def filteringMatchesMessages(implicit
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    (_, m) =>
      for {
        timeout <- dbTimeout.getOrDefault(m.chat.id)
      } yield Timeout.isExpired(timeout)
}

trait RichardPHJBensonBot[F[_]] extends BotSkeleton[F] {

  override val botName: String = "RichardPHJBensonBot"
  val dbTimeout: DBTimeout[F]

  override val ignoreMessagePrefix: Option[String] = RichardPHJBensonBot.ignoreMessagePrefix

  override def messageRepliesDataF(implicit
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    RichardPHJBensonBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(implicit asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    List(randomLinkByKeywordReplyBundleF, randomLinkReplyBundleF).sequence.map(cs =>
      cs ++ RichardPHJBensonBot.commandRepliesData[F](dbTimeout)
    )

  private def randomLinkReplyBundleF(implicit asyncF: Async[F], log: LogWriter[F]): F[ReplyBundleCommand[F]] =
    ReplyBundleCommand[F](
      trigger = CommandTrigger("randomshow"),
      text = Some(
        TextReply[F](
          _ =>
            RandomLinkCommand
              .selectRandomLinkByKeyword[F](
                "",
                resourceAccess,
                "rphjb_LinkSources"
              )
              .use(optMessage => Applicative[F].pure(optMessage.toList)),
          true
        )
      ),
    ).pure[F]

  private def randomLinkByKeywordReplyBundleF(implicit asyncF: Async[F], log: LogWriter[F]): F[ReplyBundleCommand[F]] =
    ReplyBundleCommand[F](
      trigger = CommandTrigger("randomshowkeyword"),
      text = Some(
        TextReply[F](
          m =>
            handleCommandWithInput[F](
              m,
              "randomshowkeyword",
              "RichardPHJBensonBot",
              keywords =>
                RandomLinkCommand
                  .selectRandomLinkByKeyword[F](
                    keywords,
                    resourceAccess,
                    "rphjb_LinkSources"
                  )
                  .use(_.foldl(List(s"Nessuna puntata/show contenente '$keywords' è stata trovata")) { case (_, v) =>
                    List(v)
                  }.pure[F]),
              s"Inserisci una keyword da cercare tra le puntate/shows"
            ),
          true
        )
      ),
    ).pure[F]
}

object RichardPHJBensonBot extends BotOps {

  import com.benkio.richardphjbensonbot.data.Audio.messageRepliesAudioData
  import com.benkio.richardphjbensonbot.data.Video.messageRepliesVideoData
  import com.benkio.richardphjbensonbot.data.Gif.messageRepliesGifData
  import com.benkio.richardphjbensonbot.data.Special.messageRepliesSpecialData
  import com.benkio.richardphjbensonbot.data.Mix.messageRepliesMixData

  val ignoreMessagePrefix: Option[String] = Some("!")

  def messageRepliesData[F[_]: Applicative]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesVideoData[F] ++ messageRepliesMixData[
      F
    ] ++ messageRepliesSpecialData[F])
      .sorted(ReplyBundle.ordering[F])
      .reverse

  def messageReplyDataStringChunks[F[_]: Applicative]: List[String] = {
    val (triggers, lastTriggers) = messageRepliesData[F]
      .map(_.trigger match {
        case TextTrigger(lt @ _*) => lt.mkString("[", " - ", "]")
        case _                    => ""
      })
      .foldLeft((List.empty[String], "")) { case ((acc, candidate), triggerString) =>
        if ((candidate ++ triggerString).length > 4090)
          (acc :+ candidate, triggerString)
        else (acc, candidate ++ triggerString)
      }
    triggers :+ lastTriggers
  }

  def commandRepliesData[F[_]: Applicative](dbTimeout: DBTimeout[F]): List[ReplyBundleCommand[F]] = List(
    ReplyBundleCommand(
      trigger = CommandTrigger("triggerlist"),
      text = Some(
        TextReply[F](
          m => {
            if (m.chat.`type` == "private") Applicative[F].pure(messageReplyDataStringChunks[F])
            else
              Applicative[F].pure(List("NON TE LO PUOI PERMETTERE!!!(puoi usare questo comando solo in chat privata)"))
          },
          false
        )
      )
    ),
    ReplyBundleCommand(
      trigger = CommandTrigger("triggersearch"),
      text = Some(
        TextReply[F](
          m =>
            handleCommandWithInput[F](
              m,
              "triggersearch",
              "RichardPHJBensonBot",
              t =>
                messageRepliesData[F]
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
    ),
    ReplyBundleCommand(
      trigger = CommandTrigger("timeout"),
      text = Some(
        TextReply[F](
          msg =>
            handleCommandWithInput[F](
              msg,
              "timeout",
              "RichardPHJBensonBot",
              t => {
                val timeout = Timeout(msg, t).toList
                if (timeout.isEmpty)
                  List(
                    s"Timeout set failed: wrong input format for $t, the input must be in the form '\timeout 00:00:00'"
                  ).pure[F]
                else
                  timeout.traverse_(dbTimeout.setTimeout) *> List("Timeout set successfully").pure[F]
              },
              """Input Required: the input must be in the form '\timeout 00:00:00'"""
            ),
          true
        )
      )
    ),
    ReplyBundleCommand(
      trigger = CommandTrigger("bensonify"),
      text = Some(
        TextReply[F](
          msg =>
            handleCommandWithInput[F](
              msg,
              "bensonify",
              "RichardPHJBensonBot",
              t => List(Bensonify.compute(t)).pure[F],
              "E PARLAAAAAAA!!!!"
            ),
          true
        )
      )
    ),
    ReplyBundleCommand(
      trigger = CommandTrigger("instructions"),
      text = Some(
        TextReply[F](
          _ => List(s"""
---- Instruzioni Per il Bot di Benson ----

Il bot reagisce automaticamente ai messaggi in base ai trigger che si
possono trovare dal comando:

/triggerlist

ATTENZIONE: tale comando invierà una lunga lista. Consultarlo
privatamente nella chat del bot.

Questo bot consente di convertire le frasi come le direbbe Richard
attraverso il comando:

/bensonify «Frase»

la frase è necessaria, altrimenti il bot vi risponderà in malomodo.

Infine, se si vuole disabilitare il bot per un particolare messaggio,
ad esempio per un messaggio lungo che potrebbe causare vari trigger
in una volta, è possibile farlo iniziando il messaggio con il
carattere '!':

! «Messaggio»
""").pure[F],
          false
        )
      )
    )
  )
  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess
      .fromResources[F]
      .getResourceByteArray("rphjb_RichardPHJBensonBot.token")
      .map(_.map(_.toChar).mkString)

  final case class BotSetup[F[_]](token: String, resourceAccess: ResourceAccess[F], dbTimeout: DBTimeout[F])

  def buildCommonBot[F[_]: Async](
      httpClient: Client[F]
  )(implicit log: LogWriter[F]): Resource[F, BotSetup[F]] =
    for {
      tk     <- token[F]
      config <- Resource.eval(Config.loadConfig[F])
      _      <- Resource.eval(log.info(s"RichardPHJBensonBot Configuration: $config"))
      transactor = Transactor.fromDriverManager[F](
        config.driver,
        config.url,
        "",
        ""
      )
      urlFetcher       <- Resource.eval(UrlFetcher[F](httpClient))
      dbResourceAccess <- Resource.eval(DBResourceAccess(transactor, urlFetcher))
      dbTimeout = DBTimeout(transactor)
      _                     <- Resource.eval(log.info("[RichardPHJBensonBot] Delete webook..."))
      deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
      _ <- Resource.eval(
        Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
          new RuntimeException(
            "[RichardPHJBensonBot] The delete webhook request failed: " + deleteWebhookResponse.as[String]
          )
        )
      )
      _ <- Resource.eval(log.info("[RichardPHJBensonBot] Webhook deleted"))
    } yield BotSetup(tk, dbResourceAccess, dbTimeout)

  def buildPollingBot[F[_]: Parallel: Async, A](
      action: RichardPHJBensonBotPolling[F] => F[A]
  )(implicit log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].build
    botSetup   <- buildCommonBot[F](httpClient)
  } yield (httpClient, botSetup)).use {
    case (httpClient, BotSetup(token, resourceAccess, dbTimeout)) => {
      implicit val api: Api[F] = BotApi(
        httpClient,
        baseUrl = s"https://api.telegram.org/bot$token"
      )
      action(new RichardPHJBensonBotPolling[F](rAccess = resourceAccess, dbTimeout = dbTimeout))
    }
  }

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
  )(implicit log: LogWriter[F]): Resource[F, RichardPHJBensonBotWebhook[F]] = for {
    botSetup <- buildCommonBot[F](httpClient)
    baseUrl = s"https://api.telegram.org/bot${botSetup.token}"
    path    = s"/${botSetup.token}"
  } yield {
    implicit val api: Api[F] = BotApi(httpClient, baseUrl = baseUrl)
    new RichardPHJBensonBotWebhook[F](
      url = webhookBaseUrl + path,
      path = s"/${botSetup.token}",
      rAccess = botSetup.resourceAccess,
      dbTimeout = botSetup.dbTimeout
    )
  }
}
