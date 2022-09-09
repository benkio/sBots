package com.benkio.youtuboancheiobot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.CommandPatterns.RandomLinkCommand
import com.benkio.telegrambotinfrastructure.botcapabilities.CommandPatterns.handleCommandWithInput
import com.benkio.telegrambotinfrastructure.botcapabilities._
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.BotOps
import com.benkio.telegrambotinfrastructure._
import com.lightbend.emoji.ShortCodes.Defaults._
import com.lightbend.emoji.ShortCodes.Implicits._
import com.lightbend.emoji._
import doobie.Transactor
import log.effect.LogWriter
import org.http4s.Status
import org.http4s.client.Client
import org.http4s.ember.client._
import telegramium.bots.high._

class YoutuboAncheIoBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    rAccess: ResourceAccess[F]
) extends BotSkeletonPolling[F]
    with YoutuboAncheIoBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = rAccess
}

class YoutuboAncheIoBotWebhook[F[_]: Async: Api: LogWriter](url: String, rAccess: ResourceAccess[F], path: String = "/")
    extends BotSkeletonWebhook[F](url, path)
    with YoutuboAncheIoBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = rAccess
}

trait YoutuboAncheIoBot[F[_]] extends BotSkeleton[F] {

  override val ignoreMessagePrefix: Option[String] = YoutuboAncheIoBot.ignoreMessagePrefix

