package com.benkio.abarberobot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.default.Actions._
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns._
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.web.UrlFetcher
import com.benkio.telegrambotinfrastructure.BotOps
import com.benkio.telegrambotinfrastructure._
import doobie.Transactor
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.ember.client._
import org.http4s.implicits._
import org.http4s.Status
import org.http4s.Uri
import telegramium.bots.high._

class ABarberoBotPolling[F[_]: Parallel: Async: Action: Api: LogWriter](
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F]
    with ABarberoBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = resAccess
}

class ABarberoBotWebhook[F[_]: Async: Api: Action: LogWriter](
    uri: Uri,
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/"
) extends BotSkeletonWebhook[F](uri, path)
    with ABarberoBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = resAccess
}

trait ABarberoBot[F[_]] extends BotSkeleton[F] {

  override val botName: String                     = ABarberoBot.botName
  override val botPrefix: String                   = ABarberoBot.botPrefix
  override val ignoreMessagePrefix: Option[String] = ABarberoBot.ignoreMessagePrefix
  val linkSources                                  = ABarberoBot.linkSources
  val backgroundJobManager: BackgroundJobManager[F]

  override def messageRepliesDataF(implicit
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    ABarberoBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(implicit asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    ABarberoBot
      .commandRepliesData[F](
        resourceAccess,
        backgroundJobManager,
        dbLayer,
        linkSources
      )
      .pure[F]
}

object ABarberoBot extends BotOps {

  val ignoreMessagePrefix: Option[String] = Some("!")
  val botName: String                     = "ABarberoBot"
  val botPrefix: String                   = "abar"
  val triggerListUrl: Uri = uri"https://github.com/benkio/myTelegramBot/blob/master/aBarberoBot/abar_triggers.txt"
  val linkSources: String = "abar_LinkSources"

  def messageRepliesAudioData[F[_]]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("kimono")
      ),
      List(
        MediaFile("abar_KimonoMaledetto.mp3"),
        MediaFile("abar_KimonoStregato.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("particelle cadaveriche")
      ),
      List(
        MediaFile("abar_ParticelleCadaveriche.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("avrebbe (mai )?immaginato".r, 18)
      ),
      List(
        MediaFile("abar_NessunoAvrebbeImmaginato.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("mortalit[aà]".r, 9)
      ),
      List(
        MediaFile("abar_Mortalita.mp3")
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("carta"),
        StringTextTriggerValue("legno leggero")
      ),
      List(
        MediaFile("abar_LegnoLeggeroCarta.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stregato")
      ),
      List(
        MediaFile("abar_KimonoStregato.mp3")
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("maledetto")
      ),
      List(
        MediaFile("abar_Pestifero.mp3"),
        MediaFile("abar_KimonoMaledetto.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("pestifero")
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
        RegexTextTriggerValue("\\bpratica\\b".r, 7)
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
        RegexTextTriggerValue("\\beccoh\\b".r, 5)
      ),
      List(
        MediaFile("abar_Ecco.mp3"),
        MediaFile("abar_Ecco2.mp3"),
        MediaFile("abar_Ecco3.mp3")
      ),
      replySelection = RandomSelection
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
        RegexTextTriggerValue("tiriamo(lo)? giù".r, 11),
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
        RegexTextTriggerValue("\\bre\\b".r, 2),
        StringTextTriggerValue("decapita")
      ),
      List(
        MediaFile("abar_Re.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bascia\\b".r, 5),
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
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("italiani"),
        StringTextTriggerValue("arrendetevi")
      ),
      List(
        MediaFile("abar_Taliani.mp3")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("c[a]{2,}[z]+[o]+".r, 5)
      ),
      List(
        MediaFile("abar_Cazzo.mp3")
      )
    )
  )

  def messageRepliesGifData[F[_]]: List[ReplyBundleMessage[F]] = List(
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
        RegexTextTriggerValue("chi(s| )se( )?ne( )?frega".r, 13)
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
        RegexTextTriggerValue("(si| si|si ){2,}".r, 4)
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
        RegexTextTriggerValue("\\bcerto\\b".r, 5)
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
        RegexTextTriggerValue("taglia(re)? la gola".r, 14)
      ),
      List(
        MediaFile("abar_TaglioGolaBereSangue.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("spacco la (testa|faccia)".r, 15)
      ),
      List(
        MediaFile("abar_SpaccoLaTesta.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("dal (culo|sedere|fondo schiera|orifizio posteriore|dietro)".r, 8)
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
        RegexTextTriggerValue("vieni (un po' )?qui".r, 9)
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
        RegexTextTriggerValue("\\bn[o]+!\\b".r, 3),
        RegexTextTriggerValue("non (lo )?vogli(a|o)".r, 10)
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

  def messageRepliesSpecialData[F[_]]: List[ReplyBundleMessage[F]] = List(
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
        RegexTextTriggerValue("(figlio|fijo) (di|de) (mignotta|puttana|troia)".r, 13)
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

  def messageRepliesData[F[_]]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesSpecialData[F])
      .sorted(ReplyBundle.orderingInstance[F])
      .reverse

  def commandRepliesData[F[_]: Async](
      resourceAccess: ResourceAccess[F],
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F],
      linkSources: String
  )(implicit
      log: LogWriter[F]
  ): List[ReplyBundleCommand[F]] = List(
    TriggerListCommand.triggerListReplyBundleCommand[F](triggerListUrl),
    TriggerSearchCommand.triggerSearchReplyBundleCommand[F](
      botName = botName,
      ignoreMessagePrefix = ignoreMessagePrefix,
      mdr = messageRepliesData[F]
    ),
    RandomLinkCommand.selectRandomLinkReplyBundleCommand(
      resourceAccess = resourceAccess,
      youtubeLinkSources = linkSources
    ),
    RandomLinkCommand.selectRandomLinkByKeywordsReplyBundleCommand(
      resourceAccess = resourceAccess,
      botName = botName,
      youtubeLinkSources = linkSources
    ),
    StatisticsCommands.topTwentyReplyBundleCommand[F](
      botPrefix = botPrefix,
      dbMedia = dbLayer.dbMedia
    ),
    SubscribeUnsubscribeCommand.subscribeReplyBundleCommand[F](
      backgroundJobManager = backgroundJobManager,
      botName = botName
    ),
    SubscribeUnsubscribeCommand.unsubscribeReplyBundleCommand[F](
      backgroundJobManager = backgroundJobManager,
      botName = botName
    ),
    InstructionsCommand.instructionsReplyBundleCommand[F](
      botName = botName,
      ignoreMessagePrefix = ignoreMessagePrefix,
      commandDescriptionsIta = List(
        TriggerListCommand.triggerListCommandDescriptionIta,
        TriggerSearchCommand.triggerSearchCommandDescriptionIta,
        RandomLinkCommand.randomLinkCommandDescriptionIta,
        RandomLinkCommand.randomLinkKeywordCommandIta,
        StatisticsCommands.topTwentyTriggersCommandDescriptionIta,
        SubscribeUnsubscribeCommand.subscribeCommandDescriptionIta,
        SubscribeUnsubscribeCommand.unsubscribeCommandDescriptionIta
      ),
      commandDescriptionsEng = List(
        TriggerListCommand.triggerListCommandDescriptionEng,
        TriggerSearchCommand.triggerSearchCommandDescriptionEng,
        RandomLinkCommand.randomLinkCommandDescriptionEng,
        RandomLinkCommand.randomLinkKeywordCommandEng,
        StatisticsCommands.topTwentyTriggersCommandDescriptionEng,
        SubscribeUnsubscribeCommand.subscribeCommandDescriptionEng,
        SubscribeUnsubscribeCommand.unsubscribeCommandDescriptionEng
      )
    ),
  )

  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess.fromResources.getResourceByteArray("abar_ABarberoBot.token").map(_.map(_.toChar).mkString)

  final case class BotSetup[F[_]](
      token: String,
      httpClient: Client[F],
      resourceAccess: ResourceAccess[F],
      dbLayer: DBLayer[F]
  )

  def buildCommonBot[F[_]: Async](
      httpClient: Client[F]
  )(implicit log: LogWriter[F]): Resource[F, BotSetup[F]] = for {
    tk     <- token[F]
    config <- Resource.eval(Config.loadConfig[F])
    _      <- Resource.eval(log.info(s"[$botName] Configuration: $config"))
    transactor = Transactor.fromDriverManager[F](
      config.driver,
      config.url,
      "",
      ""
    )
    urlFetcher <- Resource.eval(UrlFetcher[F](httpClient))
    dbLayer    <- Resource.eval(DBLayer[F](transactor))
    resourceAccess = ResourceAccess.dbResources[F](dbLayer.dbMedia, urlFetcher)
    _                     <- Resource.eval(log.info(s"[$botName] Delete webook..."))
    deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
    _ <- Resource.eval(
      Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
        new RuntimeException(s"[$botName] The delete webhook request failed: " + deleteWebhookResponse.as[String])
      )
    )
    _ <- Resource.eval(log.info(s"[$botName] Webhook deleted"))
  } yield BotSetup[F](
    token = tk,
    httpClient = httpClient,
    resourceAccess = resourceAccess,
    dbLayer = dbLayer
  )

  def buildPollingBot[F[_]: Parallel: Async: LogWriter, A](
      action: ABarberoBotPolling[F] => F[A]
  ): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].build
    botSetup   <- buildCommonBot[F](httpClient)
  } yield botSetup).use(botSetup => {
    implicit val api: Api[F] =
      BotApi(botSetup.httpClient, baseUrl = s"https://api.telegram.org/bot${botSetup.token}")
    implicit val ra: ResourceAccess[F] = botSetup.resourceAccess
    for {
      backgroundJobManager <- BackgroundJobManager[F](
        dbSubscription = botSetup.dbLayer.dbSubscription,
        resourceAccess = botSetup.resourceAccess,
        youtubeLinkSources = ABarberoBot.linkSources,
        botName = ABarberoBot.botName
      )
      result <- action(
        new ABarberoBotPolling[F](
          resAccess = botSetup.resourceAccess,
          dbLayer = botSetup.dbLayer,
          backgroundJobManager = backgroundJobManager
        )
      )
    } yield result

  })

