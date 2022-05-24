package com.benkio.abarberobot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.botcapabilities.CommandPatterns._
import com.benkio.telegrambotinfrastructure.botcapabilities._
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.BotOps
import com.benkio.telegrambotinfrastructure._
import log.effect.LogWriter
import org.http4s.Status
import org.http4s.blaze.client._
import org.http4s.client.Client
import telegramium.bots.high._

class ABarberoBotPolling[F[_]: Parallel: Async: Api: LogWriter] extends BotSkeletonPolling[F] with ABarberoBot

class ABarberoBotWebhook[F[_]: Async: Api: LogWriter](url: String, path: String = "/")
    extends BotSkeletonWebhook[F](url, path)
    with ABarberoBot

trait ABarberoBot extends BotSkeleton {

  override val resourceSource: ResourceSource = ABarberoBot.resourceSource

  override def messageRepliesDataF[F[_]: Applicative]: F[List[ReplyBundleMessage[F]]] =
    ABarberoBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF[F[_]: Async]: F[List[ReplyBundleCommand[F]]] =
    List(
      randomLinkByKeywordReplyBundleF,
      randomLinkReplyBundleF
    ).sequence.map(cs => cs ++ ABarberoBot.commandRepliesData[F])

  private def randomLinkReplyBundleF[F[_]: Async]: F[ReplyBundleCommand[F]] =
    RandomLinkCommand
      .selectRandomLinkByKeyword[F](
        "",
        ResourceSource.selectResourceAccess(resourceSource),
        "abar_LinkSources"
      )
      .use[ReplyBundleCommand[F]](optMessage =>
        ReplyBundleCommand(
          trigger = CommandTrigger("randomshow"),
          text = Some(TextReply[F](_ => Applicative[F].pure(optMessage.toList), true)),
        ).pure[F]
      )