  override def messageRepliesDataF(implicit
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    YoutuboAncheIoBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(implicit asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    List(
      randomLinkByKeywordReplyBundleF,
      randomLinkReplyBundleF
    ).sequence.map(cs =>
      cs ++
        YoutuboAncheIoBot.commandRepliesData[F]
    )

  private def randomLinkReplyBundleF(implicit asyncF: Async[F], log: LogWriter[F]): F[ReplyBundleCommand[F]] =
    ReplyBundleCommand(
      trigger = CommandTrigger("randomshow"),
      text = Some(
        TextReply[F](
          _ =>
            RandomLinkCommand
              .selectRandomLinkByKeyword[F](
                "",
                resourceAccess,
                "ytai_LinkSources"
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
              "YoutuboAncheIoBot",
              keywords =>
                RandomLinkCommand
                  .selectRandomLinkByKeyword[F](
                    keywords,
                    resourceAccess,
                    "ytai_LinkSources"
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
object YoutuboAncheIoBot extends BotOps {

  val ignoreMessagePrefix: Option[String] = Some("!")

  def messageRepliesAudioData[
      F[_] // : Applicative
  ]: List[ReplyBundleMessage[F]] = List(
  )

  def messageRepliesGifData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("acqua calabria")
      ),
      mediafiles = List(
        MediaFile("ytai_AcquaSguardo.gif"),
        MediaFile("ytai_Sete.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("fatica"),
        StringTextTriggerValue("sudore"),
        StringTextTriggerValue("sudato")
      ),
      mediafiles = List(
        MediaFile("ytai_Affaticamento.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("ascolta (queste|le) mie parole".r),
        StringTextTriggerValue("amareggiati"),
        RegexTextTriggerValue("dedicaci (il tuo tempo|le tue notti)".r)
      ),
      mediafiles = List(
        MediaFile("ytai_Amareggiati.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("architetta"),
        StringTextTriggerValue("notaia"),
        StringTextTriggerValue("ministra"),
        StringTextTriggerValue("avvocata")
      ),
      mediafiles = List(
        MediaFile("ytai_Architetta.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("bel sogno")
      ),
      mediafiles = List(
        MediaFile("ytai_BelSogno.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("brivido"),
        StringTextTriggerValue("fremito"),
        StringTextTriggerValue("tremito"),
        StringTextTriggerValue("tremore")
      ),
      mediafiles = List(
        MediaFile("ytai_Brivido.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("buonanotte")
      ),
      mediafiles = List(
        MediaFile("ytai_Buonanotte.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("buona pizza")
      ),
      mediafiles = List(
        MediaFile("ytai_BuonaPizza.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("buonasera")
      ),
      mediafiles = List(
        MediaFile("ytai_Buonasera.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("che spettacolo")
      ),
      mediafiles = List(
        MediaFile("ytai_CheSpettacolo.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("che vergogna"),
        StringTextTriggerValue("non ce l'ho"),
        StringTextTriggerValue("sopracciglia"),
        RegexTextTriggerValue("tutti (quanti )?mi criticheranno".r)
      ),
      mediafiles = List(
        MediaFile("ytai_CheVergogna.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ciao!")
      ),
      mediafiles = List(
        MediaFile("ytai_Ciao.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ciao ragazzi"),
        StringTextTriggerValue("cari saluti")
      ),
      mediafiles = List(
        MediaFile("ytai_CiaoRagazzi.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ci divertiremo"),
        StringTextTriggerValue("bel percorso")
      ),
      mediafiles = List(
        MediaFile("ytai_CiDivertiremoPercorso.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("colore meraviglioso")
      ),
      mediafiles = List(
        MediaFile("ytai_ColoreMeraviglioso.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("corpulenti"),
        StringTextTriggerValue("ciccioni")
      ),
      mediafiles = List(
        MediaFile("ytai_CorpulentiCiccioni.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("culetto")
      ),
      mediafiles = List(
        MediaFile("ytai_Culetto.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("donazioni")
      ),
      mediafiles = List(
        MediaFile("ytai_Donazioni.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ed allora")
      ),
      mediafiles = List(
        MediaFile("ytai_EdAllora.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("fai pure")
      ),
      mediafiles = List(
        MediaFile("ytai_FaiPure.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("fallo anche (tu|te)".r),
      ),
      mediafiles = List(
        MediaFile("ytai_FalloAncheTu.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("filet[ -]?o[ -]fish".r),
      ),
      mediafiles = List(
        MediaFile("ytai_FiletOFish.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue(
          "(deluso|insoddisfatto|inappagato|abbattuto|scoraggiato|demoralizzato|depresso|demotivato|avvilito|scocciato)".r
        )
      ),
      mediafiles = List(
        MediaFile("ytai_Frustrazione.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ci sto già pensando")
      ),
      mediafiles = List(
        MediaFile("ytai_GiaPensando.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sono grande"),
        StringTextTriggerValue("sono corpulento")
      ),
      mediafiles = List(
        MediaFile("ytai_GrandeCorpulento.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("grazie dottore"),
      ),
      mediafiles = List(
        MediaFile("ytai_GrazieDottore.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("grazie tante"),
        StringTextTriggerValue("sconforto grave")
      ),
      mediafiles = List(
        MediaFile("ytai_GrazieTante.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("incredibile profumo"),
        StringTextTriggerValue("incredibile aroma")
      ),
      mediafiles = List(
        MediaFile("ytai_IncredibileAromaProfumo.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("incredibile"),
        StringTextTriggerValue("inimitabile"),
        RegexTextTriggerValue("the number (one|1)".r)
      ),
      mediafiles = List(
        MediaFile("ytai_IncredibileInimitabile.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("io non lo so")
      ),
      mediafiles = List(
        MediaFile("ytai_IoNonLoSo.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("loro dovere"),
        StringTextTriggerValue("vostro diritto")
      ),
      mediafiles = List(
        MediaFile("ytai_LoroDovereVostroDiritto.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("lo so anche io")
      ),
      mediafiles = List(
        MediaFile("ytai_LoSoAncheIo.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("mah")
      ),
      mediafiles = List(
        MediaFile("ytai_Mah.gif"),
        MediaFile("ytai_ZoomMah.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("meraviglioso"),
      ),
      mediafiles = List(
        MediaFile("ytai_Meraviglioso.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("molto buona")
      ),
      mediafiles = List(
        MediaFile("ytai_MoltoBuona.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("monoporzioni"),
        RegexTextTriggerValue("mezzo (chilo|kg)".r),
        StringTextTriggerValue("tiramisù"),
      ),
      mediafiles = List(
        MediaFile("ytai_MonoporzioniTiramisu.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("a me niete va bene"),
        StringTextTriggerValue("non mi va bene niente")
      ),
      mediafiles = List(
        MediaFile("ytai_NienteVaBene.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non fa male")
      ),
      mediafiles = List(
        MediaFile("ytai_NonFaMale.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non la crede nessuno questa cosa"),
        StringTextTriggerValue("non ci crede nessuno")
      ),
      mediafiles = List(
        MediaFile("ytai_NonLaCredeNessuno.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non pensiamoci più")
      ),
      mediafiles = List(
        MediaFile("ytai_NonPensiamoci.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("incomprensione"),
        StringTextTriggerValue("non vi capiscono")
      ),
      mediafiles = List(
        MediaFile("ytai_NonViCapiscono.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("occhiolino"),
        StringTextTriggerValue(";)"),
        StringTextTriggerValue(e":wink:"),
      ),
      mediafiles = List(
        MediaFile("ytai_Occhiolino.gif"),
        MediaFile("ytai_Occhiolino2.gif"),
        MediaFile("ytai_Occhiolino3.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("olè"),
      ),
      mediafiles = List(
        MediaFile("ytai_Ole.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("olè anche io"),
      ),
      mediafiles = List(
        MediaFile("ytai_OleAncheIo.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("la perfezione"),
        StringTextTriggerValue("la nostra tendenza")
      ),
      mediafiles = List(
        MediaFile("ytai_PerfezioneTendenza.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("profumo meraviglioso"),
        StringTextTriggerValue("sentiamo il profumo")
      ),
      mediafiles = List(
        MediaFile("ytai_ProfumoMeraviglioso.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ricordami fino a domani")
      ),
      mediafiles = List(
        MediaFile("ytai_Ricordami.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ringraziamento"),
      ),
      mediafiles = List(
        MediaFile("ytai_Ringraziamento.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("se non mi aiuta"),
        StringTextTriggerValue("cosa mi aiuta")
      ),
      mediafiles = List(
        MediaFile("ytai_SentiteCheRoba.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sete"),
        RegexTextTriggerValue("(sorso|bicchiere) d'acqua".r)
      ),
      mediafiles = List(
        MediaFile("ytai_Sete.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue(Emoji(0x1f937).toString)
      ),
      mediafiles = List(
        MediaFile("ytai_Shrug.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("silenzio sta per dire qualcosa"),
      ),
      mediafiles = List(
        MediaFile("ytai_Silenzio.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("si va finchè si va"),
        StringTextTriggerValue("quando non si potrà andare più"),
        StringTextTriggerValue("è tanto facile")
      ),
      mediafiles = List(
        MediaFile("ytai_SiVaFincheSiVa.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sorprendente"),
      ),
      mediafiles = List(
        MediaFile("ytai_Sorprendente.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue(e":smile:"),
        StringTextTriggerValue(e":smiley:"),
        StringTextTriggerValue(":)"),
        StringTextTriggerValue("sorriso")
      ),
      mediafiles = List(
        MediaFile("ytai_Sorriso.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("spuntino"),
      ),
      mediafiles = List(
        MediaFile("ytai_SpuntinoConMe.gif"),
        MediaFile("ytai_SpuntinoConMe2.gif")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("ti voglio (tanto )?bene".r),
      ),
      mediafiles = List(
        MediaFile("ytai_TVTB.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("l'unica cosa che sai fare"),
      ),
      mediafiles = List(
        MediaFile("ytai_UnicaCosaMangiare.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("uno o due")
      ),
      mediafiles = List(
        MediaFile("ytai_UnoODue.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("zenzero")
      ),
      mediafiles = List(
        MediaFile("ytai_Zenzero.gif")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("zoom"),
        StringTextTriggerValue("guardo da vicino")
      ),
      mediafiles = List(
        MediaFile("ytai_Zoom.gif"),
        MediaFile("ytai_ZoomMah.gif")
      )
    ),
  )

  def messageRepliesSpecialData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("ho perso (di nuovo )qualcosa".r)
      ),
      mediafiles = List(
        MediaFile("ytai_HoPersoQualcosa.gif"),
        MediaFile("ytai_HoPersoQualcosa.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("reputazione")
      ),
      mediafiles = List(
        MediaFile("ytai_LaReputazione.gif"),
        MediaFile("ytai_CheVergogna.gif")
      ),
      replySelection = RandomSelection
    ),
  )

  def messageRepliesData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesSpecialData[F])
      .sorted(ReplyBundle.ordering[F])
      .reverse

  def messageReplyDataStringChunks[
      F[_]: Applicative
  ]: List[String] = {
    val (triggers, lastTriggers) = messageRepliesData[F]
      .map(_.trigger match {
        case TextTrigger(lt @ _*) => lt.mkString("[", " - ", "]")
        case _                    => ""
      })
      .foldLeft((List.empty[String], "")) { case ((acc, candidate), triggerString) =>
        if ((candidate ++ triggerString).length > 4090)
          (acc :+ candidate, triggerString)
        else
          (acc, candidate ++ triggerString)
      }
    triggers :+ lastTriggers
  }

  def commandRepliesData[
      F[_]: Applicative
  ]: List[ReplyBundleCommand[F]] = List(
    ReplyBundleCommand(
      trigger = CommandTrigger("triggerlist"),
      text = Some(
        TextReply(
          m => {
            if (m.chat.`type` == "private")
              Applicative[F].pure(messageReplyDataStringChunks[F])
            else
              Applicative[F].pure(List("puoi usare questo comando solo in chat privata"))
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
              "YoutuboAncheIoBot",
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
  )

  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess.fromResources.getResourceByteArray("ytai_YoutuboAncheIoBot.token").map(_.map(_.toChar).mkString)

  def buildCommonBot[F[_]: Async](
      httpClient: Client[F]
  )(implicit log: LogWriter[F]): Resource[F, (String, ResourceAccess[F])] = for {
    tk     <- token[F]
    config <- Resource.eval(Config.loadConfig[F])
    _      <- Resource.eval(log.info(s"YoutuboAncheIoBot Configuration: $config"))
    transactor = Transactor.fromDriverManager[F](
      config.driver,
      config.url,
      "",
      ""
    )
    urlFetcher            <- Resource.eval(UrlFetcher[F](httpClient))
    dbResourceAccess      <- Resource.eval(DBResourceAccess(transactor, urlFetcher))
    _                     <- Resource.eval(log.info("[YoutuboAncheIoBot] Delete webook..."))
    deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
    _ <- Resource.eval(
      Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
        new RuntimeException(
          "[YoutuboAncheIoBot] The delete webhook request failed: " + deleteWebhookResponse.as[String]
        )
      )
    )
    _ <- Resource.eval(log.info("[YoutuboAncheIoBot] Webhook deleted"))
  } yield (tk, dbResourceAccess)

  def buildPollingBot[F[_]: Parallel: Async, A](
      action: YoutuboAncheIoBotPolling[F] => F[A]
  )(implicit log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].build
    tk_ra      <- buildCommonBot[F](httpClient)
  } yield (httpClient, tk_ra._1, tk_ra._2)).use(httpClient_tk_ra => {
    implicit val api: Api[F] =
      BotApi(httpClient_tk_ra._1, baseUrl = s"https://api.telegram.org/bot${httpClient_tk_ra._2}")
    action(new YoutuboAncheIoBotPolling[F](httpClient_tk_ra._3))
  })

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host
  )(implicit log: LogWriter[F]): Resource[F, YoutuboAncheIoBotWebhook[F]] = for {
    tk_ra <- buildCommonBot[F](httpClient)
    baseUrl = s"https://api.telegram.org/bot${tk_ra._1}"
    path    = s"/${tk_ra._1}"
  } yield {
    implicit val api: Api[F] = BotApi(httpClient, baseUrl = baseUrl)
    new YoutuboAncheIoBotWebhook[F](
      url = webhookBaseUrl + path,
      rAccess = tk_ra._2,
      path = path
    )
  }
}
