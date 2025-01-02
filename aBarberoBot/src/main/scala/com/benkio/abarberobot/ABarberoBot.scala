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
  override val triggerListUri: Uri                 = ABarberoBot.triggerListUri
  override val triggerFilename: String             = ABarberoBot.triggerFilename
  override val ignoreMessagePrefix: Option[String] = ABarberoBot.ignoreMessagePrefix
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
  val triggerListUri: Uri     = uri"https://github.com/benkio/sBots/blob/master/aBarberoBot/abar_triggers.txt"
  val triggerFilename: String = "abar_triggers.txt"
  val tokenFilename: String   = "abar_ABarberoBot.token"
  val configNamespace: String = "abarDB"

  def messageRepliesAudioData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMp3[F](
      "kimono"
    )(
      mp3"abar_KimonoMaledetto.mp3",
      mp3"abar_KimonoStregato.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "particelle cadaveriche"
    )(
      mp3"abar_ParticelleCadaveriche.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "avrebbe (mai )?immaginato".r.tr(18)
    )(
      mp3"abar_NessunoAvrebbeImmaginato.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "mortalit[aà]".r.tr(9)
    )(
      mp3"abar_Mortalita.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "carta",
      "legno leggero"
    )(
      mp3"abar_LegnoLeggeroCarta.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "stregato"
    )(
      mp3"abar_KimonoStregato.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "maledetto"
    )(
      mp3"abar_Pestifero.mp3",
      mp3"abar_KimonoMaledetto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "pestifero"
    )(
      mp3"abar_Pestifero.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "distrutto",
      "mangiato dai topi",
      "bruciato",
      "sepolto",
      "nel fiume",
      "innondazione"
    )(
      mp3"abar_Distrutto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\bpratica\\b".r.tr(7)
    )(
      mp3"abar_PraticaPocoPatriotticah.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\bferro\\b".r.tr(5),
      "fuoco",
      "acqua bollente",
      "aceto"
    )(
      mp3"abar_FerroFuocoAcquaBollenteAceto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "secolo"
    )(
      mp3"abar_Secolo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "streghe",
      "maghi",
      "draghi",
      "roghi",
    )(
      mp3"abar_Draghi.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "crociate"
    )(
      mp3"abar_Crociate.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "wikipedia"
    )(
      mp3"abar_Wikipedia.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\beccoh\\b".r.tr(5)
    )(
      mp3"abar_Ecco.mp3",
      mp3"abar_Ecco2.mp3",
      mp3"abar_Ecco3.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "maglio",
      "sbriciola",
      "schiaccia"
    )(
      mp3"abar_Maglio.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "chiese",
      "castelli",
      "villaggi",
      "assedi"
    )(
      mp3"abar_Assedi.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "furore",
      "città"
    )(
      mp3"abar_Furore.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "demoni",
      "scatenat"
    )(
      mp3"abar_Demoni.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "sensei"
    )(
      mp3"abar_Sensei.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "miserabile"
    )(
      mp3"abar_Miserabile.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "omicidio",
      "cosa che capita"
    )(
      mp3"abar_CapitaOmicidio.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "cavallo",
      "tiriamo(lo)? giù".r.tr(11),
      "ammazziamolo"
    )(
      mp3"abar_Ammazziamolo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "bruciare",
      "saccheggiare",
      "fuoco"
    )(
      mp3"abar_Bbq.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "cagarelli",
      "feci",
      "cacca"
    )(
      mp3"abar_Homines.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "monsignore",
      "vescovo",
      "in culo"
    )(
      mp3"abar_Monsu.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "ottimismo"
    )(
      mp3"abar_Ottimismo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "latino"
    )(
      mp3"abar_Homines.mp3",
      mp3"abar_Vagdavercustis.mp3",
      mp3"abar_Yersinia.mp3",
      mp3"abar_Culagium.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "radetzky"
    )(
      mp3"abar_Radetzky.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "interrogateli",
      "tortura"
    )(
      mp3"abar_Reinterrogateli.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "i \\bre\\b".r.tr(2),
      "decapita"
    )(
      mp3"abar_Re.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\bascia\\b".r.tr(5),
      "sangue"
    )(
      mp3"abar_Sangue.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "spranga"
    )(
      mp3"abar_Spranga.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "stupidi"
    )(
      mp3"abar_Stupidi.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\bsubito(o|!){2,}".r.tr(6)
    )(
      mp3"abar_Subito.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "vagdavercustis"
    )(
      mp3"abar_Vagdavercustis.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "peste",
      "yersinia"
    )(
      mp3"abar_Yersinia.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "zazzera"
    )(
      mp3"abar_Zazzera.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "metallo"
    )(
      mp3"abar_Metallo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "italiani",
      "arrendetevi"
    )(
      mp3"abar_Taliani.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "c[a]{2,}[z]+[o]+".r.tr(5)
    )(
      mp3"abar_Cazzo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "eresia",
      "riti satanici",
      "rinnegamento di gesù cristo",
      "sputi sulla croce",
      "sodomia",
    )(
      mp3"abar_RitiSataniciSodomia.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "preoccupazione"
    )(
      mp3"abar_Preoccupazione.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "son(o)? tu[t]+e ba[l]+e".r.tr(13)
    )(
      mp3"abar_Bale.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "birra",
    )(
      mp3"abar_Birra.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "a roma",
      "gobeto"
    )(
      mp3"abar_Gobeto.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "mussolini",
      "fascismo",
      "fatto cose buone"
    )(
      mp3"abar_Mussolini.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "premio di maggioranza",
      "legge (acerbo|elettorale)",
      "parlamento",
      "maggioranza assoluta",
      "camicie nere",
      "listone mussolini",
      "(trenta|30) maggio"
    )(
      mp3"abar_LeggeAcerbo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "alla fiera",
      "dei cazzi",
      "e coglioni",
      "affare"
    )(
      mp3"abar_LaVogliaDeiCazzi.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "dichiarazione di guerra",
      "è stata (presentata/consegnata)",
      "palazzo venezia",
      "ambasciatori"
    )(
      mp3"abar_DichiarazioneDiGuerra.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "schiavetti",
      "facevano sesso",
      "me lo sono fatto",
      "divertentissima"
    )(
      mp3"abar_CosaDivertentissima.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "ebrei",
      "bibbia",
      "diaspora",
      "mitologia",
      "religione",
      "antico testamento"
    )(
      mp3"abar_Bibbia.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "excelsior",
      "belle epoque",
      "nel progresso",
      "benessere",
      "\\b1914\\b".r.tr(4),
      "\\b1915\\b".r.tr(4)
    )(
      mp3"abar_BelleEpoqueProgresso.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\bgoti\\b".r.tr(4),
      "siamo (qui|dentro|tanti)".r.tr(9)
    )(
      mp3"abar_Goti.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "d'orleans",
      "ateo",
      "ateismo",
      "irriveren",
      "non crede in",
      "bella figura",
      "ostenta",
      "\\borge\\b".r.tr(4),
      "venerd[iì] santo".r.tr(13),
      "il diavolo"
    )(
      mp3"abar_AteoIrriverente.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "dal mare",
      "una bestia",
      "bestemmia",
      "del(l'orso| leone)",
      "leopardo"
    )(
      mp3"abar_BestiaBestemmie.mp3"
    )
  )

  def messageRepliesGifData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToGif[F](
      "ha ragione"
    )(
      gif"abar_HaRagione.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "seziona",
      "cadaveri",
      "morti"
    )(
      gif"abar_Cadaveri.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "strappa",
      "gli arti",
      "le braccia"
    )(
      gif"abar_Strappare.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "saltare la testa",
      "questa macchina"
    )(
      gif"abar_SaltareLaTesta.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "un po' paura"
    )(
      gif"abar_Paura.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "sega",
      "dov'è"
    )(
      gif"abar_Sega.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "potere",
      "incarichi",
      "poltrone",
      "appalti",
      "spartir"
    )(
      gif"abar_Potere.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "grandioso",
      "magnifico",
      "capolavoro"
    )(
      gif"abar_Capolavoro.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "troppo facile",
      "easy"
    )(
      gif"abar_TroppoFacile.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "chi(s| )se( )?ne( )?frega".r.tr(13)
    )(
      gif"abar_Chissenefrega.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "buonasera"
    )(
      gif"abar_Buonasera.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      " a morte",
      "\\bsi si si\\b".r.tr(4)
    )(
      gif"abar_SisiAMorte.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bfrancesi\\b".r.tr(8)
    )(
      gif"abar_Francesi.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "viva il popolo",
      "comunis"
    )(
      gif"abar_VivaIlPopolo.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "fare qualcosa"
    )(
      gif"abar_FareQualcosa.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "(no|nessun|non c'è) problem(a)?",
      "ammazziamo tutti"
    )(
      gif"abar_AmmazziamoTuttiNoProblem.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bcert[o!]{3,}\\b".r.tr(5)
    )(
      gif"abar_Certo.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "rogo"
    )(
      gif"abar_Rogo.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "semplific"
    )(
      gif"abar_Semplifico.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "bere il (suo )?sangue".r.tr(15),
      "taglia(re)? la gola".r.tr(14)
    )(
      gif"abar_TaglioGolaBereSangue.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "spacco la (testa|faccia)".r.tr(15)
    )(
      gif"abar_SpaccoLaTesta.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "dal (culo|sedere|fondo schiera|orifizio posteriore|dietro)".r.tr(8)
    )(
      gif"abar_OrifizioPosteriore.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "faccia tosta",
      "furfante"
    )(
      gif"abar_Furfante.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bbasta(a|!){2,}".r.tr(5)
    )(
      gif"abar_Basta.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "tutti insieme",
      "ghigliottina"
    )(
      gif"abar_GhigliottinaTuttiInsieme.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "economisti"
    )(
      gif"abar_Economisti.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "vieni (un po' )?qui".r.tr(9)
    )(
      gif"abar_VieniQui.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "si fa così"
    )(
      gif"abar_SiFaCosi.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "rapire",
      "riscatto"
    )(
      gif"abar_Riscatto.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bn[o]+!\\b".r.tr(3),
      "non (lo )?vogli(a|o)".r.tr(10)
    )(
      gif"abar_No.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "in un attimo",
      "in piazza"
    )(
      gif"abar_InPiazza.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "in due pezzi"
    )(
      gif"abar_InDuePezzi.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bgiusto(o|!){2,}".r.tr(6)
    )(
      gif"abar_Giusto.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "gli altri (che )?sono".r.tr(15)
    )(
      gif"abar_GliAltri.gif"
    ),
    ReplyBundleMessage.textToGif[F](
      "👐",
      "🙌",
    )(
      gif"abar_AlzaLeMani.mp4",
    )
  )

  def messageRepliesVideoData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToVideo[F](
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
    ReplyBundleMessage.textToMp3[F](
      "tedesco"
    )(
      mp3"abar_Kraft.mp3",
      mp3"abar_Von_Hohenheim.mp3",
      mp3"abar_Haushofer.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
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
    ReplyBundleMessage.textToMedia[F](
      "pistola",
      "mitragliatrice",
      "fucile da caccia",
      "calibro",
      "beretta",
      "salame",
      "mortadella",
      "provolone",
      "marmellata",
      "burro",
      "(dadi|pomodori) star".r.tr(9),
      "valigett[ae] 24[ ]?ore".r.tr(15),
      "giubbotto anti[ ]?proiettile".r.tr(24),
      "libri (gialli|fantascienza)".r.tr(12),
      "fumetti",
      "charlie brown",
      "documenti d'identità",
      "targhe di auto (rubate)?".r.tr(15),
      "timbri",
      "(divise|palette) della polizia".r.tr(14),
      "pacchetti di sigarette",
      "piselli de rica",
      "fagioli cirio",
      "pasta (buitoni|barilla|corta)".r.tr(11),
      "spaghetti",
    )(
      vid"abar_ListaSpesaPartigiani.mp4",
      mp3"abar_ListaSpesaPartigiani.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tonnellate",
      "zirconio",
      "carbone",
      "acciaio",
      "oli minerali",
      "legname",
      "\\brame\\b".r.tr(4),
      "nitrato di sodio",
      "sali potassici",
      "\\bgomma\\b".r.tr(5),
      "toluolo",
      "trementina",
      "piombo",
      "stagno",
      "nichelio",
      "molibdeno",
      "tungsteno",
      "titanio",
    )(
      mp3"abar_ListaMolibdeno.mp3",
      vid"abar_ListaMolibdeno.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "error[ie]".r.tr(6),
      "pernicios[oi]".r.tr(10),
      "scandalos[oi]".r.tr(10),
      "penstilenzial[ie]".r.tr(14),
      "velenosis[s]+imo".r.tr(13),
    )(
      vid"abar_ErrorePestilenzialeVelenosissimo.mp4",
      mp3"abar_ErrorePestilenzialeVelenosissimo.mp3"
    )
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
    TriggerListCommand.triggerListReplyBundleCommand[F](triggerListUri),
    TriggerSearchCommand.triggerSearchReplyBundleCommand[F](
      botName = botName,
      ignoreMessagePrefix = ABarberoBot.ignoreMessagePrefix,
      mdr = messageRepliesData[F]
    ),
    SearchShowCommand.searchShowReplyBundleCommand(
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
    RandomDataCommand.randomDataReplyBundleCommand[F](
      botPrefix = botPrefix,
      dbMedia = dbLayer.dbMedia
    ),
    InstructionsCommand.instructionsReplyBundleCommand[F](
      botName = botName,
      ignoreMessagePrefix = ABarberoBot.ignoreMessagePrefix,
      commandDescriptionsIta = List(
        TriggerListCommand.triggerListCommandDescriptionIta,
        TriggerSearchCommand.triggerSearchCommandDescriptionIta,
        SearchShowCommand.searchShowCommandIta,
        StatisticsCommands.topTwentyTriggersCommandDescriptionIta,
        SubscribeUnsubscribeCommand.subscribeCommandDescriptionIta,
        SubscribeUnsubscribeCommand.unsubscribeCommandDescriptionIta,
        SubscribeUnsubscribeCommand.subscriptionsCommandDescriptionIta,
        TimeoutCommand.timeoutCommandDescriptionIta,
        RandomDataCommand.randomDataCommandIta
      ),
      commandDescriptionsEng = List(
        TriggerListCommand.triggerListCommandDescriptionEng,
        TriggerSearchCommand.triggerSearchCommandDescriptionEng,
        SearchShowCommand.searchShowCommandEng,
        StatisticsCommands.topTwentyTriggersCommandDescriptionEng,
        SubscribeUnsubscribeCommand.subscribeCommandDescriptionEng,
        SubscribeUnsubscribeCommand.unsubscribeCommandDescriptionEng,
        SubscribeUnsubscribeCommand.subscriptionsCommandDescriptionEng,
        TimeoutCommand.timeoutCommandDescriptionEng,
        RandomDataCommand.randomDataCommandEng
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
