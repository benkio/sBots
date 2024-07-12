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
      "non vi costa nulla"
    )(
      mp3"ytai_Donazioni.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "bengalino",
      "pappagallo",
      "uccellino",
    )(
      mp3"ytai_BengalinoDiamantino.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cocod[eè]".r.tr(6),
      "gallina"
    )(
      mp3"ytai_Cocode.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bmisc\\b".r.tr(4),
      "\\bm[i]+[a]+[o]+\\b".r.tr(4)
    )(
      mp3"ytai_Misc.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\btopolin[oi]\\b".r.tr(8)
    )(
      mp3"ytai_Topolino.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "francesismo"
    )(
      mp3"ytai_Francesismo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "grazie"
    )(
      mp3"ytai_Grazie.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "3000",
      "tremila",
      "multa",
    )(
      mp3"ytai_Multa3000euro.mp3"
    )
  )

  def messageRepliesGifData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "acqua calabria"
    )(
      gif"ytai_AcquaSguardo.mp4",
      gif"ytai_Sete.mp4",
      gif"ytai_AcquaCalabria.mp4",
      gif"ytai_AcquaCalabriaOttima.mp4",
      gif"ytai_AcquaMeravigliosa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fatica",
      "sudore",
      "sudato"
    )(
      gif"ytai_Affaticamento.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ascolta (queste|le) mie parole".r.tr(21),
      "amareggiati",
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
      "bel sogno"
    )(
      gif"ytai_BelSogno.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "brivido",
      "fremito",
      "tremito",
      "tremore"
    )(
      gif"ytai_Brivido.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "buonanotte"
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
      "buonasera"
    )(
      gif"ytai_Buonasera.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "che spettacolo"
    )(
      gif"ytai_CheSpettacolo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ciao!"
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
      "ciao ragazzi",
      "cari saluti"
    )(
      gif"ytai_CiaoRagazzi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ci divertiremo",
      "bel percorso"
    )(
      gif"ytai_CiDivertiremoPercorso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "colore meraviglioso"
    )(
      gif"ytai_ColoreMeraviglioso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "corpulenti",
      "ciccioni"
    )(
      gif"ytai_CorpulentiCiccioni.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "culetto"
    )(
      gif"ytai_Culetto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ed allora"
    )(
      gif"ytai_EdAllora.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fai pure"
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
      "ci sto già pensando"
    )(
      gif"ytai_GiaPensando.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sono grande",
      "sono corpulento"
    )(
      gif"ytai_GrandeCorpulento.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "grazie dottore",
    )(
      gif"ytai_GrazieDottore.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sconforto grave"
    )(
      gif"ytai_GrazieTante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "incredibile profumo",
      "incredibile aroma"
    )(
      gif"ytai_IncredibileAromaProfumo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "incredibile",
      "inimitabile",
      "the number (one|1)".r.tr(12)
    )(
      gif"ytai_IncredibileInimitabile.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "io non lo so"
    )(
      gif"ytai_IoNonLoSo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "loro dovere",
      "vostro diritto"
    )(
      gif"ytai_LoroDovereVostroDiritto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "lo so (bene )?anche io".r.tr(14)
    )(
      gif"ytai_LoSoAncheIo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mah"
    )(
      gif"ytai_Mah.mp4",
      gif"ytai_Mah2.mp4",
      gif"ytai_Mah3.mp4",
      gif"ytai_ZoomMah.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "meraviglioso",
    )(
      gif"ytai_Meraviglioso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "molla",
      "grassone",
      "ancora non sei morto"
    )(
      gif"ytai_Molla.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "molto buona"
    )(
      gif"ytai_MoltoBuona.mp4",
      gif"ytai_MoltoBuona2.mp4",
      gif"ytai_Buona.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "monoporzioni",
      "mezzo (chilo|kg)".r.tr(8),
    )(
      gif"ytai_MonoporzioniTiramisu.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non fa male"
    )(
      gif"ytai_NonFaMale.mp4",
      gif"ytai_AcquaMiglioreDrink.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non la crede nessuno questa cosa",
      "non ci crede nessuno"
    )(
      gif"ytai_NonLaCredeNessuno.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non pensiamoci più"
    )(
      gif"ytai_NonPensiamoci.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "incomprensione",
      "non vi capiscono"
    )(
      gif"ytai_NonViCapiscono.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "occhiolino",
      ";)",
      "😉",
    )(
      gif"ytai_Occhiolino.mp4",
      gif"ytai_Occhiolino2.mp4",
      gif"ytai_Occhiolino3.mp4",
      gif"ytai_OcchiolinoTestaDondolante.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "olè",
    )(
      gif"ytai_Ole.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "olè anche io",
    )(
      gif"ytai_OleAncheIo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "la perfezione",
      "la nostra tendenza"
    )(
      gif"ytai_PerfezioneTendenza.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "profumo meraviglioso",
    )(
      gif"ytai_ProfumoMeraviglioso.mp4",
      gif"ytai_ProfumoGamberettiSalmone.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sentiamo il profumo"
    )(
      gif"ytai_ProfumoMeraviglioso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ricordami fino a domani"
    )(
      gif"ytai_Ricordami.mp4",
      vid"ytai_RicordamiFinoADomani.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "ringraziamento",
    )(
      gif"ytai_Ringraziamento.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "se non mi aiuta",
      "cosa mi aiuta"
    )(
      gif"ytai_SentiteCheRoba.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sete",
      "(sorso|bicchiere) d'acqua".r.tr(13)
    )(
      gif"ytai_Sete.mp4",
      gif"ytai_AcquaMeravigliosa.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "🤷"
    )(
      gif"ytai_Shrug.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sta per dire qualcosa",
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
      "quando non si potrà andare più",
      "è tanto facile"
    )(
      gif"ytai_SiVaFincheSiVa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sorprendente",
    )(
      gif"ytai_Sorprendente.mp4",
      gif"ytai_SecondoBocconeSorprendente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(😄|😀|😃){3,}".r.tr(3),
      "sorriso"
    )(
      gif"ytai_Sorriso.mp4",
      gif"ytai_Sorriso2.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "spuntino",
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
      "l'unica cosa che sai fare",
    )(
      gif"ytai_UnicaCosaMangiare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "uno o due"
    )(
      gif"ytai_UnoODue.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "zenzero",
      "mia risposta"
    )(
      gif"ytai_Zenzero.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "zoom",
      "guardo da vicino"
    )(
      gif"ytai_Zoom.mp4",
      gif"ytai_ZoomMah.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tanti auguri",
    )(
      gif"ytai_TantiAuguri.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "auguri di gusto",
    )(
      gif"ytai_AuguriDiGusto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "bagnarlo"
    )(
      gif"ytai_BagnarloAcqua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "benvenuti",
      "ora e sempre"
    )(
      gif"ytai_Benvenuti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "buon appetito"
    )(
      gif"ytai_BuonAppetito.mp4",
      gif"ytai_BuonAppetito2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "diploma",
      "per pisciare",
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
      "eccitato",
    )(
      gif"ytai_MoltoEccitato.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "no pain",
      "no gain"
    )(
      gif"ytai_NoPainNoGain.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non hai più scuse",
      "riprenditi",
      "sei in gamba",
    )(
      gif"ytai_NoScuse.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "caspita",
      "sono (grosso|sono (quasi )?enorme|una palla di lardo)".r.tr(11),
    )(
      gif"ytai_PallaDiLardo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mette paura",
    )(
      gif"ytai_Paura.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "per voi",
    )(
      gif"ytai_PerVoi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "alimenti",
      "allegria",
    )(
      gif"ytai_PizzaAllegria.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "gamberetti"
    )(
      gif"ytai_ProfumoGamberettiSalmone.mp4",
      gif"ytai_Sorpresa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bproviamo\\b".r.tr(8),
      "senza morire",
    )(
      gif"ytai_ProviamoSenzaMorire.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "quello che riesco a fare",
    )(
      gif"ytai_RiescoAFare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sentendo (davvero )?male".r.tr(13),
    )(
      gif"ytai_SentendoDavveroMale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "momento magico",
    )(
      gif"ytai_SilenzioMomentoMagico.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sopraffino",
    )(
      gif"ytai_Sopraffino.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "😮",
      "😯",
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
      "fare senza",
      "faenza",
    )(
      gif"ytai_SenzaFaenza.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "caffè",
    )(
      gif"ytai_BuonCaffeATutti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "capolavoro",
      "\\bmeravigliosa\\b".r.tr(12),
    )(
      gif"ytai_CapolavoroMeravigliosa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ce la puoi fare",
    )(
      gif"ytai_CeLaPuoiFare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "che profumo"
    )(
      gif"ytai_CheProfumo.mp4",
      gif"ytai_ECheProfumo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "eccolo qua"
    )(
      gif"ytai_EccoloQua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non mi piacciono",
    )(
      gif"ytai_NonMiPiaccionoQuesteCose.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "nonno"
    )(
      gif"ytai_NonnoMito.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "soltanto per questo",
      "denaro",
      "guadagno",
    )(
      gif"ytai_SoltantoPerQuesto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dubbi enciclopedici",
      "rifletteteci",
    )(
      gif"ytai_DubbiEnciclopedici.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "miei limiti",
    )(
      gif"ytai_Limiti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "panino"
    )(
      gif"ytai_PaninoBuonoSpuntito.mp4",
      gif"ytai_Panino.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tiramisù",
    )(
      gif"ytai_MonoporzioniTiramisu.mp4",
      gif"ytai_Tiramisu.mp4",
      mp3"ytai_CucchiaioBattagliaTiramisu.mp3",
      vid"ytai_CucchiaioBattagliaTiramisu.mp4",
      gif"ytai_CucchiaioBattagliaTiramisuGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "m[ ]?&[ ]?m['s]?".r.tr(3),
      "rotear",
      "ruotar",
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
      "obeso",
      "te lo meriti",
      "mangi tanto"
    )(
      gif"ytai_CiccioneObesoMangiTanto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "si vede"
    )(
      gif"ytai_SiVede.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "correre non serve",
      "\\bfretta\\b".r.tr(6)
    )(
      gif"ytai_CorrereNonServe.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "marmellata",
      "santa rosa"
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
      "salmone",
    )(
      gif"ytai_SalmoneUnico.mp4",
      gif"ytai_TartinaSalmone.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "un successo",
      "agrodolci",
    )(
      gif"ytai_SaporiAgrodolciSpettacolari.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non parlate di",
      "questioni filosofiche",
      "non ci azzecc"
    )(
      gif"ytai_QuestioniFilosofiche.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mang[ai] in (mia )?compagnia".r.tr(18),
    )(
      gif"ytai_InCompagnia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi incontrate",
      "la pancia",
      "in imbarazzo"
    )(
      gif"ytai_IncontratePanciaImbarazzo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "caro amico",
      "chiarire",
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
      "coda"
    )(
      gif"ytai_CodaLunga.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "brindare",
      "drink",
    )(
      gif"ytai_AcquaMiglioreDrink.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "forchetta",
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
      "sei uno sfigato"
    )(
      gif"ytai_SeiUnoSfigato.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non prendetevi confidenze",
      "inteso di darvi"
    )(
      gif"ytai_Confidenze.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "illegale"
    )(
      gif"ytai_IllegaleFuorilegge.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ne ho già parlato",
      "tornare sugli stessi punti",
      "lamentato con me"
    )(
      gif"ytai_NeHoGiaParlato.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "alla salute"
    )(
      gif"ytai_AllaSalute.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "grazie ragazzi",
      "grazie a tutti"
    )(
      gif"ytai_GrazieRagazzi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi (reputi|consideri) intelligente".r.tr(22),
      "mi (reputi|consideri) sensibile".r.tr(19),
      "sensibile e intelligente"
    )(
      gif"ytai_SensibileIntelligente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dico basta"
    )(
      gif"ytai_AdessoViDicoBasta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "che bontà",
      "eccoli qua",
    )(
      gif"ytai_ECheProfumo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "parlo poco",
      "ingozzo",
      "non ve la prendete",
      "vostra compagnia",
    )(
      gif"ytai_SeParloPoco.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "meno di un minuto"
    )(
      gif"ytai_MenoDiMinuto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fetta al latte"
    )(
      gif"ytai_TorreKinderFettaLatte.mp4",
      gif"ytai_KinderFettaAlLatte.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "lasciate libera",
      "linea telefonica",
      "per cose reali",
      "che mi serve"
    )(
      gif"ytai_LineaTelefonica.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ce l'ho fatta",
      "\\bp(à|a')\\b".r.tr(2)
    )(
      gif"ytai_CeLhoFatta.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "grazie tante"
    )(
      gif"ytai_GrazieTante.mp4",
      gif"ytai_GrazieTante2.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "beviamoci sopra",
      "non c'è alcohol",
    )(
      gif"ytai_BeviamociSopraNoAlcohol.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cercherò di prepararlo",
    )(
      gif"ytai_CercheroDiPrepararlo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dipende da come mi sento"
    )(
      gif"ytai_DipendeDaComeMiSento.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "eccellente"
    )(
      gif"ytai_Eccellente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non fatelo più"
    )(
      gif"ytai_NonFateloPiu.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "molto dolce",
      "molto buono"
    )(
      gif"ytai_MoltoDolceMoltoBuono.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ne avevo proprio voglia"
    )(
      gif"ytai_NeAvevoProprioVoglia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "c'entra (quasi )?sempre".r.tr(14),
    )(
      gif"ytai_CentraQuasiSempre.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "riesco a darvi",
      "imparare (anche io )?(un po' )?di più".r.tr(15),
    )(
      gif"ytai_DarviImparare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "forte profumo",
    )(
      gif"ytai_ForteProfumoMiele.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "😋",
      "yum",
      "gustoso",
    )(
      gif"ytai_GestoGustoso.mp4",
      gif"ytai_MoltoGustoso.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mia filosofia",
      "risol(to|vere) con tutti".r.tr(15),
    )(
      gif"ytai_HoRisoltoConTutti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sentendo in compagnia"
    )(
      gif"ytai_MiStoSentendoInCompagnia.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vengono le parole",
      "intendi dire",
    )(
      gif"ytai_NoParoleMostraIntenzioni.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "quello che ci voleva",
      "bibita (bella )?fresca".r.tr(13),
    )(
      gif"ytai_QuestaBibitaBellaFresca.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "secondo boccone",
    )(
      gif"ytai_SecondoBocconeSorprendente.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "veterano",
      "chat day",
    )(
      gif"ytai_VeteranoChatDays.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vivete questo momento"
    )(
      gif"ytai_ViveteQuestoMomentoConMe.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\btremo\\b".r.tr(5),
      "le mie condizioni"
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
      "cercando di fare",
      "del mio meglio"
    )(
      gif"ytai_FareDelMioMeglio.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "limitato molto",
      "essere privato",
      "questi soldi",
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
      "zebra",
      "giraffa",
    )(
      gif"ytai_ZebraGiraffa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "smart"
    )(
      gif"ytai_SpuntinoSmart.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ipocrita"
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
      "nel bene",
      "nel male",
    )(
      gif"ytai_CiaoFollowersNelBeneNelMale.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "se volete sapere",
      "100%",
      "non va per me",
    )(
      gif"ytai_SapereTuttoNonVa.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "molto aromatico",
      "affumicatura",
      "pepe nero",
    )(
      gif"ytai_MoltoAromatico.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ci riusciremo",
    )(
      gif"ytai_NonViPreoccupateCiRiusciremo.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non vi preoccupate",
    )(
      gif"ytai_NonViPreoccupateCiRiusciremo.mp4",
      gif"ytai_BeviamociSopraNoAlcohol.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fuorilegge"
    )(
      gif"ytai_NonSonoFuorileggeNecessita.mp4",
      gif"ytai_IllegaleFuorilegge.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ho necessità"
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
      "felice di ritrovarvi"
    )(
      gif"ytai_FeliceDiRitrovarvi.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "fa(re|rti|tti) (due |i )?conti".r.tr(9),
      "il lavoro che fai",
    )(
      gif"ytai_FattiDueConti.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "followers",
    )(
      gif"ytai_CiaoFollowersNelBeneNelMale.mp4",
      gif"ytai_BuonanotteFollowers.mp4",
      gif"ytai_CiaoCariAmiciFollowers.mp4",
      gif"ytai_PersonaliLotteFollowers.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "inquietante",
    )(
      gif"ytai_NonInquietante.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "vediamo un po'",
    )(
      gif"ytai_VediamoUnPo.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "rinfrescante",
      "calabria",
      "s(\\.)?r(\\.)?l(\\.)?".r.tr(3),
    )(
      gif"ytai_RinfrescanteDiCalabria.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "prima o poi"
    )(
      gif"ytai_NonViPreoccupateCiRiusciremo.mp4",
      gif"ytai_PassioneAllevareGalline.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "allevare",
      "galline",
      "mi[ae] passion[ie]".r.tr(12)
    )(
      gif"ytai_PassioneAllevareGalline.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "com'è venuto",
      "perfetto"
    )(
      gif"ytai_Perfetto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "per cortesia",
      "dottori",
      "dentist[ia]".r.tr(8),
      "ho (ancora )?tanta fame".r.tr(13),
    )(
      mp3"ytai_DentistiDottoriFame.mp3",
      vid"ytai_DentistiDottoriFame.mp4",
      gif"ytai_DentistiDottoriFameGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "verza",
      "cavolo cappuccio",
      "giuseppe",
      "ma che m(i |')hai detto".r.tr(8),
    )(
      gif"ytai_VerzaGiuseppeGif.mp4",
      vid"ytai_VerzaGiuseppe.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "più piccoline",
      "sono dolci",
      "al punto giusto",
    )(
      gif"ytai_DolciAlPuntoGiusto.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "amarognolo",
      "si intona",
      "non guasta",
      "contrasto"
    )(
      gif"ytai_ContrastoAmarognolo.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "bella fresca"
    )(
      gif"ytai_BellaFresca.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "piace mangiare così",
      "critiche"
    )(
      gif"ytai_MangiareCritiche.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "ostica",
      "insalata"
    )(
      gif"ytai_OsticaInsalata.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\blotte\\b".r.tr(5),
      "condivido"
    )(
      gif"ytai_PersonaliLotteFollowers.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi assomiglia",
      "stessa forma",
      "pomodoro",
      "sta da solo",
      "bisogno di sostegno"
    )(
      mp3"ytai_PomodoroNanoStessaFormaSostegno.mp3",
      vid"ytai_PomodoroNanoStessaFormaSostegno.mp4",
      gif"ytai_PomodoroNanoStessaFormaSostegnoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "condizione umana",
      "patologico",
      "individuo che comunque vive",
    )(
      gif"ytai_CondizioneUmana.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "occhiali",
      "👓",
      "🤓",
    )(
      gif"ytai_SistemazioneOcchiali.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "miele",
    )(
      gif"ytai_ForteProfumoMiele.mp4",
      gif"ytai_AppiccicaticcioMiele.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "appiccicaticcio",
    )(
      gif"ytai_AppiccicaticcioMiele.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "scaldato",
      "panini",
      "sono ottimi",
    )(
      gif"ytai_GrazieScaldatoPanini.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bterminato\\b".r.tr(9),
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
      mp3"ytai_HoPersoQualcosa.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "reputazione"
    )(
      gif"ytai_LaReputazione.mp4",
      gif"ytai_CheVergogna.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "donazioni"
    )(
      gif"ytai_Donazioni.mp4",
      mp3"ytai_Donazioni.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "a me niente va bene",
      "non [tm]i va bene niente".r.tr(21)
    )(
      gif"ytai_NienteVaBene.mp4",
      mp3"ytai_NienteVaBene.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "ciccione"
    )(
      gif"ytai_Molla.mp4",
      gif"ytai_CiccioneObesoMangiTanto.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "allora l[iì]".r.tr(9)
    )(
      gif"ytai_AlloraLi.mp4",
      mp3"ytai_AlloraLi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "che vergogna",
      "non ce l'ho",
      "sopracciglia",
      "tutti (quanti )?mi criticheranno".r.tr(22)
    )(
      gif"ytai_CheVergogna.mp4",
      mp3"ytai_CheVergogna.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "ti voglio (tanto )?bene".r.tr(14),
    )(
      gif"ytai_TVTB.mp4",
      mp3"ytai_AncheIoTiVoglioTantoBene.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vi voglio (tanto )*bene".r.tr(14)
    )(
      gif"ytai_ViVoglioTantoBeneGif.mp4",
      vid"ytai_ViVoglioTantoBene.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "pu[oò] capitare".r.tr(12)
    )(
      gif"ytai_PuoCapitareGif.mp4",
      vid"ytai_PuoCapitare.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "strudel"
    )(
      mp3"ytai_30CentimentriStrudelBuonissimo.mp3",
      vid"ytai_30CentimentriStrudelBuonissimo.mp4",
      gif"ytai_30CentimentriStrudelBuonissimoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "boscaiolo",
      "come si presenta"
    )(
      mp3"ytai_BoscaioloPresentaColFungo.mp3",
      vid"ytai_BoscaioloPresentaColFungo.mp4",
      gif"ytai_BoscaioloPresentaColFungoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "buon salame",
      "cari amici"
    )(
      mp3"ytai_BuonSalameCariAmici.mp3",
      vid"ytai_BuonSalameCariAmici.mp4",
      gif"ytai_BuonSalameCariAmiciGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "buono!"
    )(
      mp3"ytai_Buono.mp3",
      vid"ytai_Buono.mp4",
      gif"ytai_BuonoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ragù"
    )(
      mp3"ytai_BuonoRagu.mp3",
      vid"ytai_BuonoRagu.mp4",
      gif"ytai_BuonoRaguGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non ci arrivo",
      "dopo (che ho )?mangiato".r.tr(13)
    )(
      mp3"ytai_CiaoNonCiArrivo.mp3",
      vid"ytai_CiaoNonCiArrivo.mp4",
      gif"ytai_CiaoNonCiArrivoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cocco e cioccolata",
      "unione strepitosa"
    )(
      mp3"ytai_CoccoCioccolataUnioneStrepitosa.mp3",
      vid"ytai_CoccoCioccolataUnioneStrepitosa.mp4",
      gif"ytai_CoccoCioccolataUnioneStrepitosaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cotto bene",
      "croccante"
    )(
      mp3"ytai_CottoBeneMoltoCroccante.mp3",
      vid"ytai_CottoBeneMoltoCroccante.mp4",
      gif"ytai_CottoBeneMoltoCroccanteGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vivete questo momento con me",
      "sempre sul tubo"
    )(
      mp3"ytai_CredeteSempreSulTuboViveteMomentoConMe.mp3",
      vid"ytai_CredeteSempreSulTuboViveteMomentoConMe.mp4",
      gif"ytai_CredeteSempreSulTuboViveteMomentoConMeGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cucchiaio",
      "da battaglia"
    )(
      mp3"ytai_CucchiaioBattagliaTiramisu.mp3",
      vid"ytai_CucchiaioBattagliaTiramisu.mp4",
      gif"ytai_CucchiaioBattagliaTiramisuGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "champignon",
      "in un paio d'ore",
      "rovinato lo stomaco",
      "stetti male",
      "continuando a bere",
    )(
      vid"ytai_StoriaChampignon.mp4",
      gif"ytai_FungoChampignonRovinatoStomaco.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ho spinto a bere",
      "un bicchiere tira l'altro",
      "aspettavo che portassero la carne",
      "cibi di sostanza",
    )(
      vid"ytai_StoriaChampignon.mp4",
      gif"ytai_HoSpintoABere.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sapete come si chiama?",
      "incoscienza",
      "non pensarci",
      "ha un'altro nome",
    )(
      mp3"ytai_NonIncoscienzaNonPensarci.mp3",
      vid"ytai_NonIncoscienzaNonPensarci.mp4",
      gif"ytai_NonIncoscienzaNonPensarciGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "prosciutto crudo"
    )(
      mp3"ytai_ProsciuttoCrudoSpettacolo.mp3",
      vid"ytai_ProsciuttoCrudoSpettacolo.mp4",
      gif"ytai_ProsciuttoCrudoSpettacoloGif.mp4",
      mp3"ytai_ProsciuttoCrudoParla.mp3",
      vid"ytai_ProsciuttoCrudoParla.mp4",
      gif"ytai_ProsciuttoCrudoParlaGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "rafforzare lo stomaco",
      "polletto"
    )(
      mp3"ytai_RafforzareStomacoPolletto.mp3",
      vid"ytai_RafforzareStomacoPolletto.mp4",
      gif"ytai_RafforzareStomacoPollettoGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(30|trenta) centimetri"
    )(
      mp3"ytai_30CentimentriStrudelBuonissimo.mp3",
      vid"ytai_30CentimentriStrudelBuonissimo.mp4",
      gif"ytai_30CentimentriStrudelBuonissimoGif.mp4",
      mp3"ytai_Serpentello30Centimetri.mp3",
      vid"ytai_Serpentello30Centimetri.mp4",
      gif"ytai_Serpentello30CentimetriGif.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "serpentello"
    )(
      mp3"ytai_Serpentello30Centimetri.mp3",
      vid"ytai_Serpentello30Centimetri.mp4",
      gif"ytai_Serpentello30CentimetriGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "matrimonio"
    )(
      vid"ytai_StoriaChampignon.mp4",
      mp3"ytai_StoriaChampignon.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "superiamo questi limiti",
      "limiti di pensiero",
      "andiamo oltre"
    )(
      vid"ytai_SuperiamoLimitiPensiero.mp4",
      mp3"ytai_SuperiamoLimitiPensiero.mp3",
      gif"ytai_SuperiamoLimitiPensieroGif.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "rovesciata l'acqua",
      "\\bops\\b".r.tr(3)
    )(
      mp3"ytai_OpsRovesciataAcqua.mp3",
      gif"ytai_OpsRovesciataAcquaGif.mp4",
      vid"ytai_OpsRovesciataAcqua.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ho fatto bene a farlo",
      "non lo volevo fare",
      "mi sto sentendo (bene|in compagnia)".r.tr(20)
    )(
      gif"ytai_NoVideoHoFattoBeneCompagniaGif.mp4",
      mp3"ytai_NoVideoHoFattoBeneCompagnia.mp3",
      vid"ytai_NoVideoHoFattoBeneCompagnia.mp4"
    ),
  )

  def messageRepliesVideoData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "in america",
      "(posto|carico) i video".r.tr(13),
      "restiamo in contatto",
      "attraverso i commenti",
      "sto risolvendo"
    )(
      vid"ytai_SognoAmericano.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "senape",
      "non è scaduta",
      "ha un gusto strano",
      "non ne mangio più",
    )(
      vid"ytai_Senape.mp4"
    ),
    ReplyBundleMessage.textToMedia[F](
      "gigantesco",
      "sushi",
      "che bellezza"
    )(
      vid"ytai_SushiQuestoEGigantescoBellezza.mp4"
    )
  )

  def messageRepliesImageData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "\\b(ar)?rabbi([oa]|at[oa])\\b".r.tr(6),
      "collera",
      "[🤬😡😠]".r.tr(1),
    )(
      pho"ytai_Rabbia.jpg"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[😦😧]".r.tr(1),
      "shock",
    )(
      pho"ytai_Shock.jpg"
    ),
    ReplyBundleMessage.textToMedia[F](
      "😐",
      "attonito"
    )(
      pho"ytai_Attonito.jpg"
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
      )(using Parallel[F], Async[F], botSetup.api, log)
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
      )(using Async[F], botSetup.api, log)
    }
}
