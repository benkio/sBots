package com.benkio.richardphjbensonbot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.richardphjbensonbot.Config
import com.benkio.telegrambotinfrastructure.botcapabilities.CommandPatterns._
import com.benkio.telegrambotinfrastructure.botcapabilities._
import com.benkio.telegrambotinfrastructure.model.TextReply
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.BotOps
import com.benkio.telegrambotinfrastructure._
import log.effect.LogWriter
import org.http4s.Status
import org.http4s.blaze.client._
import org.http4s.client.Client
import telegramium.bots.high._

class RichardPHJBensonBotPolling[F[_]: Parallel: Async: Api: LogWriter](rAccess: ResourceAccess[F])
    extends BotSkeletonPolling[F]
    with RichardPHJBensonBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = rAccess
}

class RichardPHJBensonBotWebhook[F[_]: Async: Api: LogWriter](
    url: String,
    rAccess: ResourceAccess[F],
    path: String = "/"
) extends BotSkeletonWebhook[F](url, path)
    with RichardPHJBensonBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = rAccess
}

trait RichardPHJBensonBot[F[_]] extends BotSkeleton[F] {

  override def messageRepliesDataF(implicit applicativeF: Applicative[F]): F[List[ReplyBundleMessage[F]]] =
    RichardPHJBensonBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(implicit asyncF: Async[F]): F[List[ReplyBundleCommand[F]]] =
    List(randomLinkByKeywordReplyBundleF, randomLinkReplyBundleF).sequence.map(cs =>
      cs ++ RichardPHJBensonBot.commandRepliesData[F]
    )

  private def randomLinkReplyBundleF(implicit asyncF: Async[F]): F[ReplyBundleCommand[F]] =
    RandomLinkCommand
      .selectRandomLinkByKeyword[F](
        "",
        resourceAccess,
        "rphjb_LinkSources"
      )
      .use[ReplyBundleCommand[F]](optMessage =>
        ReplyBundleCommand[F](
          trigger = CommandTrigger("randomshow"),
          text = Some(TextReply[F](_ => Applicative[F].pure(optMessage.toList), true)),
        ).pure[F]
      )

  private def randomLinkByKeywordReplyBundleF(implicit asyncF: Async[F]): F[ReplyBundleCommand[F]] =
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

  def commandRepliesData[F[_]: Applicative]: List[ReplyBundleCommand[F]] = List(
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
    ResourceAccess.fromResources
      .getResourceByteArray("rphjb_RichardPHJBensonBot.token")
      .map(_.map(_.toChar).mkString)

  def buildCommonBot[F[_]: Async](
      httpClient: Client[F]
  )(implicit log: LogWriter[F]): Resource[F, (String, ResourceAccess[F])] =
    for {
      tk     <- token[F]
      config <- Resource.eval(Config.loadConfig[F])
      dbResourceAccess = DBResourceAccess(config)
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
    } yield (tk, dbResourceAccess)

  def buildPollingBot[F[_]: Parallel: Async, A](
      action: RichardPHJBensonBotPolling[F] => F[A]
  )(implicit log: LogWriter[F]): F[A] = (for {
    httpClient        <- BlazeClientBuilder[F].resource
    tk_resourceAccess <- buildCommonBot[F](httpClient)
  } yield (httpClient, tk_resourceAccess)).use(httpClient_tkResourceAccess => {
    implicit val api: Api[F] = BotApi(
      httpClient_tkResourceAccess._1,
      baseUrl = s"https://api.telegram.org/bot${httpClient_tkResourceAccess._2._1}"
    )
    action(new RichardPHJBensonBotPolling[F](rAccess = httpClient_tkResourceAccess._2._2))
  })

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
  )(implicit log: LogWriter[F]): Resource[F, RichardPHJBensonBotWebhook[F]] = for {
    tk_resourceAccess <- buildCommonBot[F](httpClient)
    baseUrl = s"https://api.telegram.org/bot${tk_resourceAccess._1}"
    path    = s"/${tk_resourceAccess._1}"
  } yield {
    implicit val api: Api[F] = BotApi(httpClient, baseUrl = baseUrl)
    new RichardPHJBensonBotWebhook[F](
      url = webhookBaseUrl + path,
      path = s"/${tk_resourceAccess._1}",
      rAccess = tk_resourceAccess._2
    )
  }
}