  private def randomLinkByKeywordReplyBundleF[F[_]: Async]: F[ReplyBundleCommand[F]] =
    ReplyBundleCommand[F](
      trigger = CommandTrigger("randomshowkeyword"),
      text = Some(
        TextReply[F](
          m =>
            handleCommandWithInput[F](
              m,
              "randomshowkeyword",
              "ABarberoBot",
              keywords =>
                RandomLinkCommand
                  .selectRandomLinkByKeyword[F](
                    keywords,
                    ResourceSource.selectResourceAccess(resourceSource),
                    "abar_LinkSources"
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

object ABarberoBot extends BotOps {

  val resourceSource: ResourceSource = FileSystem

  def messageRepliesAudioData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pestifero"),
        StringTextTriggerValue("maledetto")
      ),
      List(
        MediaFile("abar_Pestifero.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("distrutto"),
        StringTextTriggerValue("mangiato dai topi"),
        StringTextTriggerValue("bruciato"),
        StringTextTriggerValue("sepolto"),
        StringTextTriggerValue("nel fiume"),
        StringTextTriggerValue("innondazione")
      ),
      List(
        MediaFile("abar_Distrutto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bpratica\\b".r)
      ),
      List(
        MediaFile("abar_PraticaPocoPatriotticah.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ferro"),
        StringTextTriggerValue("fuoco"),
        StringTextTriggerValue("acqua bollente"),
        StringTextTriggerValue("aceto")
      ),
      List(
        MediaFile("abar_FerroFuocoAcquaBollenteAceto.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("secolo")
      ),
      List(
        MediaFile("abar_Secolo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("streghe"),
        StringTextTriggerValue("maghi"),
        StringTextTriggerValue("draghi"),
        StringTextTriggerValue("roghi"),
      ),
      List(
        MediaFile("abar_Draghi.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("crociate")
      ),
      List(
        MediaFile("abar_Crociate.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("wikipedia")
      ),
      List(
        MediaFile("abar_Wikipedia.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\beccoh\\b".r)
      ),
      List(
        MediaFile("abar_Ecco.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("maglio"),
        StringTextTriggerValue("sbriciola"),
        StringTextTriggerValue("schiaccia")
      ),
      List(
        MediaFile("abar_Maglio.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("chiese"),
        StringTextTriggerValue("castelli"),
        StringTextTriggerValue("villaggi"),
        StringTextTriggerValue("assedi")
      ),
      List(
        MediaFile("abar_Assedi.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("furore"),
        StringTextTriggerValue("città")
      ),
      List(
        MediaFile("abar_Furore.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("demoni"),
        StringTextTriggerValue("scatenat")
      ),
      List(
        MediaFile("abar_Demoni.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sensei")
      ),
      List(
        MediaFile("abar_Sensei.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("miserabile")
      ),
      List(
        MediaFile("abar_Miserabile.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("omicidio"),
        StringTextTriggerValue("cosa che capita")
      ),
      List(
        MediaFile("abar_CapitaOmicidio.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cavallo"),
        RegexTextTriggerValue("tiriamo(lo)? giù".r),
        StringTextTriggerValue("ammazziamolo")
      ),
      List(
        MediaFile("abar_Ammazziamolo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("bruciare"),
        StringTextTriggerValue("saccheggiare"),
        StringTextTriggerValue("fuoco")
      ),
      List(
        MediaFile("abar_Bbq.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("guerra"),
        StringTextTriggerValue("chi vuole"),
        StringTextTriggerValue("la vogliamo")
      ),
      List(
        MediaFile("abar_Guerra.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cagarelli"),
        StringTextTriggerValue("feci"),
        StringTextTriggerValue("cacca")
      ),
      List(
        MediaFile("abar_Homines.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("monsignore"),
        StringTextTriggerValue("vescovo"),
        StringTextTriggerValue("in culo")
      ),
      List(
        MediaFile("abar_Monsu.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ottimismo")
      ),
      List(
        MediaFile("abar_Ottimismo.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("latino")
      ),
      List(
        MediaFile("abar_Homines.mp3"),
        MediaFile("abar_Vagdavercustis.mp3"),
        MediaFile("abar_Yersinia.mp3"),
        MediaFile("abar_Culagium.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("radetzky")
      ),
      List(
        MediaFile("abar_Radetzky.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("interrogateli"),
        StringTextTriggerValue("tortura")
      ),
      List(
        MediaFile("abar_Reinterrogateli.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue(" re "),
        StringTextTriggerValue("decapita")
      ),
      List(
        MediaFile("abar_Re.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bascia\\b".r),
        StringTextTriggerValue("sangue")
      ),
      List(
        MediaFile("abar_Sangue.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("spranga")
      ),
      List(
        MediaFile("abar_Spranga.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stupidi")
      ),
      List(
        MediaFile("abar_Stupidi.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("subito")
      ),
      List(
        MediaFile("abar_Subito.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vagdavercustis")
      ),
      List(
        MediaFile("abar_Vagdavercustis.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("peste"),
        StringTextTriggerValue("yersinia")
      ),
      List(
        MediaFile("abar_Yersinia.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("zazzera")
      ),
      List(
        MediaFile("abar_Zazzera.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("metallo")
      ),
      List(
        MediaFile("abar_Metallo.mp3")
      )
    )
  )

  def messageRepliesGifData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ha ragione")
      ),
      List(
        MediaFile("abar_HaRagione.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("seziona"),
        StringTextTriggerValue("cadaveri"),
        StringTextTriggerValue("morti")
      ),
      List(
        MediaFile("abar_Cadaveri.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("strappa"),
        StringTextTriggerValue("gli arti"),
        StringTextTriggerValue("le braccia")
      ),
      List(
        MediaFile("abar_Strappare.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("saltare la testa"),
        StringTextTriggerValue("questa macchina")
      ),
      List(
        MediaFile("abar_SaltareLaTesta.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("paura")
      ),
      List(
        MediaFile("abar_Paura.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sega"),
        StringTextTriggerValue("dov'è")
      ),
      List(
        MediaFile("abar_Sega.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("potere"),
        StringTextTriggerValue("incarichi"),
        StringTextTriggerValue("poltrone"),
        StringTextTriggerValue("appalti"),
        StringTextTriggerValue("spartir")
      ),
      List(
        MediaFile("abar_Potere.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("grandioso"),
        StringTextTriggerValue("magnifico"),
        StringTextTriggerValue("capolavoro")
      ),
      List(
        MediaFile("abar_Capolavoro.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("troppo facile"),
        StringTextTriggerValue("easy")
      ),
      List(
        MediaFile("abar_TroppoFacile.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("trappola"),
        StringTextTriggerValue("tranello"),
        StringTextTriggerValue("inganno")
      ),
      List(
        MediaFile("abar_Trappola.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("faida"),
        StringTextTriggerValue("vendetta"),
        StringTextTriggerValue("rappresaglia"),
        StringTextTriggerValue("ritorsione")
      ),
      List(
        MediaFile("abar_Faida.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("chi(s| )se( )?ne( )?frega".r)
      ),
      List(
        MediaFile("abar_Chissenefrega.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("buonasera")
      ),
      List(
        MediaFile("abar_Buonasera.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("a morte"),
        RegexTextTriggerValue("(si| si|si ){2,}".r)
      ),
      List(
        MediaFile("abar_SisiAMorte.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("francesi")
      ),
      List(
        MediaFile("abar_Francesi.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("viva il popolo"),
        StringTextTriggerValue("comunis")
      ),
      List(
        MediaFile("abar_VivaIlPopolo.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("fare qualcosa")
      ),
      List(
        MediaFile("abar_FareQualcosa.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("(no|nessun|non c'è) problem(a)?"),
        StringTextTriggerValue("ammazziamo tutti")
      ),
      List(
        MediaFile("abar_AmmazziamoTuttiNoProblem.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bcerto\\b".r)
      ),
      List(
        MediaFile("abar_Certo.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("rogo")
      ),
      List(
        MediaFile("abar_Rogo.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("semplific")
      ),
      List(
        MediaFile("abar_Semplifico.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("bere"),
        RegexTextTriggerValue("taglia(re)? la gola".r)
      ),
      List(
        MediaFile("abar_TaglioGolaBereSangue.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("spacco la (testa|faccia)".r)
      ),
      List(
        MediaFile("abar_SpaccoLaTesta.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("dal (culo|sedere|fondo schiera|orifizio posteriore|dietro)".r)
      ),
      List(
        MediaFile("abar_OrifizioPosteriore.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("faccia tosta"),
        StringTextTriggerValue("furfante")
      ),
      List(
        MediaFile("abar_Furfante.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("basta!")
      ),
      List(
        MediaFile("abar_Basta.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("tutti insieme"),
        StringTextTriggerValue("ghigliottina")
      ),
      List(
        MediaFile("abar_GhigliottinaTuttiInsieme.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("economisti")
      ),
      List(
        MediaFile("abar_Economisti.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("vieni (un po' )?qui".r)
      ),
      List(
        MediaFile("abar_VieniQui.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("si fa così")
      ),
      List(
        MediaFile("abar_SiFaCosi.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("spranga")
      ),
      List(
        MediaFile("abar_Spranga.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("rapire"),
        StringTextTriggerValue("riscatto")
      ),
      List(
        MediaFile("abar_Riscatto.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue(" n[o]+!".r),
        RegexTextTriggerValue("non (lo )?vogli(a|o)".r)
      ),
      List(
        MediaFile("abar_No.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("in un attimo"),
        StringTextTriggerValue("in piazza")
      ),
      List(
        MediaFile("abar_InPiazza.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("in due pezzi")
      ),
      List(
        MediaFile("abar_InDuePezzi.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("giusto")
      ),
      List(
        MediaFile("abar_Giusto.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("gli altri")
      ),
      List(
        MediaFile("abar_GliAltri.gif")
      )
    )
  )

  def messageRepliesSpecialData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("tedesco")
      ),
      List(
        MediaFile("abar_Kraft.mp3"),
        MediaFile("abar_Von_Hohenheim.mp3"),
        MediaFile("abar_Haushofer.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("genitali"),
        StringTextTriggerValue("cosi e coglioni")
      ),
      List(
        MediaFile("abar_Cosi.mp3"),
        MediaFile("abar_Sottaceto.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("(figlio|fijo) (di|de) (mignotta|puttana|troia)".r)
      ),
      List(
        MediaFile("abar_FiglioDi.gif"),
        MediaFile("abar_FiglioDi2.gif"),
        MediaFile("abar_FiglioDi3.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sgozza")
      ),
      List(
        MediaFile("abar_Sgozzamento.mp3"),
        MediaFile("abar_Sgozzamento.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("bruciargli"),
        StringTextTriggerValue("la casa")
      ),
      List(
        MediaFile("abar_Bruciare.mp3"),
        MediaFile("abar_Bruciare.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("a pezzi"),
        StringTextTriggerValue("a pezzettini")
      ),
      List(
        MediaFile("abar_APezzettini.mp3"),
        MediaFile("abar_APezzettini.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("allarme"),
        StringTextTriggerValue("priori"),
        StringTextTriggerValue("carne")
      ),
      List(
        MediaFile("abar_Priori.mp3"),
        MediaFile("abar_Priori.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("zagaglia"),
        StringTextTriggerValue("nemico")
      ),
      List(
        MediaFile("abar_Zagaglia.mp3"),
        MediaFile("abar_Zagaglia.gif")
      )
    )
  )

  def messageRepliesData[F[_]: Applicative]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesSpecialData[F])
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
        else
          (acc, candidate ++ triggerString)
      }
    triggers :+ lastTriggers
  }

  def commandRepliesData[F[_]: Applicative]: List[ReplyBundleCommand[F]] = List(
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
    )
  )

  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess.fileSystem.getResourceByteArray[F]("abar_ABarberoBot.token").map(_.map(_.toChar).mkString)

  def buildPollingBot[F[_]: Parallel: Async, A](
      action: ABarberoBotPolling[F] => F[A]
  )(implicit log: LogWriter[F]): F[A] = (for {
    httpClient            <- BlazeClientBuilder[F].resource
    tk                    <- token[F]
    _                     <- Resource.eval(log.info("[ABarberoBot] Delete webook..."))
    deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
    _ <- Resource.eval(
      Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
        new RuntimeException("[ABarberoBot] The delete webhook request failed: " + deleteWebhookResponse.as[String])
      )
    )
    _ <- Resource.eval(log.info("[ABarberoBot] Webhook deleted"))
  } yield (httpClient, tk)).use(httpClient_tk => {
    implicit val api: Api[F] = BotApi(httpClient_tk._1, baseUrl = s"https://api.telegram.org/bot${httpClient_tk._2}")
    action(new ABarberoBotPolling[F])
  })

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host
  )(implicit log: LogWriter[F]): Resource[F, ABarberoBotWebhook[F]] = for {
    tk <- token[F]
    baseUrl = s"https://api.telegram.org/bot$tk"
    path    = s"/$tk"
    _                     <- Resource.eval(log.info("[ABarberoBot] Delete webook..."))
    deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
    _ <- Resource.eval(
      Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
        new RuntimeException("[ABarberoBot] The delete webhook request failed: " + deleteWebhookResponse.as[String])
      )
    )
    _ <- Resource.eval(log.info("[ABarberoBot] Webhook deleted"))
  } yield {
    implicit val api: Api[F] = BotApi(httpClient, baseUrl = baseUrl)
    new ABarberoBotWebhook[F](
      url = webhookBaseUrl + path,
      path = path
    )
  }
}