  def buildWebhookBot[F[_]: Async: LogWriter](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host
  ): Resource[F, ABarberoBotWebhook[F]] = for {
    botSetup <- buildCommonBot[F](httpClient)
    baseUrl  <- Resource.eval(Async[F].fromEither(Uri.fromString(s"https://api.telegram.org/bot${botSetup.token}")))
    path     <- Resource.eval(Async[F].fromEither(Uri.fromString(s"/${botSetup.token}")))
    webhookBaseUri <- Resource.eval(Async[F].fromEither(Uri.fromString(webhookBaseUrl + path)))
    api       = BotApi(httpClient, baseUrl = baseUrl.renderString)
    resAccess = botSetup.resourceAccess
    backgroundJobManager <- {
      implicit val implApi: Api[F] = api
      implicit val implResAccess   = resAccess
      Resource.eval(
        BackgroundJobManager[F](
          dbSubscription = botSetup.dbLayer.dbSubscription,
          resourceAccess = botSetup.resourceAccess,
          youtubeLinkSources = ABarberoBot.linkSources,
          botName = ABarberoBot.botName
        )
      )
    }
  } yield {
    implicit val implApi: Api[F] = api
    implicit val implResAccess   = resAccess
    new ABarberoBotWebhook[F](
      uri = webhookBaseUri,
      resAccess = botSetup.resourceAccess,
      backgroundJobManager = backgroundJobManager,
      dbLayer = botSetup.dbLayer,
      path = path
    )
  }
}
