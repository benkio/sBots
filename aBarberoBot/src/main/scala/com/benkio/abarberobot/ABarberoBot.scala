package com.benkio.abarberobot

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.*
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.model.*
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.*
import com.benkio.telegrambotinfrastructure.patterns.PostComputationPatterns
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.ember.client.*
import org.http4s.implicits.*
import telegramium.bots.high.*
import telegramium.bots.InputPartFile
import telegramium.bots.Message

class ABarberoBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    resourceAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F](resourceAccess)
    with ABarberoBot[F] {
  override def resourceAccess(using syncF: Sync[F]): ResourceAccess[F] = resourceAccess
  override def postComputation(using appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(using
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

class ABarberoBotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    resourceAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends BotSkeletonWebhook[F](uri, path, webhookCertificate, resourceAccess)
    with ABarberoBot[F] {
  override def resourceAccess(using syncF: Sync[F]): ResourceAccess[F] = resourceAccess
  override def postComputation(using appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(using
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

trait ABarberoBot[F[_]] extends BotSkeleton[F] {

  override val botName: String                     = ABarberoBot.botName
  override val botPrefix: String                   = ABarberoBot.botPrefix
  override val ignoreMessagePrefix: Option[String] = ABarberoBot.ignoreMessagePrefix
  val linkSources                                  = ABarberoBot.linkSources
  val backgroundJobManager: BackgroundJobManager[F]

  override def messageRepliesDataF(using
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    ABarberoBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(using asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    ABarberoBot
      .commandRepliesData[F](
        backgroundJobManager,
        dbLayer
      )
      .pure[F]
}

object ABarberoBot {

  val ignoreMessagePrefix: Option[String] = Some("!")
  val botName: String                     = "ABarberoBot"
  val botPrefix: String                   = "abar"
  val triggerListUrl: Uri     = uri"https://github.com/benkio/sBots/blob/master/aBarberoBot/abar_triggers.txt"
  val linkSources: String     = "abar_LinkSources"
  val tokenFilename: String   = "abar_ABarberoBot.token"
  val configNamespace: String = "abarDB"

  def messageRepliesAudioData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "kimono"
    )(
      mp3"abar_KimonoMaledetto.mp3",
      mp3"abar_KimonoStregato.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "particelle cadaveriche"
    )(
      mp3"abar_ParticelleCadaveriche.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "avrebbe (mai )?immaginato".r.tr(18)
    )(
      mp3"abar_NessunoAvrebbeImmaginato.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mortalit[aà]".r.tr(9)
    )(
      mp3"abar_Mortalita.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "carta",
      "legno leggero"
    )(
      mp3"abar_LegnoLeggeroCarta.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stregato"
    )(
      mp3"abar_KimonoStregato.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "maledetto"
    )(
      mp3"abar_Pestifero.mp3",
      mp3"abar_KimonoMaledetto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "pestifero"
    )(
      mp3"abar_Pestifero.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "distrutto",
      "mangiato dai topi",
      "bruciato",
      "sepolto",
      "nel fiume",
      "innondazione"
    )(
      mp3"abar_Distrutto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bpratica\\b".r.tr(7)
    )(
      mp3"abar_PraticaPocoPatriotticah.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bferro\\b".r.tr(5),
      "fuoco",
      "acqua bollente",
      "aceto"
    )(
      mp3"abar_FerroFuocoAcquaBollenteAceto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "secolo"
    )(
      mp3"abar_Secolo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "streghe",
      "maghi",
      "draghi",
      "roghi",
    )(
      mp3"abar_Draghi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "crociate"
    )(
      mp3"abar_Crociate.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "wikipedia"
    )(
      mp3"abar_Wikipedia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\beccoh\\b".r.tr(5)
    )(
      mp3"abar_Ecco.mp3",
      mp3"abar_Ecco2.mp3",
      mp3"abar_Ecco3.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "maglio",
      "sbriciola",
      "schiaccia"
    )(
      mp3"abar_Maglio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "chiese",
      "castelli",
      "villaggi",
      "assedi"
    )(
      mp3"abar_Assedi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "furore",
      "città"
    )(
      mp3"abar_Furore.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "demoni",
      "scatenat"
    )(
      mp3"abar_Demoni.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sensei"
    )(
      mp3"abar_Sensei.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "miserabile"
    )(
      mp3"abar_Miserabile.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "omicidio",
      "cosa che capita"
    )(
      mp3"abar_CapitaOmicidio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cavallo",
      "tiriamo(lo)? giù".r.tr(11),
      "ammazziamolo"
    )(
      mp3"abar_Ammazziamolo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "bruciare",
      "saccheggiare",
      "fuoco"
    )(
      mp3"abar_Bbq.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cagarelli",
      "feci",
      "cacca"
    )(
      mp3"abar_Homines.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "monsignore",
      "vescovo",
      "in culo"
    )(
      mp3"abar_Monsu.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ottimismo"
    )(
      mp3"abar_Ottimismo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "latino"
    )(
      mp3"abar_Homines.mp3",
      mp3"abar_Vagdavercustis.mp3",
      mp3"abar_Yersinia.mp3",
      mp3"abar_Culagium.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "radetzky"
    )(
      mp3"abar_Radetzky.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "interrogateli",
      "tortura"
    )(
      mp3"abar_Reinterrogateli.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "i \\bre\\b".r.tr(2),
      "decapita"
    )(
      mp3"abar_Re.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bascia\\b".r.tr(5),
      "sangue"
    )(
      mp3"abar_Sangue.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "spranga"
    )(
      mp3"abar_Spranga.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stupidi"
    )(
      mp3"abar_Stupidi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bsubito(o|!){2,}".r.tr(6)
    )(
      mp3"abar_Subito.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vagdavercustis"
    )(
      mp3"abar_Vagdavercustis.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "peste",
      "yersinia"
    )(
      mp3"abar_Yersinia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "zazzera"
    )(
      mp3"abar_Zazzera.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "metallo"
    )(
      mp3"abar_Metallo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "italiani",
      "arrendetevi"
    )(
      mp3"abar_Taliani.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "c[a]{2,}[z]+[o]+".r.tr(5)
    )(
      mp3"abar_Cazzo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "eresia",
      "riti satanici",
      "rinnegamento di gesù cristo",
      "sputi sulla croce",
      "sodomia",
    )(
      mp3"abar_RitiSataniciSodomia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "preoccupazione"
    )(
      mp3"abar_Preoccupazione.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "son(o)? tu[t]+e ba[l]+e".r.tr(13)
    )(
      mp3"abar_Bale.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "birra",
    )(
      mp3"abar_Birra.mp3"
    ),
  )

  def messageRepliesGifData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "ha ragione"
    )(
      gif"abar_HaRagione.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "seziona",
      "cadaveri",
      "morti"
    )(
      gif"abar_Cadaveri.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "strappa",
      "gli arti",
      "le braccia"
    )(
      gif"abar_Strappare.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "saltare la testa",
      "questa macchina"
    )(
      gif"abar_SaltareLaTesta.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "un po' paura"
    )(
      gif"abar_Paura.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sega",
      "dov'è"
    )(
      gif"abar_Sega.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "potere",
      "incarichi",
      "poltrone",
      "appalti",
      "spartir"
    )(
      gif"abar_Potere.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "grandioso",
      "magnifico",
      "capolavoro"
    )(
      gif"abar_Capolavoro.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "troppo facile",
      "easy"
    )(
      gif"abar_TroppoFacile.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "chi(s| )se( )?ne( )?frega".r.tr(13)
    )(
      gif"abar_Chissenefrega.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "buonasera"
    )(
      gif"abar_Buonasera.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      " a morte",
      "\\bsi si si\\b".r.tr(4)
    )(
      gif"abar_SisiAMorte.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "francesi"
    )(
      gif"abar_Francesi.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "viva il popolo",
      "comunis"
    )(
      gif"abar_VivaIlPopolo.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fare qualcosa"
    )(
      gif"abar_FareQualcosa.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(no|nessun|non c'è) problem(a)?",
      "ammazziamo tutti"
    )(
      gif"abar_AmmazziamoTuttiNoProblem.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bcert[o!]{3,}\\b".r.tr(5)
    )(
      gif"abar_Certo.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "rogo"
    )(
      gif"abar_Rogo.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "semplific"
    )(
      gif"abar_Semplifico.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "bere il (suo )?sangue".r.tr(15),
      "taglia(re)? la gola".r.tr(14)
    )(
      gif"abar_TaglioGolaBereSangue.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "spacco la (testa|faccia)".r.tr(15)
    )(
      gif"abar_SpaccoLaTesta.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dal (culo|sedere|fondo schiera|orifizio posteriore|dietro)".r.tr(8)
    )(
      gif"abar_OrifizioPosteriore.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "faccia tosta",
      "furfante"
    )(
      gif"abar_Furfante.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bbasta(a|!){2,}".r.tr(5)
    )(
      gif"abar_Basta.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tutti insieme",
      "ghigliottina"
    )(
      gif"abar_GhigliottinaTuttiInsieme.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "economisti"
    )(
      gif"abar_Economisti.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vieni (un po' )?qui".r.tr(9)
    )(
      gif"abar_VieniQui.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "si fa così"
    )(
      gif"abar_SiFaCosi.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "rapire",
      "riscatto"
    )(
      gif"abar_Riscatto.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bn[o]+!\\b".r.tr(3),
      "non (lo )?vogli(a|o)".r.tr(10)
    )(
      gif"abar_No.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "in un attimo",
      "in piazza"
    )(
      gif"abar_InPiazza.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "in due pezzi"
    )(
      gif"abar_InDuePezzi.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bgiusto(o|!){2,}".r.tr(6)
    )(
      gif"abar_Giusto.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "gli altri (che )?sono".r.tr(15)
    )(
      gif"abar_GliAltri.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "👐",
      "🙌",
    )(
      vid"abar_AlzaLeMani.mp4",
    )
  )

  def messageRepliesVideoData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "parole longobarde",
      "zuffa",
      "spaccare",
      "arraffare",
      "tanfo",
    )(
      vid"abar_ParoleLongobarde.mp4",
    ),
  )

  def messageRepliesSpecialData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "tedesco"
    )(
      mp3"abar_Kraft.mp3",
      mp3"abar_Von_Hohenheim.mp3",
      mp3"abar_Haushofer.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "genitali",
      "cosi e coglioni"
    )(
      mp3"abar_Cosi.mp3",
      mp3"abar_Sottaceto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(figlio|fijo) (di|de) (mignotta|puttana|troia)".r.tr(13)
    )(
      gif"abar_FiglioDi.gif",
      gif"abar_FiglioDi2.gif",
      mp3"abar_FiglioDi3.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sgozza"
    )(
      mp3"abar_Sgozzamento.mp3",
      gif"abar_Sgozzamento.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "bruciargli",
      "la casa"
    )(
      mp3"abar_Bruciare.mp3",
      gif"abar_Bruciare.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "a pezzi",
      "a pezzettini"
    )(
      mp3"abar_APezzettini.mp3",
      gif"abar_APezzettini.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "allarme",
      "priori",
      "carne"
    )(
      mp3"abar_Priori.mp3",
      gif"abar_Priori.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "zagaglia",
      "nemico"
    )(
      mp3"abar_Zagaglia.mp3",
      gif"abar_Zagaglia.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "guerra",
    )(
      vid"abar_ParoleLongobarde.mp4",
      mp3"abar_Guerra.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "faida",
    )(
      vid"abar_ParoleLongobarde.mp4",
      gif"abar_Faida.gif",
    ),
    ReplyBundleMessage.textToMedia[F](
      "spranga"
    )(
      gif"abar_Spranga.gif",
      vid"abar_ParoleLongobarde.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "trappola"
    )(
      gif"abar_Trappola.gif",
      vid"abar_ParoleLongobarde.mp4"
    ),
  )

  def messageRepliesData[F[_]: Applicative]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesVideoData[F] ++ messageRepliesSpecialData[
      F
    ])
      .sorted(ReplyBundle.orderingInstance[F])
      .reverse

  def commandRepliesData[F[_]: Async](
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F]
  )(using
      log: LogWriter[F]
  ): List[ReplyBundleCommand[F]] = List(
    TriggerListCommand.triggerListReplyBundleCommand[F](triggerListUrl),
    TriggerSearchCommand.triggerSearchReplyBundleCommand[F](
      botName = botName,
      ignoreMessagePrefix = ignoreMessagePrefix,
      mdr = messageRepliesData[F]
    ),
    RandomLinkCommand.searchShowReplyBundleCommand(
      botName = botName,
      dbShow = dbLayer.dbShow
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
    SubscribeUnsubscribeCommand.subscriptionsReplyBundleCommand[F](
      dbSubscription = dbLayer.dbSubscription,
      backgroundJobManager = backgroundJobManager,
      botName = botName
    ),
    TimeoutCommand.timeoutReplyBundleCommand[F](
      botName = botName,
      dbTimeout = dbLayer.dbTimeout,
      log = log
    ),
    InstructionsCommand.instructionsReplyBundleCommand[F](
      botName = botName,
      ignoreMessagePrefix = ignoreMessagePrefix,
      commandDescriptionsIta = List(
        TriggerListCommand.triggerListCommandDescriptionIta,
        TriggerSearchCommand.triggerSearchCommandDescriptionIta,
        RandomLinkCommand.searchShowCommandIta,
        StatisticsCommands.topTwentyTriggersCommandDescriptionIta,
        SubscribeUnsubscribeCommand.subscribeCommandDescriptionIta,
        SubscribeUnsubscribeCommand.unsubscribeCommandDescriptionIta,
        SubscribeUnsubscribeCommand.subscriptionsCommandDescriptionIta,
        TimeoutCommand.timeoutCommandDescriptionIta
      ),
      commandDescriptionsEng = List(
        TriggerListCommand.triggerListCommandDescriptionEng,
        TriggerSearchCommand.triggerSearchCommandDescriptionEng,
        RandomLinkCommand.searchShowCommandEng,
        StatisticsCommands.topTwentyTriggersCommandDescriptionEng,
        SubscribeUnsubscribeCommand.subscribeCommandDescriptionEng,
        SubscribeUnsubscribeCommand.unsubscribeCommandDescriptionEng,
        SubscribeUnsubscribeCommand.subscriptionsCommandDescriptionEng,
        TimeoutCommand.timeoutCommandDescriptionEng
      ),
    )
  )

  def buildPollingBot[F[_]: Parallel: Async: Network, A](
      action: ABarberoBotPolling[F] => F[A]
  )(using log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
    botSetup <- BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botName = botName
    )
  } yield botSetup).use { botSetup =>
    action(
      new ABarberoBotPolling[F](
        resourceAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager
      )(using Parallel[F], Async[F], botSetup.api, log)
    )
  }

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, ABarberoBotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botName = botName,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new ABarberoBotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        resourceAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(using Async[F], botSetup.api, log)
    }
}
