package com.benkio.youtuboancheiobot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.InstructionsCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.RandomLinkCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.StatisticsCommands
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.SubscribeUnsubscribeCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.TimeoutCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.TriggerListCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.TriggerSearchCommand
import com.benkio.telegrambotinfrastructure.patterns.PostComputationPatterns
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.lightbend.emoji.ShortCodes.Defaults._
import com.lightbend.emoji.ShortCodes.Implicits._
import com.lightbend.emoji._
import log.effect.LogWriter
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.ember.client._
import org.http4s.implicits._
import telegramium.bots.high._
import telegramium.bots.InputPartFile
import telegramium.bots.Message

class YoutuboAncheIoBotPolling[F[_]: Parallel: Async: Api: Action: LogWriter](
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F]
    with YoutuboAncheIoBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = resAccess
  override def postComputation(implicit appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(implicit
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

class YoutuboAncheIoBotWebhook[F[_]: Async: Api: Action: LogWriter](
    uri: Uri,
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends BotSkeletonWebhook[F](uri, path, webhookCertificate)
    with YoutuboAncheIoBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = resAccess
  override def postComputation(implicit appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(implicit
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

trait YoutuboAncheIoBot[F[_]] extends BotSkeleton[F] {

  override val botName: String                     = YoutuboAncheIoBot.botName
  override val botPrefix: String                   = YoutuboAncheIoBot.botPrefix
  override val ignoreMessagePrefix: Option[String] = YoutuboAncheIoBot.ignoreMessagePrefix
  val backgroundJobManager: BackgroundJobManager[F]

  override def messageRepliesDataF(implicit
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    YoutuboAncheIoBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(implicit asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    YoutuboAncheIoBot
      .commandRepliesData[F](
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
      )
      .pure[F]

}
object YoutuboAncheIoBot {

  val ignoreMessagePrefix: Option[String] = Some("!")
  val botName: String                     = "YoutuboAncheIoBot"
  val botPrefix: String                   = "ytai"
  val triggerListUri: Uri = uri"https://github.com/benkio/myTelegramBot/blob/master/youtuboAncheIoBot/ytai_triggers.txt"
  val tokenFilename: String   = "ytai_YoutuboAncheIoBot.token"
  val configNamespace: String = "ytaiDB"

  def messageRepliesAudioData[
      F[_]
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non vi costa nulla")
      ),
      mediafiles = List(
        MediaFile("ytai_Donazioni.mp3")
      ),
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("bengalino"),
        StringTextTriggerValue("pappagallo"),
        StringTextTriggerValue("uccellino"),
      ),
      mediafiles = List(
        MediaFile("ytai_BengalinoDiamantino.mp3")
      ),
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("cocod[eè]".r, 6),
        StringTextTriggerValue("gallina")
      ),
      mediafiles = List(
        MediaFile("ytai_Cocode.mp3")
      ),
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("\\bmisc\\b".r, 4),
        RegexTextTriggerValue("\\bm[i]+[a]+[o]+\\b".r, 4)
      ),
      mediafiles = List(
        MediaFile("ytai_Misc.mp3")
      ),
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("\\btopolin[oi]\\b".r, 8)
      ),
      mediafiles = List(
        MediaFile("ytai_Topolino.mp3")
      ),
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("francesismo")
      ),
      mediafiles = List(
        MediaFile("ytai_Francesismo.mp3")
      ),
    )
  )

  def messageRepliesGifData[
      F[_]
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("acqua calabria")
      ),
      mediafiles = List(
        GifFile("ytai_AcquaSguardo.mp4"),
        GifFile("ytai_Sete.mp4"),
        GifFile("ytai_AcquaCalabria.mp4"),
        GifFile("ytai_AcquaCalabriaOttima.mp4")
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
        GifFile("ytai_Affaticamento.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("ascolta (queste|le) mie parole".r, 21),
        StringTextTriggerValue("amareggiati"),
        RegexTextTriggerValue("dedicaci (il tuo tempo|le tue notti)".r, 21)
      ),
      mediafiles = List(
        GifFile("ytai_Amareggiati.mp4")
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
        GifFile("ytai_Architetta.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("bel sogno")
      ),
      mediafiles = List(
        GifFile("ytai_BelSogno.mp4")
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
        GifFile("ytai_Brivido.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("buonanotte")
      ),
      mediafiles = List(
        GifFile("ytai_Buonanotte.mp4"),
        GifFile("ytai_BuonanotteBrunchPlus.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("(buona )?pizza".r, 5)
      ),
      mediafiles = List(
        GifFile("ytai_BuonaPizza.mp4"),
        GifFile("ytai_PizzaAllegria.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("buonasera")
      ),
      mediafiles = List(
        GifFile("ytai_Buonasera.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("che spettacolo")
      ),
      mediafiles = List(
        GifFile("ytai_CheSpettacolo.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("che vergogna"),
        StringTextTriggerValue("non ce l'ho"),
        StringTextTriggerValue("sopracciglia"),
        RegexTextTriggerValue("tutti (quanti )?mi criticheranno".r, 22)
      ),
      mediafiles = List(
        GifFile("ytai_CheVergogna.mp4"),
        MediaFile("ytai_CheVergogna.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ciao!")
      ),
      mediafiles = List(
        GifFile("ytai_Ciao.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ciao ragazzi"),
        StringTextTriggerValue("cari saluti")
      ),
      mediafiles = List(
        GifFile("ytai_CiaoRagazzi.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ci divertiremo"),
        StringTextTriggerValue("bel percorso")
      ),
      mediafiles = List(
        GifFile("ytai_CiDivertiremoPercorso.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("colore meraviglioso")
      ),
      mediafiles = List(
        GifFile("ytai_ColoreMeraviglioso.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("corpulenti"),
        StringTextTriggerValue("ciccioni")
      ),
      mediafiles = List(
        GifFile("ytai_CorpulentiCiccioni.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("culetto")
      ),
      mediafiles = List(
        GifFile("ytai_Culetto.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ed allora")
      ),
      mediafiles = List(
        GifFile("ytai_EdAllora.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("fai pure")
      ),
      mediafiles = List(
        GifFile("ytai_FaiPure.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("fallo anche (tu|te)".r, 14),
      ),
      mediafiles = List(
        GifFile("ytai_FalloAncheTu.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("filet[ -]?o[ -]?fish".r, 10),
      ),
      mediafiles = List(
        GifFile("ytai_FiletOFish.mp4"),
        GifFile("ytai_BagnarloAcqua.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue(
          "(deluso|insoddisfatto|inappagato|abbattuto|scoraggiato|demoralizzato|depresso|demotivato|avvilito|scocciato)".r,
          6
        )
      ),
      mediafiles = List(
        GifFile("ytai_Frustrazione.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ci sto già pensando")
      ),
      mediafiles = List(
        GifFile("ytai_GiaPensando.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sono grande"),
        StringTextTriggerValue("sono corpulento")
      ),
      mediafiles = List(
        GifFile("ytai_GrandeCorpulento.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("grazie dottore"),
      ),
      mediafiles = List(
        GifFile("ytai_GrazieDottore.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("grazie tante"),
        StringTextTriggerValue("sconforto grave")
      ),
      mediafiles = List(
        GifFile("ytai_GrazieTante.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("incredibile profumo"),
        StringTextTriggerValue("incredibile aroma")
      ),
      mediafiles = List(
        GifFile("ytai_IncredibileAromaProfumo.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("incredibile"),
        StringTextTriggerValue("inimitabile"),
        RegexTextTriggerValue("the number (one|1)".r, 12)
      ),
      mediafiles = List(
        GifFile("ytai_IncredibileInimitabile.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("io non lo so")
      ),
      mediafiles = List(
        GifFile("ytai_IoNonLoSo.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("loro dovere"),
        StringTextTriggerValue("vostro diritto")
      ),
      mediafiles = List(
        GifFile("ytai_LoroDovereVostroDiritto.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("lo so anche io")
      ),
      mediafiles = List(
        GifFile("ytai_LoSoAncheIo.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("mah")
      ),
      mediafiles = List(
        GifFile("ytai_Mah.mp4"),
        GifFile("ytai_Mah2.mp4"),
        GifFile("ytai_ZoomMah.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("meraviglioso"),
      ),
      mediafiles = List(
        GifFile("ytai_Meraviglioso.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("molla"),
        StringTextTriggerValue("grassone"),
        StringTextTriggerValue("ancora non sei morto")
      ),
      mediafiles = List(
        GifFile("ytai_Molla.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("molto buona")
      ),
      mediafiles = List(
        GifFile("ytai_MoltoBuona.mp4"),
        GifFile("ytai_Buona.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("monoporzioni"),
        RegexTextTriggerValue("mezzo (chilo|kg)".r, 8),
      ),
      mediafiles = List(
        GifFile("ytai_MonoporzioniTiramisu.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non fa male")
      ),
      mediafiles = List(
        GifFile("ytai_NonFaMale.mp4"),
        GifFile("ytai_AcquaMiglioreDrink.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non la crede nessuno questa cosa"),
        StringTextTriggerValue("non ci crede nessuno")
      ),
      mediafiles = List(
        GifFile("ytai_NonLaCredeNessuno.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non pensiamoci più")
      ),
      mediafiles = List(
        GifFile("ytai_NonPensiamoci.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("incomprensione"),
        StringTextTriggerValue("non vi capiscono")
      ),
      mediafiles = List(
        GifFile("ytai_NonViCapiscono.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("occhiolino"),
        StringTextTriggerValue(";)"),
        StringTextTriggerValue(e":wink:"),
      ),
      mediafiles = List(
        GifFile("ytai_Occhiolino.mp4"),
        GifFile("ytai_Occhiolino2.mp4"),
        GifFile("ytai_Occhiolino3.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("olè"),
      ),
      mediafiles = List(
        GifFile("ytai_Ole.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("olè anche io"),
      ),
      mediafiles = List(
        GifFile("ytai_OleAncheIo.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("la perfezione"),
        StringTextTriggerValue("la nostra tendenza")
      ),
      mediafiles = List(
        GifFile("ytai_PerfezioneTendenza.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("profumo meraviglioso"),
      ),
      mediafiles = List(
        GifFile("ytai_ProfumoMeraviglioso.mp4"),
        GifFile("ytai_ProfumoGamberettiSalmone.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sentiamo il profumo")
      ),
      mediafiles = List(
        MediaFile("ytai_ProfumoMeraviglioso.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ricordami fino a domani")
      ),
      mediafiles = List(
        GifFile("ytai_Ricordami.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ringraziamento"),
      ),
      mediafiles = List(
        GifFile("ytai_Ringraziamento.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("se non mi aiuta"),
        StringTextTriggerValue("cosa mi aiuta")
      ),
      mediafiles = List(
        GifFile("ytai_SentiteCheRoba.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sete"),
        RegexTextTriggerValue("(sorso|bicchiere) d'acqua".r, 13)
      ),
      mediafiles = List(
        MediaFile("ytai_Sete.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue(Emoji(0x1f937).toString)
      ),
      mediafiles = List(
        GifFile("ytai_Shrug.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sta per dire qualcosa"),
      ),
      mediafiles = List(
        GifFile("ytai_Silenzio.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("silenzio[,]? silenzio".r, 17),
      ),
      mediafiles = List(
        MediaFile("ytai_Silenzio.mp4"),
        GifFile("ytai_SilenzioMomentoMagico.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("si v[àa] finch[eé] si v[aà]".r, 18),
        StringTextTriggerValue("quando non si potrà andare più"),
        StringTextTriggerValue("è tanto facile")
      ),
      mediafiles = List(
        GifFile("ytai_SiVaFincheSiVa.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sorprendente"),
      ),
      mediafiles = List(
        GifFile("ytai_Sorprendente.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("(😄|😀|😃){4,}".r, 4),
        StringTextTriggerValue("sorriso")
      ),
      mediafiles = List(
        GifFile("ytai_Sorriso.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("spuntino"),
      ),
      mediafiles = List(
        GifFile("ytai_SpuntinoConMe.mp4"),
        GifFile("ytai_SpuntinoConMe2.mp4"),
        GifFile("ytai_BuonoSpuntino.mp4"),
        GifFile("ytai_PaninoBuonoSpuntito.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("ti voglio (tanto )?bene".r, 14),
      ),
      mediafiles = List(
        GifFile("ytai_TVTB.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("l'unica cosa che sai fare"),
      ),
      mediafiles = List(
        GifFile("ytai_UnicaCosaMangiare.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("uno o due")
      ),
      mediafiles = List(
        GifFile("ytai_UnoODue.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("zenzero"),
        StringTextTriggerValue("mia risposta")
      ),
      mediafiles = List(
        GifFile("ytai_Zenzero.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("zoom"),
        StringTextTriggerValue("guardo da vicino")
      ),
      mediafiles = List(
        GifFile("ytai_Zoom.mp4"),
        MediaFile("ytai_ZoomMah.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("tanti auguri"),
      ),
      mediafiles = List(
        GifFile("ytai_TantiAuguri.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("auguri di gusto"),
      ),
      mediafiles = List(
        GifFile("ytai_AuguriDiGusto.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("bagnarlo")
      ),
      mediafiles = List(
        MediaFile("ytai_BagnarloAcqua.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("benvenuti"),
        StringTextTriggerValue("ora e sempre")
      ),
      mediafiles = List(
        GifFile("ytai_Benvenuti.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("buon appetito")
      ),
      mediafiles = List(
        GifFile("ytai_BuonAppetito.mp4"),
        GifFile("ytai_BuonAppetito2.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("diploma"),
        StringTextTriggerValue("per pisciare"),
        RegexTextTriggerValue("ma (che )?stiamo scherzando".r, 20)
      ),
      mediafiles = List(
        GifFile("ytai_DiplomaPisciare.mp4"),
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("non (mi sento|sto) bene".r, 12)
      ),
      mediafiles = List(
        GifFile("ytai_DiversiGioniNonStoBene.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("(grazie e )?arrivederci".r, 11),
      ),
      mediafiles = List(
        GifFile("ytai_GrazieArrivederci.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("eccitato"),
      ),
      mediafiles = List(
        GifFile("ytai_MoltoEccitato.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("no pain"),
        StringTextTriggerValue("no gain")
      ),
      mediafiles = List(
        GifFile("ytai_NoPainNoGain.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non hai più scuse"),
        StringTextTriggerValue("riprenditi"),
        StringTextTriggerValue("sei in gamba"),
      ),
      mediafiles = List(
        GifFile("ytai_NoScuse.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("caspita"),
        RegexTextTriggerValue("sono (grosso|sono (quasi )?enorme|una palla di lardo)".r, 11),
      ),
      mediafiles = List(
        GifFile("ytai_PallaDiLardo.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("mette paura"),
      ),
      mediafiles = List(
        GifFile("ytai_Paura.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("per voi"),
      ),
      mediafiles = List(
        GifFile("ytai_PerVoi.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("alimenti"),
        StringTextTriggerValue("allegria"),
      ),
      mediafiles = List(
        GifFile("ytai_PizzaAllegria.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("gamberetti")
      ),
      mediafiles = List(
        GifFile("ytai_ProfumoGamberettiSalmone.mp4"),
        GifFile("ytai_Sorpresa.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("\\bproviamo\\b".r, 8),
        StringTextTriggerValue("senza morire"),
      ),
      mediafiles = List(
        GifFile("ytai_ProviamoSenzaMorire.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("quello che riesco a fare"),
      ),
      mediafiles = List(
        GifFile("ytai_RiescoAFare.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("sentendo (davvero )?male".r, 13),
      ),
      mediafiles = List(
        GifFile("ytai_SentendoDavveroMale.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("momento magico"),
      ),
      mediafiles = List(
        MediaFile("ytai_SilenzioMomentoMagico.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sopraffino"),
      ),
      mediafiles = List(
        GifFile("ytai_Sopraffino.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("😮"),
        StringTextTriggerValue("😯"),
      ),
      mediafiles = List(
        GifFile("ytai_Sorpresa.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("staccato (quasi )?il naso".r, 16),
      ),
      mediafiles = List(
        GifFile("ytai_StaccatoNaso.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("cuc[uù]+".r, 4),
      ),
      mediafiles = List(
        GifFile("ytai_Cucu.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("al[e]+ [o]{2,}".r, 6),
      ),
      mediafiles = List(
        GifFile("ytai_AleOoo.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("fare senza"),
        StringTextTriggerValue("faenza"),
      ),
      mediafiles = List(
        GifFile("ytai_SenzaFaenza.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("caffè"),
      ),
      mediafiles = List(
        GifFile("ytai_BuonCaffeATutti.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("capolavoro"),
        RegexTextTriggerValue("\\bmeravigliosa\\b".r, 12),
      ),
      mediafiles = List(
        GifFile("ytai_CapolavoroMeravigliosa.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ce la puoi fare"),
      ),
      mediafiles = List(
        GifFile("ytai_CeLaPuoiFare.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("che profumo")
      ),
      mediafiles = List(
        GifFile("ytai_CheProfumo.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("eccolo qua")
      ),
      mediafiles = List(
        GifFile("ytai_EccoloQua.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non mi piacciono"),
      ),
      mediafiles = List(
        GifFile("ytai_NonMiPiaccionoQuesteCose.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("nonno")
      ),
      mediafiles = List(
        GifFile("ytai_NonnoMito.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("soltanto per questo"),
        StringTextTriggerValue("denaro"),
        StringTextTriggerValue("guadagno"),
      ),
      mediafiles = List(
        GifFile("ytai_SoltantoPerQuesto.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("dubbi enciclopedici"),
        StringTextTriggerValue("rifletteteci"),
      ),
      mediafiles = List(
        GifFile("ytai_DubbiEnciclopedici.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("miei limiti"),
      ),
      mediafiles = List(
        GifFile("ytai_Limiti.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("panino")
      ),
      mediafiles = List(
        GifFile("ytai_PaninoBuonoSpuntito.mp4"),
        GifFile("ytai_Panino.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("tiramisù"),
      ),
      mediafiles = List(
        GifFile("ytai_MonoporzioniTiramisu.mp4"),
        GifFile("ytai_Tiramisu.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("m&m's"),
        StringTextTriggerValue("rotear"),
        StringTextTriggerValue("ruotar"),
      ),
      mediafiles = List(
        GifFile("ytai_M&Ms.mp4"),
        GifFile("ytai_M&MsLoop.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("(😂|🤣){4,}".r, 4),
        RegexTextTriggerValue("(ah|ha){7,}".r, 14)
      ),
      mediafiles = List(
        GifFile("ytai_Risata.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("obeso"),
        StringTextTriggerValue("te lo meriti"),
        StringTextTriggerValue("mangi tanto")
      ),
      mediafiles = List(
        GifFile("ytai_CiccioneObesoMangiTanto.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("si vede")
      ),
      mediafiles = List(
        MediaFile("ytai_SiVede.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("correre non serve"),
        RegexTextTriggerValue("\\bfretta\\b".r, 6)
      ),
      mediafiles = List(
        MediaFile("ytai_CorrereNonServe.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("marmellata"),
        StringTextTriggerValue("santa rosa"),
        RegexTextTriggerValue("(la|una) torre".r, 8)
      ),
      mediafiles = List(
        MediaFile("ytai_TorreMarmellata.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("vi voglio (tanto )*bene".r, 14)
      ),
      mediafiles = List(
        MediaFile("ytai_ViVoglioTantoBene.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("salmone"),
      ),
      mediafiles = List(
        GifFile("ytai_SalmoneUnico.mp4"),
        GifFile("ytai_TartinaSalmone.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("un successo"),
        StringTextTriggerValue("agrodolci"),
      ),
      mediafiles = List(
        GifFile("ytai_SaporiAgrodolciSpettacolari.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non parlate di"),
        StringTextTriggerValue("questioni filosofiche"),
        StringTextTriggerValue("non ci azzecc")
      ),
      mediafiles = List(
        GifFile("ytai_QuestioniFilosofiche.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("mang[ai] in (mia )?compagnia".r, 18),
      ),
      mediafiles = List(
        GifFile("ytai_InCompagnia.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("mi incontrate"),
        StringTextTriggerValue("la pancia"),
        StringTextTriggerValue("in imbarazzo")
      ),
      mediafiles = List(
        GifFile("ytai_IncontratePanciaImbarazzo.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("caro amico"),
        StringTextTriggerValue("chiarire"),
      ),
      mediafiles = List(
        GifFile("ytai_GrazieAmicoChiarire.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("commovendo")
      ),
      mediafiles = List(
        GifFile("ytai_Commovendo.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("coda")
      ),
      mediafiles = List(
        GifFile("ytai_CodaLunga.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("brindare"),
        StringTextTriggerValue("drink"),
      ),
      mediafiles = List(
        GifFile("ytai_AcquaMiglioreDrink.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("forchetta"),
      ),
      mediafiles = List(
        GifFile("ytai_MancaForchetta.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("solo[?]{2,}".r, 5),
      ),
      mediafiles = List(
        GifFile("ytai_Solo.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("ottimo[!]+".r, 5),
      ),
      mediafiles = List(
        GifFile("ytai_Ottimo.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sei uno sfigato")
      ),
      mediafiles = List(
        GifFile("ytai_SeiUnoSfigato.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non prendetevi confidenze"),
        StringTextTriggerValue("inteso di darvi")
      ),
      mediafiles = List(
        GifFile("ytai_Confidenze.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("illegale"),
        StringTextTriggerValue("fuorilegge")
      ),
      mediafiles = List(
        GifFile("ytai_IllegaleFuorilegge.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ne ho già parlato"),
        StringTextTriggerValue("ritornare sugli stessi punti"),
        StringTextTriggerValue("lamentato con me")
      ),
      mediafiles = List(
        GifFile("ytai_NeHoGiaParlato.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("alla salute")
      ),
      mediafiles = List(
        GifFile("ytai_AllaSalute.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("grazie ragazzi"),
        StringTextTriggerValue("grazie a tutti")
      ),
      mediafiles = List(
        GifFile("ytai_GrazieRagazzi.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("mi (reputi|consideri) intelligente".r, 22),
        RegexTextTriggerValue("mi (reputi|consideri) sensibile".r, 19)
      ),
      mediafiles = List(
        GifFile("ytai_SensibileIntelligente.mp4")
      )
    )
  )

  def messageRepliesSpecialData[
      F[_]
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("ho perso (di nuovo )?qualcosa".r, 18)
      ),
      mediafiles = List(
        GifFile("ytai_HoPersoQualcosa.mp4"),
        MediaFile("ytai_HoPersoQualcosa.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("reputazione")
      ),
      mediafiles = List(
        GifFile("ytai_LaReputazione.mp4"),
        GifFile("ytai_CheVergogna.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("donazioni")
      ),
      mediafiles = List(
        GifFile("ytai_Donazioni.mp4"),
        MediaFile("ytai_Donazioni.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("a me niente va bene"),
        StringTextTriggerValue("non mi va bene niente")
      ),
      mediafiles = List(
        GifFile("ytai_NienteVaBene.mp4"),
        MediaFile("ytai_NienteVaBene.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ciccione")
      ),
      mediafiles = List(
        GifFile("ytai_Molla.mp4"),
        GifFile("ytai_CiccioneObesoMangiTanto.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("allora l[iì]".r, 9)
      ),
      mediafiles = List(
        GifFile("ytai_AlloraLi.mp4"),
        MediaFile("ytai_AlloraLi.mp3")
      ),
      replySelection = RandomSelection
    )
  )

  def messageRepliesData[
      F[_]
  ]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesSpecialData[F])
      .sorted(ReplyBundle.orderingInstance[F])
      .reverse

  def commandRepliesData[
      F[_]: Async
  ](
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F]
  )(implicit
      log: LogWriter[F]
  ): List[ReplyBundleCommand[F]] = List(
    TriggerListCommand.triggerListReplyBundleCommand[F](triggerListUri),
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

  def buildPollingBot[F[_]: Parallel: Async, A](
      action: YoutuboAncheIoBotPolling[F] => F[A]
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
      new YoutuboAncheIoBotPolling[F](
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
  )(implicit log: LogWriter[F]): Resource[F, YoutuboAncheIoBotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botName = botName,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new YoutuboAncheIoBotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        resAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(Async[F], botSetup.api, botSetup.action, log)
    }
}
