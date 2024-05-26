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
      mf"abar_KimonoMaledetto.mp3",
      mf"abar_KimonoStregato.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "particelle cadaveriche"
    )(
      mf"abar_ParticelleCadaveriche.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "avrebbe (mai )?immaginato".r.tr(18)
    )(
      mf"abar_NessunoAvrebbeImmaginato.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mortalit[aà]".r.tr(9)
    )(
      mf"abar_Mortalita.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "carta",
      "legno leggero"
    )(
      mf"abar_LegnoLeggeroCarta.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stregato"
    )(
      mf"abar_KimonoStregato.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "maledetto"
    )(
      mf"abar_Pestifero.mp3",
      mf"abar_KimonoMaledetto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "pestifero"
    )(
      mf"abar_Pestifero.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "distrutto",
      "mangiato dai topi",
      "bruciato",
      "sepolto",
      "nel fiume",
      "innondazione"
    )(
      mf"abar_Distrutto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bpratica\\b".r.tr(7)
    )(
      mf"abar_PraticaPocoPatriotticah.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ferro",
      "fuoco",
      "acqua bollente",
      "aceto"
    )(
      mf"abar_FerroFuocoAcquaBollenteAceto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "secolo"
    )(
      mf"abar_Secolo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "streghe",
      "maghi",
      "draghi",
      "roghi",
    )(
      mf"abar_Draghi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "crociate"
    )(
      mf"abar_Crociate.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "wikipedia"
    )(
      mf"abar_Wikipedia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\beccoh\\b".r.tr(5)
    )(
      mf"abar_Ecco.mp3",
      mf"abar_Ecco2.mp3",
      mf"abar_Ecco3.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "maglio",
      "sbriciola",
      "schiaccia"
    )(
      mf"abar_Maglio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "chiese",
      "castelli",
      "villaggi",
      "assedi"
    )(
      mf"abar_Assedi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "furore",
      "città"
    )(
      mf"abar_Furore.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "demoni",
      "scatenat"
    )(
      mf"abar_Demoni.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sensei"
    )(
      mf"abar_Sensei.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "miserabile"
    )(
      mf"abar_Miserabile.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "omicidio",
      "cosa che capita"
    )(
      mf"abar_CapitaOmicidio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cavallo",
      "tiriamo(lo)? giù".r.tr(11),
      "ammazziamolo"
    )(
      mf"abar_Ammazziamolo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "bruciare",
      "saccheggiare",
      "fuoco"
    )(
      mf"abar_Bbq.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cagarelli",
      "feci",
      "cacca"
    )(
      mf"abar_Homines.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "monsignore",
      "vescovo",
      "in culo"
    )(
      mf"abar_Monsu.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ottimismo"
    )(
      mf"abar_Ottimismo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "latino"
    )(
      mf"abar_Homines.mp3",
      mf"abar_Vagdavercustis.mp3",
      mf"abar_Yersinia.mp3",
      mf"abar_Culagium.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "radetzky"
    )(
      mf"abar_Radetzky.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "interrogateli",
      "tortura"
    )(
      mf"abar_Reinterrogateli.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "i \\bre\\b".r.tr(2),
      "decapita"
    )(
      mf"abar_Re.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bascia\\b".r.tr(5),
      "sangue"
    )(
      mf"abar_Sangue.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "spranga"
    )(
      mf"abar_Spranga.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stupidi"
    )(
      mf"abar_Stupidi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bsubito(o|!){2,}".r.tr(6)
    )(
      mf"abar_Subito.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vagdavercustis"
    )(
      mf"abar_Vagdavercustis.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "peste",
      "yersinia"
    )(
      mf"abar_Yersinia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "zazzera"
    )(
      mf"abar_Zazzera.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "metallo"
    )(
      mf"abar_Metallo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "italiani",
      "arrendetevi"
    )(
      mf"abar_Taliani.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "c[a]{2,}[z]+[o]+".r.tr(5)
    )(
      mf"abar_Cazzo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "eresia",
      "riti satanici",
      "rinnegamento di gesù cristo",
      "sputi sulla croce",
      "sodomia",
    )(
      mf"abar_RitiSataniciSodomia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "preoccupazione"
    )(
      mf"abar_Preoccupazione.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "son(o)? tu[t]+e ba[l]+e".r.tr(13)
    )(
      mf"abar_Bale.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "birra",
    )(
      mf"abar_Birra.mp3"
    ),
  )

  def messageRepliesGifData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "ha ragione"
    )(
      mf"abar_HaRagione.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "seziona",
      "cadaveri",
      "morti"
    )(
      mf"abar_Cadaveri.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "strappa",
      "gli arti",
      "le braccia"
    )(
      mf"abar_Strappare.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "saltare la testa",
      "questa macchina"
    )(
      mf"abar_SaltareLaTesta.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "un po' paura"
    )(
      mf"abar_Paura.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sega",
      "dov'è"
    )(
      mf"abar_Sega.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "potere",
      "incarichi",
      "poltrone",
      "appalti",
      "spartir"
    )(
      mf"abar_Potere.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "grandioso",
      "magnifico",
      "capolavoro"
    )(
      mf"abar_Capolavoro.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "troppo facile",
      "easy"
    )(
      mf"abar_TroppoFacile.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "chi(s| )se( )?ne( )?frega".r.tr(13)
    )(
      mf"abar_Chissenefrega.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "buonasera"
    )(
      mf"abar_Buonasera.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      " a morte",
      "\\bsi si si\\b".r.tr(4)
    )(
      mf"abar_SisiAMorte.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "francesi"
    )(
      mf"abar_Francesi.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "viva il popolo",
      "comunis"
    )(
      mf"abar_VivaIlPopolo.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fare qualcosa"
    )(
      mf"abar_FareQualcosa.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(no|nessun|non c'è) problem(a)?",
      "ammazziamo tutti"
    )(
      mf"abar_AmmazziamoTuttiNoProblem.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bcert[o!]{3,}\\b".r.tr(5)
    )(
      mf"abar_Certo.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "rogo"
    )(
      mf"abar_Rogo.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "semplific"
    )(
      mf"abar_Semplifico.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "bere il (suo )?sangue".r.tr(15),
      "taglia(re)? la gola".r.tr(14)
    )(
      mf"abar_TaglioGolaBereSangue.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "spacco la (testa|faccia)".r.tr(15)
    )(
      mf"abar_SpaccoLaTesta.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dal (culo|sedere|fondo schiera|orifizio posteriore|dietro)".r.tr(8)
    )(
      mf"abar_OrifizioPosteriore.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "faccia tosta",
      "furfante"
    )(
      mf"abar_Furfante.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bbasta(a|!){2,}".r.tr(5)
    )(
      mf"abar_Basta.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tutti insieme",
      "ghigliottina"
    )(
      mf"abar_GhigliottinaTuttiInsieme.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "economisti"
    )(
      mf"abar_Economisti.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vieni (un po' )?qui".r.tr(9)
    )(
      mf"abar_VieniQui.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "si fa così"
    )(
      mf"abar_SiFaCosi.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "rapire",
      "riscatto"
    )(
      mf"abar_Riscatto.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bn[o]+!\\b".r.tr(3),
      "non (lo )?vogli(a|o)".r.tr(10)
    )(
      mf"abar_No.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "in un attimo",
      "in piazza"
    )(
      mf"abar_InPiazza.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "in due pezzi"
    )(
      mf"abar_InDuePezzi.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bgiusto(o|!){2,}".r.tr(6)
    )(
      mf"abar_Giusto.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "gli altri (che )?sono".r.tr(15)
    )(
      mf"abar_GliAltri.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "👐",
      "🙌",
    )(
      mf"abar_AlzaLeMani.mp4",
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
      mf"abar_ParoleLongobarde.mp4",
    ),
  )

  def messageRepliesSpecialData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "tedesco"
    )(
      mf"abar_Kraft.mp3",
      mf"abar_Von_Hohenheim.mp3",
      mf"abar_Haushofer.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "genitali",
      "cosi e coglioni"
    )(
      mf"abar_Cosi.mp3",
      mf"abar_Sottaceto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(figlio|fijo) (di|de) (mignotta|puttana|troia)".r.tr(13)
    )(
      mf"abar_FiglioDi.gif",
      mf"abar_FiglioDi2.gif",
      mf"abar_FiglioDi3.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sgozza"
    )(
      mf"abar_Sgozzamento.mp3",
      mf"abar_Sgozzamento.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "bruciargli",
      "la casa"
    )(
      mf"abar_Bruciare.mp3",
      mf"abar_Bruciare.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "a pezzi",
      "a pezzettini"
    )(
      mf"abar_APezzettini.mp3",
      mf"abar_APezzettini.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "allarme",
      "priori",
      "carne"
    )(
      mf"abar_Priori.mp3",
      mf"abar_Priori.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "zagaglia",
      "nemico"
    )(
      mf"abar_Zagaglia.mp3",
      mf"abar_Zagaglia.gif"
    ),
    ReplyBundleMessage.textToMedia[F](
      "guerra",
    )(
      mf"abar_ParoleLongobarde.mp4",
      mf"abar_Guerra.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "faida",
    )(
      mf"abar_ParoleLongobarde.mp4",
      mf"abar_Faida.gif",
    ),
    ReplyBundleMessage.textToMedia[F](
      "spranga"
    )(
      mf"abar_Spranga.gif",
      mf"abar_ParoleLongobarde.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "trappola"
    )(
      mf"abar_Trappola.gif",
      mf"abar_ParoleLongobarde.mp4"
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
