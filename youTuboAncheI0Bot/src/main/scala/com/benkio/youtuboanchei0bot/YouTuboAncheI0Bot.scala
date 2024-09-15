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
    ReplyBundleMessage.textToMp3[F](
      "non vi costa nulla"
    )(
      mp3"ytai_Donazioni.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "bengalino",
      "pappagallo",
      "uccellino",
    )(
      mp3"ytai_BengalinoDiamantino.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "cocod[eè]".r.tr(6),
      "gallina"
    )(
      mp3"ytai_Cocode.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\bmisc\\b".r.tr(4),
      "\\bm[i]+[a]+[o]+\\b".r.tr(4)
    )(
      mp3"ytai_Misc.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "\\btopolin[oi]\\b".r.tr(8)
    )(
      mp3"ytai_Topolino.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "francesismo"
    )(
      mp3"ytai_Francesismo.mp3"
    ),
    ReplyBundleMessage.textToMp3[F](
      "grazie"
    )(
      mp3"ytai_Grazie.mp3",
    ),
    ReplyBundleMessage.textToMp3[F](
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
    ReplyBundleMessage.textToGif[F](
      "reputazione"
    )(
      gif"ytai_LaReputazione.mp4",
      gif"ytai_CheVergogna.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ciccione"
    )(
      gif"ytai_Molla.mp4",
      gif"ytai_CiccioneObesoMangiTanto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "acqua calabria"
    )(
      gif"ytai_AcquaSguardo.mp4",
      gif"ytai_Sete.mp4",
      gif"ytai_AcquaCalabria.mp4",
      gif"ytai_AcquaCalabriaOttima.mp4",
      gif"ytai_AcquaMeravigliosa.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fatica",
      "sudore",
      "sudato"
    )(
      gif"ytai_Affaticamento.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ascolta (queste|le) mie parole".r.tr(21),
      "amareggiati",
      "dedicaci (il tuo tempo|le tue notti)".r.tr(21)
    )(
      gif"ytai_Amareggiati.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\barchitetta\\b".r.tr(10),
      "\\bnotaia\\b".r.tr(6),
      "\\bministra\\b".r.tr(8),
      "\\bavvocata\\b".r.tr(8)
    )(
      gif"ytai_Architetta.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "bel sogno"
    )(
      gif"ytai_BelSogno.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "brivido",
      "fremito",
      "tremito",
      "tremore"
    )(
      gif"ytai_Brivido.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "buonanotte"
    )(
      gif"ytai_Buonanotte.mp4",
      gif"ytai_BuonanotteBrunchPlus.mp4",
      gif"ytai_BuonanotteFollowers.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "(buona )?pizza".r.tr(5)
    )(
      gif"ytai_BuonaPizza.mp4",
      gif"ytai_PizzaAllegria.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "buonasera"
    )(
      gif"ytai_Buonasera.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "che spettacolo"
    )(
      gif"ytai_CheSpettacolo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ciao ragazzi",
      "cari saluti"
    )(
      gif"ytai_CiaoRagazzi.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ci divertiremo",
      "bel percorso"
    )(
      gif"ytai_CiDivertiremoPercorso.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "colore meraviglioso"
    )(
      gif"ytai_ColoreMeraviglioso.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "corpulenti",
      "ciccioni"
    )(
      gif"ytai_CorpulentiCiccioni.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "culetto"
    )(
      gif"ytai_Culetto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ed allora"
    )(
      gif"ytai_EdAllora.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fai pure"
    )(
      gif"ytai_FaiPure.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fallo anche (tu|te)".r.tr(14),
    )(
      gif"ytai_FalloAncheTu.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "filet[ -]?o[ -]?fish".r.tr(10),
    )(
      gif"ytai_FiletOFish.mp4",
      gif"ytai_BagnarloAcqua.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(deluso|insoddisfatto|inappagato|abbattuto|scoraggiato|demoralizzato|depresso|demotivato|avvilito|scocciato)".r
        .tr(6)
    )(
      gif"ytai_Frustrazione.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ci sto già pensando"
    )(
      gif"ytai_GiaPensando.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sono grande",
      "sono corpulento"
    )(
      gif"ytai_GrandeCorpulento.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "grazie dottore",
    )(
      gif"ytai_GrazieDottore.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sconforto grave"
    )(
      gif"ytai_GrazieTante.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "incredibile profumo",
      "incredibile aroma"
    )(
      gif"ytai_IncredibileAromaProfumo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "incredibile",
      "inimitabile",
      "the number (one|1)".r.tr(12)
    )(
      gif"ytai_IncredibileInimitabile.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "io non lo so"
    )(
      gif"ytai_IoNonLoSo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "loro dovere",
      "vostro diritto"
    )(
      gif"ytai_LoroDovereVostroDiritto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "lo so (bene )?anche io".r.tr(14)
    )(
      gif"ytai_LoSoAncheIo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mah"
    )(
      gif"ytai_Mah.mp4",
      gif"ytai_Mah2.mp4",
      gif"ytai_Mah3.mp4",
      gif"ytai_ZoomMah.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "meraviglioso",
    )(
      gif"ytai_Meraviglioso.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "molla",
      "grassone",
      "ancora non sei morto"
    )(
      gif"ytai_Molla.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "molto buona"
    )(
      gif"ytai_MoltoBuona.mp4",
      gif"ytai_MoltoBuona2.mp4",
      gif"ytai_Buona.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "monoporzioni",
      "mezzo (chilo|kg)".r.tr(8),
    )(
      gif"ytai_MonoporzioniTiramisu.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non fa male"
    )(
      gif"ytai_NonFaMale.mp4",
      gif"ytai_AcquaMiglioreDrink.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non la crede nessuno questa cosa",
      "non ci crede nessuno"
    )(
      gif"ytai_NonLaCredeNessuno.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non pensiamoci più"
    )(
      gif"ytai_NonPensiamoci.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "incomprensione",
      "non vi capiscono"
    )(
      gif"ytai_NonViCapiscono.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "occhiolino",
      ";)",
      "😉",
    )(
      gif"ytai_Occhiolino.mp4",
      gif"ytai_Occhiolino2.mp4",
      gif"ytai_Occhiolino3.mp4",
      gif"ytai_OcchiolinoTestaDondolante.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "olè",
    )(
      gif"ytai_Ole.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "olè anche io",
    )(
      gif"ytai_OleAncheIo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "la perfezione",
      "la nostra tendenza"
    )(
      gif"ytai_PerfezioneTendenza.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "profumo meraviglioso",
    )(
      gif"ytai_ProfumoMeraviglioso.mp4",
      gif"ytai_ProfumoGamberettiSalmone.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sentiamo il profumo"
    )(
      gif"ytai_ProfumoMeraviglioso.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ringraziamento",
    )(
      gif"ytai_Ringraziamento.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "se non mi aiuta",
      "cosa mi aiuta"
    )(
      gif"ytai_SentiteCheRoba.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sete",
      "(sorso|bicchiere) d'acqua".r.tr(13)
    )(
      gif"ytai_Sete.mp4",
      gif"ytai_AcquaMeravigliosa.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "🤷"
    )(
      gif"ytai_Shrug.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sta per dire qualcosa",
    )(
      gif"ytai_Silenzio.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "silenzio[,]? silenzio".r.tr(17),
    )(
      gif"ytai_Silenzio.mp4",
      gif"ytai_SilenzioMomentoMagico.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "si v[àa] finch[eé] si v[aà]".r.tr(18),
      "quando non si potrà andare più",
      "è tanto facile"
    )(
      gif"ytai_SiVaFincheSiVa.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sorprendente",
    )(
      gif"ytai_Sorprendente.mp4",
      gif"ytai_SecondoBocconeSorprendente.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(😄|😀|😃){3,}".r.tr(3),
      "sorriso"
    )(
      gif"ytai_Sorriso.mp4",
      gif"ytai_Sorriso2.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
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
    ReplyBundleMessage.textToGif[F](
      "l'unica cosa che sai fare",
    )(
      gif"ytai_UnicaCosaMangiare.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "uno o due"
    )(
      gif"ytai_UnoODue.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "zenzero",
      "mia risposta"
    )(
      gif"ytai_Zenzero.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "zoom",
      "guardo da vicino"
    )(
      gif"ytai_Zoom.mp4",
      gif"ytai_ZoomMah.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "tanti auguri",
    )(
      gif"ytai_TantiAuguri.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "auguri di gusto",
    )(
      gif"ytai_AuguriDiGusto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "bagnarlo"
    )(
      gif"ytai_BagnarloAcqua.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "benvenuti",
      "ora e sempre"
    )(
      gif"ytai_Benvenuti.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "buon appetito"
    )(
      gif"ytai_BuonAppetito.mp4",
      gif"ytai_BuonAppetito2.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "diploma",
      "per pisciare",
      "ma (che )?stiamo scherzando".r.tr(20)
    )(
      gif"ytai_DiplomaPisciare.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "non (mi sento|sto) bene".r.tr(12)
    )(
      gif"ytai_DiversiGioniNonStoBene.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(grazie e )?arrivederci".r.tr(11),
    )(
      gif"ytai_GrazieArrivederci.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "eccitato",
    )(
      gif"ytai_MoltoEccitato.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "no pain",
      "no gain"
    )(
      gif"ytai_NoPainNoGain.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non hai più scuse",
      "riprenditi",
      "sei in gamba",
    )(
      gif"ytai_NoScuse.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "caspita",
      "sono (grosso|sono (quasi )?enorme|una palla di lardo)".r.tr(11),
    )(
      gif"ytai_PallaDiLardo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mette paura",
    )(
      gif"ytai_Paura.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "per voi",
    )(
      gif"ytai_PerVoi.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "alimenti",
      "allegria",
    )(
      gif"ytai_PizzaAllegria.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "gamberetti"
    )(
      gif"ytai_ProfumoGamberettiSalmone.mp4",
      gif"ytai_Sorpresa.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bproviamo\\b".r.tr(8),
      "senza morire",
    )(
      gif"ytai_ProviamoSenzaMorire.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "quello che riesco a fare",
    )(
      gif"ytai_RiescoAFare.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sentendo (davvero )?male".r.tr(13),
    )(
      gif"ytai_SentendoDavveroMale.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "momento magico",
    )(
      gif"ytai_SilenzioMomentoMagico.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sopraffino",
    )(
      gif"ytai_Sopraffino.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "😮",
      "😯",
    )(
      gif"ytai_Sorpresa.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "staccato (quasi )?il naso".r.tr(16),
    )(
      gif"ytai_StaccatoNaso.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "cuc[uù]+".r.tr(4),
    )(
      gif"ytai_Cucu.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "al[e]+ [o]{2,}".r.tr(6),
    )(
      gif"ytai_AleOoo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fare senza",
      "faenza",
    )(
      gif"ytai_SenzaFaenza.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "caffè",
    )(
      gif"ytai_BuonCaffeATutti.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "capolavoro",
      "\\bmeravigliosa\\b".r.tr(12),
    )(
      gif"ytai_CapolavoroMeravigliosa.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ce la puoi fare",
    )(
      gif"ytai_CeLaPuoiFare.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "che profumo"
    )(
      gif"ytai_CheProfumo.mp4",
      gif"ytai_ECheProfumo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "eccolo qua"
    )(
      gif"ytai_EccoloQua.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non mi piacciono",
    )(
      gif"ytai_NonMiPiaccionoQuesteCose.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "nonno"
    )(
      gif"ytai_NonnoMito.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "soltanto per questo",
      "denaro",
      "guadagno",
    )(
      gif"ytai_SoltantoPerQuesto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "dubbi enciclopedici",
      "rifletteteci",
    )(
      gif"ytai_DubbiEnciclopedici.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "miei limiti",
    )(
      gif"ytai_Limiti.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "panino"
    )(
      gif"ytai_PaninoBuonoSpuntito.mp4",
      gif"ytai_Panino.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "m[ ]?&[ ]?m['s]?".r.tr(3),
      "rotear",
      "ruotar",
    )(
      gif"ytai_M&Ms.mp4",
      gif"ytai_M&MsLoop.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "(😂|🤣){3,}".r.tr(4),
      "(ah|ha){5,}".r.tr(14)
    )(
      gif"ytai_Risata.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "obeso",
      "te lo meriti",
      "mangi tanto"
    )(
      gif"ytai_CiccioneObesoMangiTanto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "si vede"
    )(
      gif"ytai_SiVede.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "correre non serve",
      "\\bfretta\\b".r.tr(6)
    )(
      gif"ytai_CorrereNonServe.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "marmellata",
      "santa rosa"
    )(
      gif"ytai_TorreMarmellata.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "(la|una) torre".r.tr(8)
    )(
      gif"ytai_TorreMarmellata.mp4",
      gif"ytai_TorreKinderFettaLatte.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "salmone",
    )(
      gif"ytai_SalmoneUnico.mp4",
      gif"ytai_TartinaSalmone.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "un successo",
      "agrodolci",
    )(
      gif"ytai_SaporiAgrodolciSpettacolari.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non parlate di",
      "questioni filosofiche",
      "non ci azzecc"
    )(
      gif"ytai_QuestioniFilosofiche.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mang[ai] in (mia )?compagnia".r.tr(18),
    )(
      gif"ytai_InCompagnia.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mi incontrate",
      "la pancia",
      "in imbarazzo"
    )(
      gif"ytai_IncontratePanciaImbarazzo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "caro amico",
      "chiarire",
    )(
      gif"ytai_GrazieAmicoChiarire.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "comm(uo|o)vendo".r.tr(10)
    )(
      gif"ytai_Commovendo.mp4",
      gif"ytai_CommuovendoFareQuelloChePiace.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "coda"
    )(
      gif"ytai_CodaLunga.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "brindare",
      "drink",
    )(
      gif"ytai_AcquaMiglioreDrink.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "forchetta",
    )(
      gif"ytai_MancaForchetta.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "solo[?]{2,}".r.tr(5),
    )(
      gif"ytai_Solo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ottimo[!]+".r.tr(5),
    )(
      gif"ytai_Ottimo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sei uno sfigato"
    )(
      gif"ytai_SeiUnoSfigato.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non prendetevi confidenze",
      "inteso di darvi"
    )(
      gif"ytai_Confidenze.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "illegale"
    )(
      gif"ytai_IllegaleFuorilegge.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ne ho già parlato",
      "tornare sugli stessi punti",
      "lamentato con me"
    )(
      gif"ytai_NeHoGiaParlato.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "alla salute"
    )(
      gif"ytai_AllaSalute.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "grazie ragazzi",
      "grazie a tutti"
    )(
      gif"ytai_GrazieRagazzi.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mi (reputi|consideri) intelligente".r.tr(22),
      "mi (reputi|consideri) sensibile".r.tr(19),
      "sensibile e intelligente"
    )(
      gif"ytai_SensibileIntelligente.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "dico basta"
    )(
      gif"ytai_AdessoViDicoBasta.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "che bontà",
      "eccoli qua",
    )(
      gif"ytai_ECheProfumo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "parlo poco",
      "ingozzo",
      "non ve la prendete",
      "vostra compagnia",
    )(
      gif"ytai_SeParloPoco.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "meno di un minuto"
    )(
      gif"ytai_TiramisuMenoDiUnMinuto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fetta al latte"
    )(
      gif"ytai_TorreKinderFettaLatte.mp4",
      gif"ytai_KinderFettaAlLatte.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "lasciate libera",
      "linea telefonica",
      "per cose reali",
      "che mi serve"
    )(
      gif"ytai_LineaTelefonica.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ce l'ho fatta",
      "\\bp(à|a')\\b".r.tr(2)
    )(
      gif"ytai_CeLhoFatta.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "grazie tante"
    )(
      gif"ytai_GrazieTante.mp4",
      gif"ytai_GrazieTante2.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "beviamoci sopra",
      "non c'è alcohol",
    )(
      gif"ytai_BeviamociSopraNoAlcohol.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "cercherò di prepararlo",
    )(
      gif"ytai_CercheroDiPrepararlo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "dipende da come mi sento"
    )(
      gif"ytai_DipendeDaComeMiSento.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "eccellente"
    )(
      gif"ytai_Eccellente.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non fatelo più"
    )(
      gif"ytai_NonFateloPiu.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "molto dolce",
      "molto buono"
    )(
      gif"ytai_MoltoDolceMoltoBuono.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ne avevo proprio voglia"
    )(
      gif"ytai_NeAvevoProprioVoglia.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "c'entra (quasi )?sempre".r.tr(14),
    )(
      gif"ytai_CentraQuasiSempre.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "riesco a darvi",
      "imparare (anche io )?(un po' )?di più".r.tr(15),
    )(
      gif"ytai_DarviImparare.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "forte profumo",
    )(
      gif"ytai_ForteProfumoMiele.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "😋",
      "yum",
      "gustoso",
    )(
      gif"ytai_GestoGustoso.mp4",
      gif"ytai_MoltoGustoso.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mia filosofia",
      "risol(to|vere) con tutti".r.tr(15),
    )(
      gif"ytai_HoRisoltoConTutti.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "sentendo in compagnia"
    )(
      gif"ytai_MiStoSentendoInCompagnia.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "vengono le parole",
      "intendi dire",
    )(
      gif"ytai_NoParoleMostraIntenzioni.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "quello che ci voleva",
      "bibita (bella )?fresca".r.tr(13),
    )(
      gif"ytai_QuestaBibitaBellaFresca.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "secondo boccone",
    )(
      gif"ytai_SecondoBocconeSorprendente.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "veterano",
      "chat day",
    )(
      gif"ytai_VeteranoChatDays.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "vivete questo momento"
    )(
      gif"ytai_ViveteQuestoMomentoConMe.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\btremo\\b".r.tr(5),
      "le mie condizioni"
    )(
      gif"ytai_Tremo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fallo anche (te|tu)".r.tr(14),
      "\\bcome me\\b".r.tr(5)
    )(
      gif"ytai_FalloAncheTeComeMe.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "cercando di fare",
      "del mio meglio"
    )(
      gif"ytai_FareDelMioMeglio.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "limitato molto",
      "essere privato",
      "questi soldi",
    )(
      gif"ytai_PrivatoSoldiLimitatoMolto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bball(o|are|i)\\b".r.tr(5),
      "\\bdanz(a|are|i)\\b".r.tr(5),
    )(
      gif"ytai_Ballo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "zebra",
      "giraffa",
    )(
      gif"ytai_ZebraGiraffa.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "smart"
    )(
      gif"ytai_SpuntinoSmart.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ipocrita"
    )(
      gif"ytai_SonoIpocrita.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "imbarazz(o|ato)".r.tr(9)
    )(
      gif"ytai_SentireInImbarazzo.mp4",
      gif"ytai_LeggermenteImbarazzato.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "nel bene",
      "nel male",
    )(
      gif"ytai_CiaoFollowersNelBeneNelMale.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "se volete sapere",
      "100%",
      "non va per me",
    )(
      gif"ytai_SapereTuttoNonVa.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "molto aromatico",
      "affumicatura",
      "pepe nero",
    )(
      gif"ytai_MoltoAromatico.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ci riusciremo",
    )(
      gif"ytai_NonViPreoccupateCiRiusciremo.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "non vi preoccupate",
    )(
      gif"ytai_NonViPreoccupateCiRiusciremo.mp4",
      gif"ytai_BeviamociSopraNoAlcohol.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fuorilegge"
    )(
      gif"ytai_NonSonoFuorileggeNecessita.mp4",
      gif"ytai_IllegaleFuorilegge.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "ho necessità"
    )(
      gif"ytai_NonSonoFuorileggeNecessita.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "mi fa (tanto )?piacere".r.tr(12)
    )(
      gif"ytai_MiFaTantoPiacere.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "\\bshow\\b".r.tr(4)
    )(
      gif"ytai_LoShowDeveContunuare.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "felice di ritrovarvi"
    )(
      gif"ytai_FeliceDiRitrovarvi.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "fa(re|rti|tti) (due |i )?conti".r.tr(9),
      "il lavoro che fai",
    )(
      gif"ytai_FattiDueConti.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "inquietante",
    )(
      gif"ytai_NonInquietante.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "vediamo un po'",
    )(
      gif"ytai_VediamoUnPo.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "rinfrescante",
      "calabria",
      "s(\\.)?r(\\.)?l(\\.)?".r.tr(3),
    )(
      gif"ytai_RinfrescanteDiCalabria.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "prima o poi"
    )(
      gif"ytai_NonViPreoccupateCiRiusciremo.mp4",
      gif"ytai_PassioneAllevareGalline.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "allevare",
      "galline",
      "mi[ae] passion[ie]".r.tr(12)
    )(
      gif"ytai_PassioneAllevareGalline.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "com'è venuto",
      "perfetto"
    )(
      gif"ytai_Perfetto.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "più piccoline",
      "sono dolci",
      "al punto giusto",
    )(
      gif"ytai_DolciAlPuntoGiusto.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "amarognolo",
      "si intona",
      "non guasta",
      "contrasto"
    )(
      gif"ytai_ContrastoAmarognolo.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "bella fresca"
    )(
      gif"ytai_BellaFresca.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "piace mangiare così",
      "critiche"
    )(
      gif"ytai_MangiareCritiche.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "ostica",
      "insalata"
    )(
      gif"ytai_OsticaInsalata.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "\\blotte\\b".r.tr(5),
      "condivido"
    )(
      gif"ytai_PersonaliLotteFollowers.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "condizione umana",
      "patologico",
      "individuo che comunque vive",
    )(
      gif"ytai_CondizioneUmana.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "occhiali",
      "👓",
      "🤓",
    )(
      gif"ytai_SistemazioneOcchiali.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
      "miele",
    )(
      gif"ytai_ForteProfumoMiele.mp4",
      gif"ytai_AppiccicaticcioMiele.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "appiccicaticcio",
    )(
      gif"ytai_AppiccicaticcioMiele.mp4"
    ),
    ReplyBundleMessage.textToGif[F](
      "scaldato",
      "panini",
      "sono ottimi",
    )(
      gif"ytai_GrazieScaldatoPanini.mp4",
    ),
    ReplyBundleMessage.textToGif[F](
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
      "ricordami fino a domani"
    )(
      gif"ytai_Ricordami.mp4",
      vid"ytai_RicordamiFinoADomani.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "ho perso (di nuovo )?qualcosa".r.tr(18)
    )(
      gif"ytai_HoPersoQualcosa.mp4",
      mp3"ytai_HoPersoQualcosa.mp3"
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
    )(
      mp3"ytai_BuonSalameCariAmici.mp3",
      vid"ytai_BuonSalameCariAmici.mp4",
      gif"ytai_BuonSalameCariAmiciGif.mp4",
    ),
    ReplyBundleMessage.textToMedia[F](
      "cari amici"
    )(
      mp3"ytai_BuonSalameCariAmici.mp3",
      vid"ytai_BuonSalameCariAmici.mp4",
      gif"ytai_BuonSalameCariAmiciGif.mp4",
      gif"ytai_CariAmiciFollowersBuongiornoSabatoGif.mp4",
      vid"ytai_CariAmiciFollowersBuongiornoSabato.mp4",
      mp3"ytai_CariAmiciFollowersBuongiornoSabato.mp3"
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
    ReplyBundleMessage.textToMedia[F](
      "venticinque (milioni|mila euro)".r.tr(19)
    )(
      gif"ytai_25MilioniVisualizzazioni25MilaEuroGif.mp4",
      vid"ytai_25MilioniVisualizzazioni25MilaEuro.mp4",
      mp3"ytai_25MilioniVisualizzazioni25MilaEuro.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "autori",
      "massicci piedi",
    )(
      gif"ytai_AutoriMassicciPiediGif.mp4",
      vid"ytai_AutoriMassicciPiedi.mp4",
      mp3"ytai_AutoriMassicciPiedi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "criceto",
      "🐹"
    )(
      gif"ytai_BellaTecnicaDelCricetoGif.mp4",
      vid"ytai_BellaTecnicaDelCriceto.mp4",
      mp3"ytai_BellaTecnicaDelCriceto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "followers",
    )(
      gif"ytai_CiaoFollowersNelBeneNelMale.mp4",
      gif"ytai_BuonanotteFollowers.mp4",
      gif"ytai_CiaoCariAmiciFollowers.mp4",
      gif"ytai_PersonaliLotteFollowers.mp4",
      gif"ytai_CariAmiciFollowersBuongiornoSabatoGif.mp4",
      vid"ytai_CariAmiciFollowersBuongiornoSabato.mp4",
      mp3"ytai_CariAmiciFollowersBuongiornoSabato.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "allora l[iì]".r.tr(9)
    )(
      gif"ytai_AlloraLi.mp4",
      mp3"ytai_AlloraLi.mp3"
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
      "verza",
      "cavolo cappuccio",
      "giuseppe",
      "ma che m(i |')hai detto".r.tr(8),
    )(
      gif"ytai_VerzaGiuseppeGif.mp4",
      vid"ytai_VerzaGiuseppe.mp4"
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
      "tiramisù",
    )(
      gif"ytai_MonoporzioniTiramisu.mp4",
      gif"ytai_Tiramisu.mp4",
      mp3"ytai_CucchiaioBattagliaTiramisu.mp3",
      vid"ytai_CucchiaioBattagliaTiramisu.mp4",
      gif"ytai_CucchiaioBattagliaTiramisuGif.mp4",
      gif"ytai_LoopAperturaTiramisu.mp4"
    ),
    ReplyBundleMessage.textToMedia[F]("ciao!")(
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
    ReplyBundleMessage.textToMedia[F]("buongiorno", "buon sabato")(
      gif"ytai_CariAmiciFollowersBuongiornoSabatoGif.mp4",
      vid"ytai_CariAmiciFollowersBuongiornoSabato.mp4",
      mp3"ytai_CariAmiciFollowersBuongiornoSabato.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "eh (no|allora)".r.tr(5),
      "imbeccare",
      "bengalini"
    )(
      gif"ytai_ImbeccareComeBengaliniGif.mp4",
      vid"ytai_ImbeccareComeBengalini.mp4",
      mp3"ytai_ImbeccareComeBengalini.mp3"
    )
  )

  def messageRepliesVideoData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToVideo[F](
      "in america",
      "(posto|carico) i video".r.tr(13),
      "restiamo in contatto",
      "attraverso i commenti",
      "sto risolvendo"
    )(
      vid"ytai_SognoAmericano.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
      "senape",
      "non è scaduta",
      "ha un gusto strano",
      "non ne mangio più",
    )(
      vid"ytai_Senape.mp4"
    ),
    ReplyBundleMessage.textToVideo[F](
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
