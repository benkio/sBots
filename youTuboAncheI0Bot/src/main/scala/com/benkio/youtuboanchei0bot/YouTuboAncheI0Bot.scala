package com.benkio.youtuboanchei0bot

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
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.ember.client._
import org.http4s.implicits._
import telegramium.bots.high._
import telegramium.bots.InputPartFile
import telegramium.bots.Message

class YouTuboAncheI0BotPolling[F[_]: Parallel: Async: Api: Action: LogWriter](
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F]
    with YouTuboAncheI0Bot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = resAccess
  override def postComputation(implicit appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(implicit
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

class YouTuboAncheI0BotWebhook[F[_]: Async: Api: Action: LogWriter](
    uri: Uri,
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends BotSkeletonWebhook[F](uri, path, webhookCertificate)
    with YouTuboAncheI0Bot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = resAccess
  override def postComputation(implicit appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(implicit
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

trait YouTuboAncheI0Bot[F[_]] extends BotSkeleton[F] {

  override val botName: String                     = YouTuboAncheI0Bot.botName
  override val botPrefix: String                   = YouTuboAncheI0Bot.botPrefix
  override val ignoreMessagePrefix: Option[String] = YouTuboAncheI0Bot.ignoreMessagePrefix
  val backgroundJobManager: BackgroundJobManager[F]

  override def messageRepliesDataF(implicit
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    YouTuboAncheI0Bot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(implicit asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    YouTuboAncheI0Bot
      .commandRepliesData[F](
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
      )
      .pure[F]

}
object YouTuboAncheI0Bot {

  val ignoreMessagePrefix: Option[String] = Some("!")
  val botName: String                     = "YouTuboAncheI0Bot"
  val botPrefix: String                   = "ytai"
  val triggerListUri: Uri     = uri"https://github.com/benkio/sBots/blob/master/youTuboAncheI0Bot/ytai_triggers.txt"
  val tokenFilename: String   = "ytai_YouTuboAncheI0Bot.token"
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
        GifFile("ytai_AcquaCalabriaOttima.mp4"),
        GifFile("ytai_AcquaMeravigliosa.mp4")
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
        RegexTextTriggerValue("\\barchitetta\\b".r, 10),
        RegexTextTriggerValue("\\bnotaia\\b".r, 6),
        RegexTextTriggerValue("\\bministra\\b".r, 8),
        RegexTextTriggerValue("\\bavvocata\\b".r, 8)
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
        GifFile("ytai_BuonanotteBrunchPlus.mp4"),
        GifFile("ytai_BuonanotteFollowers.mp4"),
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
        StringTextTriggerValue("ciao!")
      ),
      mediafiles = List(
        GifFile("ytai_Ciao.mp4"),
        GifFile("ytai_Ciao2.mp4"),
        GifFile("ytai_Ciao3.mp4"),
        GifFile("ytai_CiaoRagazzi.mp4"),
        GifFile("ytai_CiaoFollowersNelBeneNelMale.mp4"),
        GifFile("ytai_CiaoCariAmiciFollowers.mp4")
      ),
      replySelection = RandomSelection
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
        RegexTextTriggerValue("lo so (bene )?anche io".r, 14)
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
        GifFile("ytai_Mah3.mp4"),
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
        GifFile("ytai_MoltoBuona2.mp4"),
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
        GifFile("ytai_Occhiolino3.mp4"),
        GifFile("ytai_OcchiolinoTestaDondolante.mp4")
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
        GifFile("ytai_ProfumoMeraviglioso.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ricordami fino a domani")
      ),
      mediafiles = List(
        GifFile("ytai_Ricordami.mp4"),
        MediaFile("ytai_RicordamiFinoADomani.mp4"),
      ),
      replySelection = RandomSelection
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
        GifFile("ytai_Sete.mp4"),
        GifFile("ytai_AcquaMeravigliosa.mp4"),
      ),
      replySelection = RandomSelection
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
        GifFile("ytai_Silenzio.mp4"),
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
        GifFile("ytai_Sorprendente.mp4"),
        GifFile("ytai_SecondoBocconeSorprendente.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("(😄|😀|😃){3,}".r, 3),
        StringTextTriggerValue("sorriso")
      ),
      mediafiles = List(
        GifFile("ytai_Sorriso.mp4"),
        GifFile("ytai_Sorriso2.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("spuntino"),
      ),
      mediafiles = List(
        GifFile("ytai_SpuntinoConMe.mp4"),
        GifFile("ytai_SpuntinoConMe2.mp4"),
        GifFile("ytai_SpuntinoConMe3.mp4"),
        GifFile("ytai_BuonoSpuntino.mp4"),
        GifFile("ytai_PaninoBuonoSpuntito.mp4"),
        GifFile("ytai_SpuntinoSmart.mp4"),
        GifFile("ytai_BuonoSpuntinoAncheATe.mp4"),
      ),
      replySelection = RandomSelection
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
        GifFile("ytai_ZoomMah.mp4")
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
        GifFile("ytai_BagnarloAcqua.mp4")
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
        GifFile("ytai_SilenzioMomentoMagico.mp4")
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
        GifFile("ytai_CheProfumo.mp4"),
        GifFile("ytai_ECheProfumo.mp4")
      ),
      replySelection = RandomSelection
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
        GifFile("ytai_SiVede.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("correre non serve"),
        RegexTextTriggerValue("\\bfretta\\b".r, 6)
      ),
      mediafiles = List(
        GifFile("ytai_CorrereNonServe.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("marmellata"),
        StringTextTriggerValue("santa rosa")
      ),
      mediafiles = List(
        GifFile("ytai_TorreMarmellata.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("(la|una) torre".r, 8)
      ),
      mediafiles = List(
        GifFile("ytai_TorreMarmellata.mp4"),
        GifFile("ytai_TorreKinderFettaLatte.mp4")
      ),
      replySelection = RandomSelection
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
        StringTextTriggerValue("illegale")
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
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("dico basta")
      ),
      mediafiles = List(
        GifFile("ytai_AdessoViDicoBasta.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("che bontà"),
        StringTextTriggerValue("eccoli qua"),
      ),
      mediafiles = List(
        GifFile("ytai_ECheProfumo.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("parlo poco"),
        StringTextTriggerValue("ingozzo"),
        StringTextTriggerValue("non ve la prendete"),
        StringTextTriggerValue("vostra compagnia"),
      ),
      mediafiles = List(
        GifFile("ytai_SeParloPoco.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("meno di un minuto")
      ),
      mediafiles = List(
        GifFile("ytai_MenoDiMinuto.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("fetta al latte")
      ),
      mediafiles = List(
        GifFile("ytai_TorreKinderFettaLatte.mp4"),
        GifFile("ytai_KinderFettaAlLatte.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("lasciate libera"),
        StringTextTriggerValue("linea telefonica"),
        StringTextTriggerValue("per cose reali"),
        StringTextTriggerValue("che mi serve")
      ),
      mediafiles = List(
        GifFile("ytai_LineaTelefonica.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ce l'ho fatta"),
        RegexTextTriggerValue("\\bp(à|a')\\b".r, 2)
      ),
      mediafiles = List(
        GifFile("ytai_CeLhoFatta.mp4")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("grazie tante")
      ),
      mediafiles = List(
        GifFile("ytai_GrazieTante.mp4"),
        GifFile("ytai_GrazieTante2.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("beviamoci sopra"),
        StringTextTriggerValue("non c'è alcohol"),
      ),
      List(
        GifFile("ytai_BeviamociSopraNoAlcohol.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cercherò di prepararlo"),
      ),
      List(
        GifFile("ytai_CercheroDiPrepararlo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("dipende da come mi sento")
      ),
      List(
        GifFile("ytai_DipendeDaComeMiSento.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("eccellente")
      ),
      List(
        GifFile("ytai_Eccellente.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non fatelo più")
      ),
      List(
        GifFile("ytai_NonFateloPiu.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("molto dolce"),
        StringTextTriggerValue("molto buono")
      ),
      List(
        GifFile("ytai_MoltoDolceMoltoBuono.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ne avevo proprio voglia")
      ),
      List(
        GifFile("ytai_NeAvevoProprioVoglia.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("c'entra (quasi )?sempre".r, 14),
      ),
      List(
        GifFile("ytai_CentraQuasiSempre.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("riesco a darvi"),
        RegexTextTriggerValue("imparare (anche io )?(un po' )?di più".r, 15),
      ),
      List(
        GifFile("ytai_DarviImparare.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("miele"),
        StringTextTriggerValue("forte profumo"),
      ),
      List(
        GifFile("ytai_ForteProfumoMiele.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("😋"),
        StringTextTriggerValue("yum"),
        StringTextTriggerValue("gustoso"),
      ),
      List(
        GifFile("ytai_GestoGustoso.mp4"),
        GifFile("ytai_MoltoGustoso.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("mia filosofia"),
        RegexTextTriggerValue("risol(to|vere) con tutti".r, 15),
      ),
      List(
        GifFile("ytai_HoRisoltoConTutti.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("sentendo in compagnia")
      ),
      List(
        GifFile("ytai_MiStoSentendoInCompagnia.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vengono le parole"),
        StringTextTriggerValue("intendi dire"),
      ),
      List(
        GifFile("ytai_NoParoleMostraIntenzioni.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("quello che ci voleva"),
        RegexTextTriggerValue("bibita (bella )?fresca".r, 13),
      ),
      List(
        GifFile("ytai_QuestaBibitaBellaFresca.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("secondo boccone"),
      ),
      List(
        GifFile("ytai_SecondoBocconeSorprendente.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("veterano"),
        StringTextTriggerValue("chat day"),
      ),
      List(
        GifFile("ytai_VeteranoChatDays.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vivete questo momento")
      ),
      List(
        GifFile("ytai_ViveteQuestoMomentoConMe.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\btremo\\b".r, 5),
        StringTextTriggerValue("le mie condizioni")
      ),
      List(
        GifFile("ytai_Tremo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("fallo anche (te|tu)".r, 14),
        RegexTextTriggerValue("\\bcome me\\b".r, 5)
      ),
      List(
        GifFile("ytai_FalloAncheTeComeMe.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("cercando di fare"),
        StringTextTriggerValue("del mio meglio")
      ),
      List(
        GifFile("ytai_FareDelMioMeglio.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("limitato molto"),
        StringTextTriggerValue("essere privato"),
        StringTextTriggerValue("questi soldi"),
      ),
      List(
        GifFile("ytai_PrivatoSoldiLimitatoMolto.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bball(o|are|i)\\b".r, 5),
        RegexTextTriggerValue("\\bdanz(a|are|i)\\b".r, 5),
      ),
      List(
        GifFile("ytai_Ballo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("zebra"),
        StringTextTriggerValue("giraffa"),
      ),
      List(
        GifFile("ytai_ZebraGiraffa.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("smart")
      ),
      List(
        GifFile("ytai_SpuntinoSmart.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ipocrita")
      ),
      List(
        GifFile("ytai_SonoIpocrita.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("imbarazz(o|ato)".r, 9)
      ),
      List(
        GifFile("ytai_SentireInImbarazzo.mp4"),
        GifFile("ytai_LeggermenteImbarazzato.mp4"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("nel bene"),
        StringTextTriggerValue("nel male"),
      ),
      List(
        GifFile("ytai_CiaoFollowersNelBeneNelMale.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("se volete sapere"),
        StringTextTriggerValue("100%"),
        StringTextTriggerValue("non va per me"),
      ),
      List(
        GifFile("ytai_SapereTuttoNonVa.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("molto aromatico"),
        StringTextTriggerValue("affumicatura"),
        StringTextTriggerValue("pepe nero"),
      ),
      List(
        GifFile("ytai_MoltoAromatico.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ci riusciremo"),
      ),
      List(
        GifFile("ytai_NonViPreoccupateCiRiusciremo.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("non vi preoccupate"),
      ),
      List(
        GifFile("ytai_NonViPreoccupateCiRiusciremo.mp4"),
        GifFile("ytai_BeviamociSopraNoAlcohol.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("fuorilegge")
      ),
      List(
        GifFile("ytai_NonSonoFuorileggeNecessita.mp4"),
        GifFile("ytai_IllegaleFuorilegge.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ho necessità")
      ),
      List(
        GifFile("ytai_NonSonoFuorileggeNecessita.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("mi fa (tanto )?piacere".r, 12)
      ),
      List(
        GifFile("ytai_MiFaTantoPiacere.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\bshow\\b".r, 4)
      ),
      List(
        GifFile("ytai_LoShowDeveContunuare.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("felice di ritrovarvi")
      ),
      List(
        GifFile("ytai_FeliceDiRitrovarvi.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("fa(re|rti|tti) (due |i )?conti".r, 9),
        StringTextTriggerValue("il lavoro che fai"),
      ),
      List(
        GifFile("ytai_FattiDueConti.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("followers"),
      ),
      List(
        GifFile("ytai_CiaoFollowersNelBeneNelMale.mp4"),
        GifFile("ytai_BuonanotteFollowers.mp4"),
        GifFile("ytai_CiaoCariAmiciFollowers.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("inquietante"),
      ),
      List(
        GifFile("ytai_NonInquietante.mp4"),
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("vediamo un po'"),
      ),
      List(
        GifFile("ytai_VediamoUnPo.mp4"),
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("rinfrescante"),
        StringTextTriggerValue("calabria"),
        RegexTextTriggerValue("s(\\.)?r(\\.)?l(\\.)?".r, 3),
      ),
      List(
        GifFile("ytai_RinfrescanteDiCalabria.mp4"),
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("prima o poi")
      ),
      List(
        GifFile("ytai_NonViPreoccupateCiRiusciremo.mp4"),
        GifFile("ytai_PassioneAllevareGalline.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("allevare"),
        StringTextTriggerValue("galline"),
        RegexTextTriggerValue("mi[ae] passion[ie]".r, 12)
      ),
      List(
        GifFile("ytai_PassioneAllevareGalline.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("com'è venuto"),
        StringTextTriggerValue("perfetto")
      ),
      List(
        GifFile("ytai_Perfetto.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("per cortesia"),
        StringTextTriggerValue("dottori"),
        RegexTextTriggerValue("dentist[ia]".r, 8),
        RegexTextTriggerValue("ho (ancora )?tanta fame".r, 13),
      ),
      List(
        GifFile("ytai_DentistiFame.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("verza"),
        StringTextTriggerValue("cavolo cappuccio"),
        StringTextTriggerValue("giuseppe"),
        RegexTextTriggerValue("ma che m(i |')hai detto".r, 8),
      ),
      List(
        GifFile("ytai_VerzaGiuseppeGif.mp4"),
        MediaFile("ytai_VerzaGiuseppe.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("più piccoline"),
        StringTextTriggerValue("sono dolci"),
        StringTextTriggerValue("al punto giusto"),
      ),
      List(
        GifFile("ytai_DolciAlPuntoGiusto.mp4"),
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("amarognolo"),
        StringTextTriggerValue("si intona"),
        StringTextTriggerValue("non guasta"),
        StringTextTriggerValue("contrasto")
      ),
      List(
        GifFile("ytai_ContrastoAmarognolo.mp4"),
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("bella fresca")
      ),
      List(
        GifFile("ytai_BellaFresca.mp4"),
      ),
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("piace mangiare così"),
        StringTextTriggerValue("critiche")
      ),
      List(
        GifFile("ytai_MangiareCritiche.mp4"),
      ),
    ),
        ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("ostica"),
        StringTextTriggerValue("insalata")
      ),
      List(
        GifFile("ytai_OsticaInsalata.mp4"),
      ),
    ),
  )

  def messageRepliesMixData[
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
        RegexTextTriggerValue("non [tm]i va bene niente".r, 21)
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
        RegexTextTriggerValue("ti voglio (tanto )?bene".r, 14),
      ),
      mediafiles = List(
        GifFile("ytai_TVTB.mp4"),
        MediaFile("ytai_AncheIoTiVoglioTantoBene.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("vi voglio (tanto )*bene".r, 14)
      ),
      mediafiles = List(
        GifFile("ytai_ViVoglioTantoBeneGif.mp4"),
        MediaFile("ytai_ViVoglioTantoBene.mp4")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("pu[oò] capitare".r, 12)
      ),
      List(
        GifFile("ytai_PuoCapitareGif.mp4"),
        MediaFile("ytai_PuoCapitare.mp4")
      ),
      replySelection = RandomSelection
    ),
  )

  def messageRepliesVideoData[
      F[_]
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("in america"),
        RegexTextTriggerValue("(posto|carico) i video".r, 13),
        StringTextTriggerValue("restiamo in contatto"),
        StringTextTriggerValue("attraverso i commenti"),
        StringTextTriggerValue("sto risolvendo")
      ),
      List(
        MediaFile("ytai_SognoAmericano.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stetti male"),
        StringTextTriggerValue("rovinato lo stomaco"),
        StringTextTriggerValue("champignon"),
        StringTextTriggerValue("matrimonio"),
      ),
      List(
        MediaFile("ytai_StoriaChampignon.mp4")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("senape"),
        StringTextTriggerValue("non è scaduta"),
        StringTextTriggerValue("ha un gusto strano"),
        StringTextTriggerValue("non ne mangio più"),
      ),
      List(
        MediaFile("ytai_Senape.mp4")
      )
    ),
  )

  def messageRepliesImageData[
      F[_]
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("\\b(ar)?rabbi([oa]|at[oa])\\b".r, 6),
        StringTextTriggerValue("collera"),
        RegexTextTriggerValue("[🤬😡😠]".r, 1),
      ),
      List(
        MediaFile("ytai_Rabbia.jpg")
      )
    ),
    ReplyBundleMessage(
      TextTrigger(
        RegexTextTriggerValue("[😦😧]".r, 1),
        StringTextTriggerValue("shock"),
      ),
      List(
        MediaFile("ytai_Shock.jpg")
      )
    ),
  )

  def messageRepliesData[
      F[_]
  ]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesMixData[F] ++ messageRepliesVideoData[
      F
    ] ++ messageRepliesImageData[F])
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

  def buildPollingBot[F[_]: Parallel: Async: Network, A](
      action: YouTuboAncheI0BotPolling[F] => F[A]
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
      new YouTuboAncheI0BotPolling[F](
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
  )(implicit log: LogWriter[F]): Resource[F, YouTuboAncheI0BotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botName = botName,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new YouTuboAncheI0BotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        resAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(Async[F], botSetup.api, botSetup.action, log)
    }
}
