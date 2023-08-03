package com.benkio.abarberobot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns._
import com.benkio.telegrambotinfrastructure.patterns.PostComputationPatterns
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.ember.client._
import org.http4s.implicits._
import telegramium.bots.high._
import telegramium.bots.InputPartFile
import telegramium.bots.Message

class ABarberoBotPolling[F[_]: Parallel: Async: Api: Action: LogWriter](
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F]
    with ABarberoBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = resAccess
  override def postComputation(implicit appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(implicit
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

class ABarberoBotWebhook[F[_]: Async: Api: Action: LogWriter](
    uri: Uri,
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends BotSkeletonWebhook[F](uri, path, webhookCertificate)
    with ABarberoBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = resAccess
  override def postComputation(implicit appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(implicit
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

  override def messageRepliesDataF(implicit
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    ABarberoBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(implicit asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
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
        RegexTextTriggerValue("i \\bre\\b".r, 2),
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
        RegexTextTriggerValue("\\bsubito(o|!){2,}".r, 6)
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
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("eresia"),
        StringTextTriggerValue("riti satanici"),
        StringTextTriggerValue("rinnegamento di gesù cristo"),
        StringTextTriggerValue("sputi sulla croce"),
        StringTextTriggerValue("sodomia"),
      ),
      List(
        MediaFile("abar_RitiSataniciSodomia.mp3")
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
        StringTextTriggerValue("un po' paura")
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
        StringTextTriggerValue(" a morte"),
        RegexTextTriggerValue("\\bsi si si\\b".r, 4)
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
        RegexTextTriggerValue("\\bcert[o!]{3,}\\b".r, 5)
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
        RegexTextTriggerValue("bere il (suo )?sangue".r, 15),
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
        RegexTextTriggerValue("\\bbasta(a|!){2,}".r, 5)
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
        RegexTextTriggerValue("\\bgiusto(o|!){2,}".r, 6)
      ),
      List(
        MediaFile("abar_Giusto.gif")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("gli altri (che )?sono".r, 15)
      ),
      List(
        MediaFile("abar_GliAltri.gif")
      )
    )
  )

  def messageRepliesVideoData[F[_]]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("parole longobarde"),
        StringTextTriggerValue("zuffa"),
        StringTextTriggerValue("spaccare"),
        StringTextTriggerValue("arraffare"),
        StringTextTriggerValue("tanfo"),
      ),
      List(
        MediaFile("abar_ParoleLongobarde.mp4"),
      ),
    ),
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
      ),
      replySelection = RandomSelection
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
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("guerra"),
      ),
      List(
        MediaFile("abar_ParoleLongobarde.mp4"),
        MediaFile("abar_Guerra.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("faida"),
      ),
      List(
        MediaFile("abar_ParoleLongobarde.mp4"),
        MediaFile("abar_Faida.gif"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("spranga")
      ),
      List(
        MediaFile("abar_Spranga.gif"),
        MediaFile("abar_ParoleLongobarde.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("trappola")
      ),
      List(
        MediaFile("abar_Trappola.gif"),
        MediaFile("abar_ParoleLongobarde.mp4")
      ),
      replySelection = RandomSelection
    ),
  )

  def messageRepliesData[F[_]]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesVideoData[F] ++ messageRepliesSpecialData[
      F
    ])
      .sorted(ReplyBundle.orderingInstance[F])
      .reverse

  def commandRepliesData[F[_]: Async](
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F]
  )(implicit
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
      )
    ),
  )

  def buildPollingBot[F[_]: Parallel: Async: Network, A](
      action: ABarberoBotPolling[F] => F[A]
  )(implicit log: LogWriter[F]): F[A] = (for {
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
        resAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager
      )(Parallel[F], Async[F], botSetup.api, botSetup.action, log)
    )
  }

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(implicit log: LogWriter[F]): Resource[F, ABarberoBotWebhook[F]] =
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
        resAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(Async[F], botSetup.api, botSetup.action, log)
    }
}
