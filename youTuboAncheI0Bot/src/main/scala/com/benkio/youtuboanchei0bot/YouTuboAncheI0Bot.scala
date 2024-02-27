package com.benkio.youtuboanchei0bot

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.*
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.model.*
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
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.ember.client.*
import org.http4s.implicits.*
import telegramium.bots.high.*
import telegramium.bots.InputPartFile
import telegramium.bots.Message

class YouTuboAncheI0BotPolling[F[_]: Parallel: Async: Api: LogWriter](
    val resourceAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F](resourceAccess)
    with YouTuboAncheI0Bot[F] {
  override def resourceAccess(using syncF: Sync[F]): ResourceAccess[F] = resourceAccess
  override def postComputation(using appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(using
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

class YouTuboAncheI0BotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    resourceAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends BotSkeletonWebhook[F](uri, path, webhookCertificate, resourceAccess)
    with YouTuboAncheI0Bot[F] {
  override def resourceAccess(using syncF: Sync[F]): ResourceAccess[F] = resourceAccess
  override def postComputation(using appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(using
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

trait YouTuboAncheI0Bot[F[_]] extends BotSkeleton[F] {

  override val botName: String                     = YouTuboAncheI0Bot.botName
  override val botPrefix: String                   = YouTuboAncheI0Bot.botPrefix
  override val ignoreMessagePrefix: Option[String] = YouTuboAncheI0Bot.ignoreMessagePrefix
  val backgroundJobManager: BackgroundJobManager[F]

  override def messageRepliesDataF(using
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    YouTuboAncheI0Bot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(using asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
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
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      stt"non vi costa nulla"
    )(
      mf"ytai_Donazioni.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"bengalino",
      stt"pappagallo",
      stt"uccellino",
    )(
      mf"ytai_BengalinoDiamantino.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cocod[eè]".r.tr(6),
      stt"gallina"
    )(
      mf"ytai_Cocode.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bmisc\\b".r.tr(4),
      "\\bm[i]+[a]+[o]+\\b".r.tr(4)
    )(
      mf"ytai_Misc.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\btopolin[oi]\\b".r.tr(8)
    )(
      mf"ytai_Topolino.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"francesismo"
    )(
      mf"ytai_Francesismo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"grazie"
    )(
      mf"ytai_Grazie.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"3000",
      stt"tremila",
      stt"multa",
    )(
      mf"ytai_Multa3000euro.mp3"
    )
  )

  def messageRepliesGifData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      stt"acqua calabria"
    )(
      gif"ytai_AcquaSguardo.mp4",
      gif"ytai_Sete.mp4",
      gif"ytai_AcquaCalabria.mp4",
      gif"ytai_AcquaCalabriaOttima.mp4",
      gif"ytai_AcquaMeravigliosa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"fatica",
      stt"sudore",
      stt"sudato"
    )(
      gif"ytai_Affaticamento.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ascolta (queste|le) mie parole".r.tr(21),
      stt"amareggiati",
      "dedicaci (il tuo tempo|le tue notti)".r.tr(21)
    )(
      gif"ytai_Amareggiati.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\barchitetta\\b".r.tr(10),
      "\\bnotaia\\b".r.tr(6),
      "\\bministra\\b".r.tr(8),
      "\\bavvocata\\b".r.tr(8)
    )(
      gif"ytai_Architetta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"bel sogno"
    )(
      gif"ytai_BelSogno.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"brivido",
      stt"fremito",
      stt"tremito",
      stt"tremore"
    )(
      gif"ytai_Brivido.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"buonanotte"
    )(
      gif"ytai_Buonanotte.mp4",
      gif"ytai_BuonanotteBrunchPlus.mp4",
      gif"ytai_BuonanotteFollowers.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "(buona )?pizza".r.tr(5)
    )(
      gif"ytai_BuonaPizza.mp4",
      gif"ytai_PizzaAllegria.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"buonasera"
    )(
      gif"ytai_Buonasera.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"che spettacolo"
    )(
      gif"ytai_CheSpettacolo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ciao!"
    )(
      gif"ytai_Ciao.mp4",
      gif"ytai_Ciao2.mp4",
      gif"ytai_Ciao3.mp4",
      gif"ytai_CiaoRagazzi.mp4",
      gif"ytai_CiaoFollowersNelBeneNelMale.mp4",
      gif"ytai_CiaoCariAmiciFollowers.mp4",
      mp3"ytai_CiaoNonCiArrivo.mp3",
      vid"ytai_CiaoNonCiArrivo.mp4",
      gif"ytai_CiaoNonCiArrivoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ciao ragazzi",
      stt"cari saluti"
    )(
      gif"ytai_CiaoRagazzi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ci divertiremo",
      stt"bel percorso"
    )(
      gif"ytai_CiDivertiremoPercorso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"colore meraviglioso"
    )(
      gif"ytai_ColoreMeraviglioso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"corpulenti",
      stt"ciccioni"
    )(
      gif"ytai_CorpulentiCiccioni.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"culetto"
    )(
      gif"ytai_Culetto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ed allora"
    )(
      gif"ytai_EdAllora.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"fai pure"
    )(
      gif"ytai_FaiPure.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fallo anche (tu|te)".r.tr(14),
    )(
      gif"ytai_FalloAncheTu.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "filet[ -]?o[ -]?fish".r.tr(10),
    )(
      gif"ytai_FiletOFish.mp4",
      gif"ytai_BagnarloAcqua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      RegexTextTriggerValue(
        "(deluso|insoddisfatto|inappagato|abbattuto|scoraggiato|demoralizzato|depresso|demotivato|avvilito|scocciato)".r,
        6
      )
    )(
      gif"ytai_Frustrazione.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ci sto già pensando"
    )(
      gif"ytai_GiaPensando.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sono grande",
      stt"sono corpulento"
    )(
      gif"ytai_GrandeCorpulento.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"grazie dottore",
    )(
      gif"ytai_GrazieDottore.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sconforto grave"
    )(
      gif"ytai_GrazieTante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"incredibile profumo",
      stt"incredibile aroma"
    )(
      gif"ytai_IncredibileAromaProfumo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"incredibile",
      stt"inimitabile",
      "the number (one|1)".r.tr(12)
    )(
      gif"ytai_IncredibileInimitabile.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"io non lo so"
    )(
      gif"ytai_IoNonLoSo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"loro dovere",
      stt"vostro diritto"
    )(
      gif"ytai_LoroDovereVostroDiritto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "lo so (bene )?anche io".r.tr(14)
    )(
      gif"ytai_LoSoAncheIo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mah"
    )(
      gif"ytai_Mah.mp4",
      gif"ytai_Mah2.mp4",
      gif"ytai_Mah3.mp4",
      gif"ytai_ZoomMah.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"meraviglioso",
    )(
      gif"ytai_Meraviglioso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"molla",
      stt"grassone",
      stt"ancora non sei morto"
    )(
      gif"ytai_Molla.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"molto buona"
    )(
      gif"ytai_MoltoBuona.mp4",
      gif"ytai_MoltoBuona2.mp4",
      gif"ytai_Buona.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"monoporzioni",
      "mezzo (chilo|kg)".r.tr(8),
    )(
      gif"ytai_MonoporzioniTiramisu.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non fa male"
    )(
      gif"ytai_NonFaMale.mp4",
      gif"ytai_AcquaMiglioreDrink.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non la crede nessuno questa cosa",
      stt"non ci crede nessuno"
    )(
      gif"ytai_NonLaCredeNessuno.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non pensiamoci più"
    )(
      gif"ytai_NonPensiamoci.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"incomprensione",
      stt"non vi capiscono"
    )(
      gif"ytai_NonViCapiscono.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"occhiolino",
      stt";)",
      stt"😉",
    )(
      gif"ytai_Occhiolino.mp4",
      gif"ytai_Occhiolino2.mp4",
      gif"ytai_Occhiolino3.mp4",
      gif"ytai_OcchiolinoTestaDondolante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"olè",
    )(
      gif"ytai_Ole.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"olè anche io",
    )(
      gif"ytai_OleAncheIo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"la perfezione",
      stt"la nostra tendenza"
    )(
      gif"ytai_PerfezioneTendenza.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"profumo meraviglioso",
    )(
      gif"ytai_ProfumoMeraviglioso.mp4",
      gif"ytai_ProfumoGamberettiSalmone.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sentiamo il profumo"
    )(
      gif"ytai_ProfumoMeraviglioso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ricordami fino a domani"
    )(
      gif"ytai_Ricordami.mp4",
      mf"ytai_RicordamiFinoADomani.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ringraziamento",
    )(
      gif"ytai_Ringraziamento.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"se non mi aiuta",
      stt"cosa mi aiuta"
    )(
      gif"ytai_SentiteCheRoba.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sete",
      "(sorso|bicchiere) d'acqua".r.tr(13)
    )(
      gif"ytai_Sete.mp4",
      gif"ytai_AcquaMeravigliosa.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"🤷"
    )(
      gif"ytai_Shrug.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sta per dire qualcosa",
    )(
      gif"ytai_Silenzio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "silenzio[,]? silenzio".r.tr(17),
    )(
      gif"ytai_Silenzio.mp4",
      gif"ytai_SilenzioMomentoMagico.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "si v[àa] finch[eé] si v[aà]".r.tr(18),
      stt"quando non si potrà andare più",
      stt"è tanto facile"
    )(
      gif"ytai_SiVaFincheSiVa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sorprendente",
    )(
      gif"ytai_Sorprendente.mp4",
      gif"ytai_SecondoBocconeSorprendente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(😄|😀|😃){3,}".r.tr(3),
      stt"sorriso"
    )(
      gif"ytai_Sorriso.mp4",
      gif"ytai_Sorriso2.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"spuntino",
    )(
      gif"ytai_SpuntinoConMe.mp4",
      gif"ytai_SpuntinoConMe2.mp4",
      gif"ytai_SpuntinoConMe3.mp4",
      gif"ytai_BuonoSpuntino.mp4",
      gif"ytai_PaninoBuonoSpuntito.mp4",
      gif"ytai_SpuntinoSmart.mp4",
      gif"ytai_BuonoSpuntinoAncheATe.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"l'unica cosa che sai fare",
    )(
      gif"ytai_UnicaCosaMangiare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"uno o due"
    )(
      gif"ytai_UnoODue.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"zenzero",
      stt"mia risposta"
    )(
      gif"ytai_Zenzero.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"zoom",
      stt"guardo da vicino"
    )(
      gif"ytai_Zoom.mp4",
      gif"ytai_ZoomMah.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"tanti auguri",
    )(
      gif"ytai_TantiAuguri.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"auguri di gusto",
    )(
      gif"ytai_AuguriDiGusto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"bagnarlo"
    )(
      gif"ytai_BagnarloAcqua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"benvenuti",
      stt"ora e sempre"
    )(
      gif"ytai_Benvenuti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"buon appetito"
    )(
      gif"ytai_BuonAppetito.mp4",
      gif"ytai_BuonAppetito2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"diploma",
      stt"per pisciare",
      "ma (che )?stiamo scherzando".r.tr(20)
    )(
      gif"ytai_DiplomaPisciare.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "non (mi sento|sto) bene".r.tr(12)
    )(
      gif"ytai_DiversiGioniNonStoBene.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(grazie e )?arrivederci".r.tr(11),
    )(
      gif"ytai_GrazieArrivederci.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"eccitato",
    )(
      gif"ytai_MoltoEccitato.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"no pain",
      stt"no gain"
    )(
      gif"ytai_NoPainNoGain.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non hai più scuse",
      stt"riprenditi",
      stt"sei in gamba",
    )(
      gif"ytai_NoScuse.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"caspita",
      "sono (grosso|sono (quasi )?enorme|una palla di lardo)".r.tr(11),
    )(
      gif"ytai_PallaDiLardo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mette paura",
    )(
      gif"ytai_Paura.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"per voi",
    )(
      gif"ytai_PerVoi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"alimenti",
      stt"allegria",
    )(
      gif"ytai_PizzaAllegria.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"gamberetti"
    )(
      gif"ytai_ProfumoGamberettiSalmone.mp4",
      gif"ytai_Sorpresa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bproviamo\\b".r.tr(8),
      stt"senza morire",
    )(
      gif"ytai_ProviamoSenzaMorire.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"quello che riesco a fare",
    )(
      gif"ytai_RiescoAFare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sentendo (davvero )?male".r.tr(13),
    )(
      gif"ytai_SentendoDavveroMale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"momento magico",
    )(
      gif"ytai_SilenzioMomentoMagico.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sopraffino",
    )(
      gif"ytai_Sopraffino.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"😮",
      stt"😯",
    )(
      gif"ytai_Sorpresa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "staccato (quasi )?il naso".r.tr(16),
    )(
      gif"ytai_StaccatoNaso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cuc[uù]+".r.tr(4),
    )(
      gif"ytai_Cucu.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "al[e]+ [o]{2,}".r.tr(6),
    )(
      gif"ytai_AleOoo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"fare senza",
      stt"faenza",
    )(
      gif"ytai_SenzaFaenza.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"caffè",
    )(
      gif"ytai_BuonCaffeATutti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"capolavoro",
      "\\bmeravigliosa\\b".r.tr(12),
    )(
      gif"ytai_CapolavoroMeravigliosa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ce la puoi fare",
    )(
      gif"ytai_CeLaPuoiFare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"che profumo"
    )(
      gif"ytai_CheProfumo.mp4",
      gif"ytai_ECheProfumo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"eccolo qua"
    )(
      gif"ytai_EccoloQua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non mi piacciono",
    )(
      gif"ytai_NonMiPiaccionoQuesteCose.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"nonno"
    )(
      gif"ytai_NonnoMito.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"soltanto per questo",
      stt"denaro",
      stt"guadagno",
    )(
      gif"ytai_SoltantoPerQuesto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"dubbi enciclopedici",
      stt"rifletteteci",
    )(
      gif"ytai_DubbiEnciclopedici.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"miei limiti",
    )(
      gif"ytai_Limiti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"panino"
    )(
      gif"ytai_PaninoBuonoSpuntito.mp4",
      gif"ytai_Panino.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"tiramisù",
    )(
      gif"ytai_MonoporzioniTiramisu.mp4",
      gif"ytai_Tiramisu.mp4",
      mp3"ytai_CucchiaioBattagliaTiramisu.mp3",
      vid"ytai_CucchiaioBattagliaTiramisu.mp4",
      gif"ytai_CucchiaioBattagliaTiramisuGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "m[ ]?&[ ]?m['s]?".r.tr(3),
      stt"rotear",
      stt"ruotar",
    )(
      gif"ytai_M&Ms.mp4",
      gif"ytai_M&MsLoop.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "(😂|🤣){3,}".r.tr(4),
      "(ah|ha){5,}".r.tr(14)
    )(
      gif"ytai_Risata.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"obeso",
      stt"te lo meriti",
      stt"mangi tanto"
    )(
      gif"ytai_CiccioneObesoMangiTanto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"si vede"
    )(
      gif"ytai_SiVede.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"correre non serve",
      "\\bfretta\\b".r.tr(6)
    )(
      gif"ytai_CorrereNonServe.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"marmellata",
      stt"santa rosa"
    )(
      gif"ytai_TorreMarmellata.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(la|una) torre".r.tr(8)
    )(
      gif"ytai_TorreMarmellata.mp4",
      gif"ytai_TorreKinderFettaLatte.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"salmone",
    )(
      gif"ytai_SalmoneUnico.mp4",
      gif"ytai_TartinaSalmone.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"un successo",
      stt"agrodolci",
    )(
      gif"ytai_SaporiAgrodolciSpettacolari.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non parlate di",
      stt"questioni filosofiche",
      stt"non ci azzecc"
    )(
      gif"ytai_QuestioniFilosofiche.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mang[ai] in (mia )?compagnia".r.tr(18),
    )(
      gif"ytai_InCompagnia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mi incontrate",
      stt"la pancia",
      stt"in imbarazzo"
    )(
      gif"ytai_IncontratePanciaImbarazzo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"caro amico",
      stt"chiarire",
    )(
      gif"ytai_GrazieAmicoChiarire.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "comm(uo|o)vendo".r.tr(10)
    )(
      gif"ytai_Commovendo.mp4",
      gif"ytai_CommuovendoFareQuelloChePiace.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"coda"
    )(
      gif"ytai_CodaLunga.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"brindare",
      stt"drink",
    )(
      gif"ytai_AcquaMiglioreDrink.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"forchetta",
    )(
      gif"ytai_MancaForchetta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "solo[?]{2,}".r.tr(5),
    )(
      gif"ytai_Solo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ottimo[!]+".r.tr(5),
    )(
      gif"ytai_Ottimo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sei uno sfigato"
    )(
      gif"ytai_SeiUnoSfigato.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non prendetevi confidenze",
      stt"inteso di darvi"
    )(
      gif"ytai_Confidenze.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"illegale"
    )(
      gif"ytai_IllegaleFuorilegge.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ne ho già parlato",
      stt"ritornare sugli stessi punti",
      stt"lamentato con me"
    )(
      gif"ytai_NeHoGiaParlato.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"alla salute"
    )(
      gif"ytai_AllaSalute.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"grazie ragazzi",
      stt"grazie a tutti"
    )(
      gif"ytai_GrazieRagazzi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi (reputi|consideri) intelligente".r.tr(22),
      "mi (reputi|consideri) sensibile".r.tr(19),
      stt"sensibile e intelligente"
    )(
      gif"ytai_SensibileIntelligente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"dico basta"
    )(
      gif"ytai_AdessoViDicoBasta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"che bontà",
      stt"eccoli qua",
    )(
      gif"ytai_ECheProfumo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"parlo poco",
      stt"ingozzo",
      stt"non ve la prendete",
      stt"vostra compagnia",
    )(
      gif"ytai_SeParloPoco.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"meno di un minuto"
    )(
      gif"ytai_MenoDiMinuto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"fetta al latte"
    )(
      gif"ytai_TorreKinderFettaLatte.mp4",
      gif"ytai_KinderFettaAlLatte.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"lasciate libera",
      stt"linea telefonica",
      stt"per cose reali",
      stt"che mi serve"
    )(
      gif"ytai_LineaTelefonica.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ce l'ho fatta",
      "\\bp(à|a')\\b".r.tr(2)
    )(
      gif"ytai_CeLhoFatta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"grazie tante"
    )(
      gif"ytai_GrazieTante.mp4",
      gif"ytai_GrazieTante2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"beviamoci sopra",
      stt"non c'è alcohol",
    )(
      gif"ytai_BeviamociSopraNoAlcohol.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"cercherò di prepararlo",
    )(
      gif"ytai_CercheroDiPrepararlo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"dipende da come mi sento"
    )(
      gif"ytai_DipendeDaComeMiSento.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"eccellente"
    )(
      gif"ytai_Eccellente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non fatelo più"
    )(
      gif"ytai_NonFateloPiu.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"molto dolce",
      stt"molto buono"
    )(
      gif"ytai_MoltoDolceMoltoBuono.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ne avevo proprio voglia"
    )(
      gif"ytai_NeAvevoProprioVoglia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "c'entra (quasi )?sempre".r.tr(14),
    )(
      gif"ytai_CentraQuasiSempre.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"riesco a darvi",
      "imparare (anche io )?(un po' )?di più".r.tr(15),
    )(
      gif"ytai_DarviImparare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"forte profumo",
    )(
      gif"ytai_ForteProfumoMiele.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"😋",
      stt"yum",
      stt"gustoso",
    )(
      gif"ytai_GestoGustoso.mp4",
      gif"ytai_MoltoGustoso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mia filosofia",
      "risol(to|vere) con tutti".r.tr(15),
    )(
      gif"ytai_HoRisoltoConTutti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sentendo in compagnia"
    )(
      gif"ytai_MiStoSentendoInCompagnia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"vengono le parole",
      stt"intendi dire",
    )(
      gif"ytai_NoParoleMostraIntenzioni.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"quello che ci voleva",
      "bibita (bella )?fresca".r.tr(13),
    )(
      gif"ytai_QuestaBibitaBellaFresca.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"secondo boccone",
    )(
      gif"ytai_SecondoBocconeSorprendente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"veterano",
      stt"chat day",
    )(
      gif"ytai_VeteranoChatDays.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"vivete questo momento"
    )(
      gif"ytai_ViveteQuestoMomentoConMe.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\btremo\\b".r.tr(5),
      stt"le mie condizioni"
    )(
      gif"ytai_Tremo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fallo anche (te|tu)".r.tr(14),
      "\\bcome me\\b".r.tr(5)
    )(
      gif"ytai_FalloAncheTeComeMe.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"cercando di fare",
      stt"del mio meglio"
    )(
      gif"ytai_FareDelMioMeglio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"limitato molto",
      stt"essere privato",
      stt"questi soldi",
    )(
      gif"ytai_PrivatoSoldiLimitatoMolto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bball(o|are|i)\\b".r.tr(5),
      "\\bdanz(a|are|i)\\b".r.tr(5),
    )(
      gif"ytai_Ballo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"zebra",
      stt"giraffa",
    )(
      gif"ytai_ZebraGiraffa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"smart"
    )(
      gif"ytai_SpuntinoSmart.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ipocrita"
    )(
      gif"ytai_SonoIpocrita.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "imbarazz(o|ato)".r.tr(9)
    )(
      gif"ytai_SentireInImbarazzo.mp4",
      gif"ytai_LeggermenteImbarazzato.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"nel bene",
      stt"nel male",
    )(
      gif"ytai_CiaoFollowersNelBeneNelMale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"se volete sapere",
      stt"100%",
      stt"non va per me",
    )(
      gif"ytai_SapereTuttoNonVa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"molto aromatico",
      stt"affumicatura",
      stt"pepe nero",
    )(
      gif"ytai_MoltoAromatico.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ci riusciremo",
    )(
      gif"ytai_NonViPreoccupateCiRiusciremo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non vi preoccupate",
    )(
      gif"ytai_NonViPreoccupateCiRiusciremo.mp4",
      gif"ytai_BeviamociSopraNoAlcohol.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"fuorilegge"
    )(
      gif"ytai_NonSonoFuorileggeNecessita.mp4",
      gif"ytai_IllegaleFuorilegge.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ho necessità"
    )(
      gif"ytai_NonSonoFuorileggeNecessita.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi fa (tanto )?piacere".r.tr(12)
    )(
      gif"ytai_MiFaTantoPiacere.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bshow\\b".r.tr(4)
    )(
      gif"ytai_LoShowDeveContunuare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"felice di ritrovarvi"
    )(
      gif"ytai_FeliceDiRitrovarvi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fa(re|rti|tti) (due |i )?conti".r.tr(9),
      stt"il lavoro che fai",
    )(
      gif"ytai_FattiDueConti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"followers",
    )(
      gif"ytai_CiaoFollowersNelBeneNelMale.mp4",
      gif"ytai_BuonanotteFollowers.mp4",
      gif"ytai_CiaoCariAmiciFollowers.mp4",
      gif"ytai_PersonaliLotteFollowers.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"inquietante",
    )(
      gif"ytai_NonInquietante.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"vediamo un po'",
    )(
      gif"ytai_VediamoUnPo.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"rinfrescante",
      stt"calabria",
      "s(\\.)?r(\\.)?l(\\.)?".r.tr(3),
    )(
      gif"ytai_RinfrescanteDiCalabria.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"prima o poi"
    )(
      gif"ytai_NonViPreoccupateCiRiusciremo.mp4",
      gif"ytai_PassioneAllevareGalline.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"allevare",
      stt"galline",
      "mi[ae] passion[ie]".r.tr(12)
    )(
      gif"ytai_PassioneAllevareGalline.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"com'è venuto",
      stt"perfetto"
    )(
      gif"ytai_Perfetto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"per cortesia",
      stt"dottori",
      "dentist[ia]".r.tr(8),
      "ho (ancora )?tanta fame".r.tr(13),
    )(
      mp3"ytai_DentistiDottoriFame.mp3",
      vid"ytai_DentistiDottoriFame.mp4",
      gif"ytai_DentistiDottoriFameGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"verza",
      stt"cavolo cappuccio",
      stt"giuseppe",
      "ma che m(i |')hai detto".r.tr(8),
    )(
      gif"ytai_VerzaGiuseppeGif.mp4",
      mf"ytai_VerzaGiuseppe.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"più piccoline",
      stt"sono dolci",
      stt"al punto giusto",
    )(
      gif"ytai_DolciAlPuntoGiusto.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"amarognolo",
      stt"si intona",
      stt"non guasta",
      stt"contrasto"
    )(
      gif"ytai_ContrastoAmarognolo.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"bella fresca"
    )(
      gif"ytai_BellaFresca.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"piace mangiare così",
      stt"critiche"
    )(
      gif"ytai_MangiareCritiche.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ostica",
      stt"insalata"
    )(
      gif"ytai_OsticaInsalata.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\blotte\\b".r.tr(5),
      stt"condivido"
    )(
      gif"ytai_PersonaliLotteFollowers.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mi assomiglia",
      stt"stessa forma",
      stt"pomodoro",
      stt"sta da solo",
      stt"bisogno di sostegno"
    )(
      mp3"ytai_PomodoroNanoStessaFormaSostegno.mp3",
      vid"ytai_PomodoroNanoStessaFormaSostegno.mp4",
      gif"ytai_PomodoroNanoStessaFormaSostegnoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"condizione umana",
      stt"patologico",
      stt"individuo che comunque vive",
    )(
      gif"ytai_CondizioneUmana.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"occhiali",
      stt"👓",
      stt"🤓",
    )(
      gif"ytai_SistemazioneOcchiali.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"miele",
    )(
      gif"ytai_ForteProfumoMiele.mp4",
      gif"ytai_AppiccicaticcioMiele.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"appiccicaticcio",
    )(
      gif"ytai_AppiccicaticcioMiele.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"scaldato",
      stt"panini",
      stt"sono ottimi",
    )(
      gif"ytai_GrazieScaldatoPanini.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"terminato",
      "facilit[aàá]".r.tr(7)
    )(
      gif"ytai_EstremaFacilita.mp4"
    )
  )

  def messageRepliesMixData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "ho perso (di nuovo )?qualcosa".r.tr(18)
    )(
      gif"ytai_HoPersoQualcosa.mp4",
      mf"ytai_HoPersoQualcosa.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"reputazione"
    )(
      gif"ytai_LaReputazione.mp4",
      gif"ytai_CheVergogna.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"donazioni"
    )(
      gif"ytai_Donazioni.mp4",
      mf"ytai_Donazioni.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"a me niente va bene",
      "non [tm]i va bene niente".r.tr(21)
    )(
      gif"ytai_NienteVaBene.mp4",
      mf"ytai_NienteVaBene.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ciccione"
    )(
      gif"ytai_Molla.mp4",
      gif"ytai_CiccioneObesoMangiTanto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "allora l[iì]".r.tr(9)
    )(
      gif"ytai_AlloraLi.mp4",
      mf"ytai_AlloraLi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"che vergogna",
      stt"non ce l'ho",
      stt"sopracciglia",
      "tutti (quanti )?mi criticheranno".r.tr(22)
    )(
      gif"ytai_CheVergogna.mp4",
      mf"ytai_CheVergogna.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "ti voglio (tanto )?bene".r.tr(14),
    )(
      gif"ytai_TVTB.mp4",
      mf"ytai_AncheIoTiVoglioTantoBene.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vi voglio (tanto )*bene".r.tr(14)
    )(
      gif"ytai_ViVoglioTantoBeneGif.mp4",
      mf"ytai_ViVoglioTantoBene.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "pu[oò] capitare".r.tr(12)
    )(
      gif"ytai_PuoCapitareGif.mp4",
      mf"ytai_PuoCapitare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"strudel"
    )(
      mp3"ytai_30CentimentriStrudelBuonissimo.mp3",
      vid"ytai_30CentimentriStrudelBuonissimo.mp4",
      gif"ytai_30CentimentriStrudelBuonissimoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"boscaiolo",
      stt"come si presenta"
    )(
      mp3"ytai_BoscaioloPresentaColFungo.mp3",
      vid"ytai_BoscaioloPresentaColFungo.mp4",
      gif"ytai_BoscaioloPresentaColFungoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"buon salame",
      stt"cari amici"
    )(
      mp3"ytai_BuonSalameCariAmici.mp3",
      vid"ytai_BuonSalameCariAmici.mp4",
      gif"ytai_BuonSalameCariAmiciGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"buono!"
    )(
      mp3"ytai_Buono.mp3",
      vid"ytai_Buono.mp4",
      gif"ytai_BuonoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ragù"
    )(
      mp3"ytai_BuonoRagu.mp3",
      vid"ytai_BuonoRagu.mp4",
      gif"ytai_BuonoRaguGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non ci arrivo",
      "dopo (che ho )?mangiato".r.tr(13)
    )(
      mp3"ytai_CiaoNonCiArrivo.mp3",
      vid"ytai_CiaoNonCiArrivo.mp4",
      gif"ytai_CiaoNonCiArrivoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"cocco e cioccolata",
      stt"unione strepitosa"
    )(
      mp3"ytai_CoccoCioccolataUnioneStrepitosa.mp3",
      vid"ytai_CoccoCioccolataUnioneStrepitosa.mp4",
      gif"ytai_CoccoCioccolataUnioneStrepitosaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"cotto bene",
      stt"croccante"
    )(
      mp3"ytai_CottoBeneMoltoCroccante.mp3",
      vid"ytai_CottoBeneMoltoCroccante.mp4",
      gif"ytai_CottoBeneMoltoCroccanteGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"vivete questo momento con me",
      stt"sempre sul tubo",
      stt"youtube"
    )(
      mp3"ytai_CredeteSempreSulTuboViveteMomentoConMe.mp3",
      vid"ytai_CredeteSempreSulTuboViveteMomentoConMe.mp4",
      gif"ytai_CredeteSempreSulTuboViveteMomentoConMeGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"cucchiaio",
      stt"da battaglia"
    )(
      mp3"ytai_CucchiaioBattagliaTiramisu.mp3",
      vid"ytai_CucchiaioBattagliaTiramisu.mp4",
      gif"ytai_CucchiaioBattagliaTiramisuGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"champignon",
      stt"in un paio d'ore",
      stt"rovinato lo stomaco",
      stt"stetti male",
      stt"continuando a bere",
    )(
      vid"ytai_StoriaChampignon.mp4",
      gif"ytai_FungoChampignonRovinatoStomaco.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ho spinto a bere",
      stt"un bicchiere tira l'altro",
      stt"aspettavo che portassero la carne",
      stt"cibi di sostanza",
    )(
      vid"ytai_StoriaChampignon.mp4",
      gif"ytai_HoSpintoABere.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sapete come si chiama?",
      stt"incoscienza",
      stt"non pensarci",
      stt"ha un'altro nome",
    )(
      mp3"ytai_NonIncoscienzaNonPensarci.mp3",
      vid"ytai_NonIncoscienzaNonPensarci.mp4",
      gif"ytai_NonIncoscienzaNonPensarciGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"prosciutto crudo"
    )(
      mp3"ytai_ProsciuttoCrudoSpettacolo.mp3",
      vid"ytai_ProsciuttoCrudoSpettacolo.mp4",
      gif"ytai_ProsciuttoCrudoSpettacoloGif.mp4",
      mp3"ytai_ProsciuttoCrudoParla.mp3",
      vid"ytai_ProsciuttoCrudoParla.mp4",
      gif"ytai_ProsciuttoCrudoParlaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"rafforzare lo stomaco",
      stt"polletto"
    )(
      mp3"ytai_RafforzareStomacoPolletto.mp3",
      vid"ytai_RafforzareStomacoPolletto.mp4",
      gif"ytai_RafforzareStomacoPollettoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"(30|trenta) centimetri"
    )(
      mp3"ytai_30CentimentriStrudelBuonissimo.mp3",
      vid"ytai_30CentimentriStrudelBuonissimo.mp4",
      gif"ytai_30CentimentriStrudelBuonissimoGif.mp4",
      mp3"ytai_Serpentello30Centimetri.mp3",
      vid"ytai_Serpentello30Centimetri.mp4",
      gif"ytai_Serpentello30CentimetriGif.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"serpentello"
    )(
      mp3"ytai_Serpentello30Centimetri.mp3",
      vid"ytai_Serpentello30Centimetri.mp4",
      gif"ytai_Serpentello30CentimetriGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"matrimonio"
    )(
      mf"ytai_StoriaChampignon.mp4",
      mf"ytai_StoriaChampignon.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"superiamo questi limiti",
      stt"limiti di pensiero",
      stt"andiamo oltre"
    )(
      mf"ytai_SuperiamoLimitiPensiero.mp4",
      mf"ytai_SuperiamoLimitiPensiero.mp3",
      gif"ytai_SuperiamoLimitiPensieroGif.mp4"
    ),
  )

  def messageRepliesVideoData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      stt"in america",
      "(posto|carico) i video".r.tr(13),
      stt"restiamo in contatto",
      stt"attraverso i commenti",
      stt"sto risolvendo"
    )(
      mf"ytai_SognoAmericano.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"senape",
      stt"non è scaduta",
      stt"ha un gusto strano",
      stt"non ne mangio più",
    )(
      mf"ytai_Senape.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"gigantesco",
      stt"sushi",
      stt"che bellezza"
    )(
      mf"ytai_SushiQuestoEGigantescoBellezza.mp4"
    )
  )

  def messageRepliesImageData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "\\b(ar)?rabbi([oa]|at[oa])\\b".r.tr(6),
      stt"collera",
      "[🤬😡😠]".r.tr(1),
    )(
      mf"ytai_Rabbia.jpg"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[😦😧]".r.tr(1),
      stt"shock",
    )(
      mf"ytai_Shock.jpg"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"😐",
      stt"attonito"
    )(
      mf"ytai_Attonito.jpg"
    ),
  )

  def messageRepliesData[
      F[_]: Applicative
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
  )(using
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
      ),
    )
  )

  def buildPollingBot[F[_]: Parallel: Async: Network, A](
      action: YouTuboAncheI0BotPolling[F] => F[A]
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
      new YouTuboAncheI0BotPolling[F](
        resourceAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager
      )(Parallel[F], Async[F], botSetup.api, log)
    )
  }

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, YouTuboAncheI0BotWebhook[F]] =
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
        resourceAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(Async[F], botSetup.api, log)
    }
}
