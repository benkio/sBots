package com.benkio.m0sconibot

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

class M0sconiBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    val resourceAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F](resourceAccess)
    with M0sconiBot[F] {
  override def resourceAccess(using syncF: Sync[F]): ResourceAccess[F] = resourceAccess
  override def postComputation(using appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(using
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

class M0sconiBotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends BotSkeletonWebhook[F](uri, path, webhookCertificate, resAccess)
    with M0sconiBot[F] {
  override def resourceAccess(using syncF: Sync[F]): ResourceAccess[F] = resAccess
  override def postComputation(using appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(using
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

trait M0sconiBot[F[_]] extends BotSkeleton[F] {

  override val botName: String                     = M0sconiBot.botName
  override val botPrefix: String                   = M0sconiBot.botPrefix
  override val ignoreMessagePrefix: Option[String] = M0sconiBot.ignoreMessagePrefix
  override val triggerFilename: String             = M0sconiBot.triggerFilename
  override val triggerListUri: Uri                 = M0sconiBot.triggerListUri
  val backgroundJobManager: BackgroundJobManager[F]

  override def messageRepliesDataF(using
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    M0sconiBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(using asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    M0sconiBot
      .commandRepliesData[F](
        dbLayer = dbLayer
      )
      .pure[F]

}
object M0sconiBot {

  val ignoreMessagePrefix: Option[String] = Some("!")
  val triggerFilename: String             = "mos_triggers.txt"
  val botName: String                     = "M0sconiBot"
  val botPrefix: String                   = "mos"
  val triggerListUri: Uri                 = uri"https://github.com/benkio/sBots/blob/master/m0sconiBot/mos_triggers.txt"
  val tokenFilename: String               = "mos_M0sconiBot.token"
  val configNamespace: String             = "mosDB"

  def messageRepliesAudioData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToMedia[F](
      "va l[aà]".r.tr(5)
    )(
      mp3"mos_ChiudiQuellaPortaPerFavore.mp3",
      mp3"mos_AspettaUnSecondoDioCane.mp3",
      mp3"mos_DaiVaLaRipartiamoSubito.mp3",
      mp3"mos_VaffanuloDai.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(quella|la) porta".r.tr(8)
    )(
      mp3"mos_AvantiNdrioConQuellaPortaLi.mp3",
      mp3"mos_ChiEQuelMonaSbatteLaPortaChiudeUrlando.mp3",
      mp3"mos_ChiudiQuellaPortaDai.mp3",
      mp3"mos_ChiudiQuellaPortaPerFavore.mp3",
      mp3"mos_SerraLaPortaDioCane.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bmona\\b".r.tr(4)
    )(
      mp3"mos_AndateInMona.mp3",
      mp3"mos_ChiEQuelMonaSbatteLaPortaChiudeUrlando.mp3",
      mp3"mos_EntraDentroMona.mp3",
      mp3"mos_NoVaInMonaNonTornoIndrio.mp3",
      mp3"mos_VaInMonaCortesiaSimpatiaPorcoDioVaInCasinoVaffanculo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dio cane"
    )(
      mp3"mos_AndateInMona.mp3",
      mp3"mos_AntonioFossoCarteColla.mp3",
      mp3"mos_AspettaUnSecondoDioCane.mp3",
      mp3"mos_AvantiNdrioConQuellaPortaLi.mp3",
      mp3"mos_BasketDioCaneDipenDipanDaCapo.mp3",
      mp3"mos_DioCane.mp3",
      mp3"mos_DioCane2.mp3",
      mp3"mos_DioCaneLaMadonna.mp3",
      mp3"mos_DioCaneMaNonEPossibile.mp3",
      mp3"mos_DioCaneX3MadonnaPuttana.mp3",
      mp3"mos_DioSchifosoCan.mp3",
      mp3"mos_InPrimoPianoIlCalcioDioSchifosoCan.mp3",
      mp3"mos_Innervosire.mp3",
      mp3"mos_MagistratoCuratoreFallimentareExDioPorcoDioCane.mp3",
      mp3"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3",
      mp3"mos_SerraLaPortaDioCane.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "madonna"
    )(
      mp3"mos_DioCaneLaMadonna.mp3",
      mp3"mos_DioCaneX3MadonnaPuttana.mp3",
      mp3"mos_DioPorcaMadonnaDeDio.mp3",
      mp3"mos_MaEPossibilePortannaLaMadonna.mp3",
      mp3"mos_MadonnaDeDio.mp3",
      mp3"mos_MadonnaPuttana.mp3",
      mp3"mos_MadonnaPuttina.mp3",
      mp3"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3",
      mp3"mos_TennisVaffanculoDioPorcoMadonnaPuttana.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dio porco"
    )(
      mp3"mos_BasketSeiVantaggiDioPorco.mp3",
      mp3"mos_BresciaFoggia.mp3",
      mp3"mos_CittaDioPorco.mp3",
      mp3"mos_CoppaDelleCoppeSampdoriaDioPorco.mp3",
      mp3"mos_DioPorcaMadonnaDeDio.mp3",
      mp3"mos_DioPorco.mp3",
      mp3"mos_DioPorco2.mp3",
      mp3"mos_DioPorco3.mp3",
      mp3"mos_DioPorcoCheNotiziaDioCaneBoia.mp3",
      mp3"mos_MagistratoCuratoreFallimentareExDioPorcoDioCane.mp3",
      mp3"mos_SeVeniteAvantiAncoraViDoUnPunio.mp3",
      mp3"mos_TennisVaffanculoDioPorcoMadonnaPuttana.mp3",
      mp3"mos_VaffanculoDioPorco.mp3",
      mp3"mos_VaffanuloDai.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dio boia"
    )(
      mp3"mos_DioPorcoCheNotiziaDioCaneBoia.mp3",
      mp3"mos_NoVaInMonaNonTornoIndrio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "de dio"
    )(
      mp3"mos_BresciaFoggia.mp3",
      mp3"mos_DioPorcaMadonnaDeDio.mp3",
      mp3"mos_MadonnaDeDio.mp3",
      mp3"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3",
      mp3"mos_PilotaRomanoAndreaDeCesariDioCanariaDeDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "a[h]? n[o]?n lo so".r.tr(11)
    )(
      mp3"mos_AhNonLoSo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non posso (mica )?improvvisar(e|me)".r.tr(21),
    )(
      mp3"mos_AndateInMona.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "andiamo dai",
      "lo fa apposta"
    )(
      mp3"mos_AndiamoDai.mp3",
      mp3"mos_MarioFerrettoPorcoDioRumoreLiFaAposta.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\b(in)?colla\\b".r.tr(5),
      "le carte"
    )(
      mp3"mos_DioBonDiUnDioLeSeaTagaENonLeSeStaga.mp3",
      mp3"mos_AntonioFossoCarteColla.mp3",
      mp3"mos_GuardaCheRobaLaCollaAttaccataAlleCarte.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dio bono"
    )(
      mp3"mos_DioBonDiUnDioLeSeaTagaENonLeSeStaga.mp3",
      mp3"mos_DioBono.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "antonio fosso",
      "neo brigatista",
    )(
      mp3"mos_AntonioFossoCarteColla.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ostia",
    )(
      mp3"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3",
      mp3"mos_AntonioFossoCarteColla.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "aspetta un secondo",
    )(
      mp3"mos_AspettaUnSecondoDioCane.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "avanti (indrio|indietro)".r.tr(13),
    )(
      mp3"mos_AvantiNdrioConQuellaPortaLi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "marco bisagno",
      "la minoranza",
      "assemblea",
      "irregolare",
    )(
      mp3"mos_AvvocatoMarcoBisagnoAssembleaIrregolareMinoranze.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "basket",
      "pallacanestro",
    )(
      mp3"mos_BasketSeiVantaggiDioPorco.mp3",
      mp3"mos_BasketDioCaneDipenDipanDaCapo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "palasport",
      "piazzale olimpia",
      "udine",
      "di[tp]an".r.tr(5),
      "diche",
      "\\bdipen\\b".r.tr(5),
    )(
      mp3"mos_BasketDioCaneDipenDipanDaCapo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "manovre offersive",
      "imprecisi al tiro",
      "sei vantaggi"
    )(
      mp3"mos_BasketSeiVantaggiDioPorco.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "quadro completo",
      "tanto tempo anche",
      "brescia(-| )foggia".r.tr(14),
      "cagliari(-| )roma".r.tr(13),
      "fiorentina(-| )milan".r.tr(16),
      "genova(-| )atalanta".r.tr(15),
      "\\binter\\b".r.tr(5)
    )(
      mp3"mos_BresciaFoggia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "buongiorno"
    )(
      mp3"mos_Buongiorno.mp3",
      mp3"mos_Buongiorno2.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "stata una giornata"
    )(
      mp3"mos_Buongiorno2.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "but(tato|ta) per aria tutto".r.tr(22)
    )(
      mp3"mos_ButtaPellAria.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cos'è che è caduto"
    )(
      mp3"mos_Caduto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non si riesce a capire un cazzo"
    )(
      mp3"mos_CapireUnCazzo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "chiude urlando"
    )(
      mp3"mos_ChiEQuelMonaSbatteLaPortaChiudeUrlando.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "chissà che non m(i |')incazzo".r.tr(26)
    )(
      mp3"mos_ChissaCheNonMIncazzaEh.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mancalacqua",
      "lugagnano",
      "vigasio",
      "poveliano",
      "tosse",
    )(
      mp3"mos_CiclismoAllieviDio.mp3",
      mp3"mos_TosseDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ciclismo"
    )(
      mp3"mos_CiclismoAllieviDio.mp3",
      mp3"mos_CiclismoGianniBugnoRitardo.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      "nostra città"
    )(
      mp3"mos_CittaDioPorco.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "coppa delle coppe",
      "sampdoria"
    )(
      mp3"mos_CoppaDelleCoppeSampdoriaDioPorco.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "orco dio"
    )(
      mp3"mos_CortiOrcoDio.mp3",
      mp3"mos_Formula1DonningtonPrimaSessioneVaffanculo.mp3",
      mp3"mos_OrcoDio.mp3",
      mp3"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "che cazzo ([cg]he )è qua sotto".r.tr(25)
    )(
      mp3"mos_CosaCheCazzoGheE.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ripartiamo subito"
    )(
      mp3"mos_DaiVaLaRipartiamoSubito.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bsigla\\b".r.tr(5)
    )(
      mp3"mos_DallaSiglaDai.mp3",
      mp3"mos_Sigla.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\b(difa|ddbb|babba)\\b".r.tr(5),
      "farfugl(i|a|iare|iamento)".r.tr(7)
    )(
      mp3"mos_Difabbddffbbaa.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non è possibile"
    )(
      mp3"mos_DioCaneMaNonEPossibile.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dio (pa){3,}".r.tr(6)
    )(
      mp3"mos_DioPaPaPaPaPaPa.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "che notizia"
    )(
      mp3"mos_DioPorcoCheNotiziaDioCaneBoia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dio schifoso"
    )(
      mp3"mos_DioSchifosoCan.mp3",
      mp3"mos_InPrimoPianoIlCalcioDioSchifosoCan.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dios"
    )(
      mp3"mos_Dios.mp3",
      mp3"mos_RiprendoDallaTabella.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "entra dentro"
    )(
      mp3"mos_EntraDentroMona.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "formula (uno|1)".r.tr(9),
      "donington",
      "prima sessione"
    )(
      mp3"mos_Formula1DonningtonPrimaSessioneVaffanculo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vaffanculo"
    )(
      mp3"mos_Formula1DonningtonPrimaSessioneVaffanculo.mp3",
      mp3"mos_TennisVaffanculoDioPorcoMadonnaPuttana.mp3",
      mp3"mos_VaInMonaCortesiaSimpatiaPorcoDioVaInCasinoVaffanculo.mp3",
      mp3"mos_Vaffanculo.mp3",
      mp3"mos_VaffanculoDioPorco.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ges[uù] cristo".r.tr(11),
      "no nessuno",
    )(
      mp3"mos_GesuCristoNoNessuno.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "giochi olimpici",
      "giorgio mazzetta",
      "giorgio mazzetta",
      "m[ie] fermo un (secondo|attimo)".r.tr(18),
    )(
      mp3"mos_GiochiOlimpiciAspettaCheMeFermoUnAttimo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "girato le foto"
    )(
      mp3"mos_GiratoLeFoto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "imbecilli"
    )(
      mp3"mos_Imbecilli.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "in primo piano"
    )(
      mp3"mos_InPrimoPianoIlCalcioDioSchifosoCan.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi ha fatto innervosire",
      "gli spacco la testa",
    )(
      mp3"mos_Innervosire.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "juventus",
      "austria",
      "vienna",
    )(
      mp3"mos_JuventusAustriaViennaPorcoDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "porco dio"
    )(
      mp3"mos_MaPorcoDio.mp3",
      mp3"mos_JuventusAustriaViennaPorcoDio.mp3",
      mp3"mos_MarioFerrettoPorcoDioRumoreLiFaAposta.mp3",
      mp3"mos_PiovanelliPorcoDio.mp3",
      mp3"mos_PorcoDio4.mp3",
      mp3"mos_QuotazioniPorcoDio.mp3",
      mp3"mos_TelefonoPorcoDio.mp3",
      mp3"mos_VaInMonaCortesiaSimpatiaPorcoDioVaInCasinoVaffanculo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "lo troverò",
      "prima o dopo",
    )(
      mp3"mos_LoTroveroDeficiente.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ma è possibile"
    )(
      mp3"mos_MaEPossibilePortannaLaMadonna.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "puttina"
    )(
      mp3"mos_MadonnaPuttina.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "il commercialista",
      "il magistrato",
      "gianfranco bertani",
    )(
      mp3"mos_MagistratoCuratoreFallimentareExDioPorcoDioCane.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mario ferretto",
      "accolto con sorpresa",
      "rumore li",
    )(
      mp3"mos_MarioFerrettoPorcoDioRumoreLiFaAposta.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi vedo già"
    )(
      mp3"mos_MeVedoGia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "mi sono distratto",
      "mi hai distratto"
    )(
      mp3"mos_MiSonoDistratto.mp3",
      mp3"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non torno (indietro|indrio)".r.tr(16)
    )(
      mp3"mos_NoVaInMonaNonTornoIndrio.mp3",
      mp3"mos_NonTornoIndietroDonadoni.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "monitor"
    )(
      mp3"mos_NonCapiscoPiuNienteMonitor.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non capisco più niente",
      "non riesco a capire più niente"
    )(
      mp3"mos_NonRiescoACapireNiente.mp3",
      mp3"mos_NonCapiscoPiuNienteMonitor.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "donadoni"
    )(
      mp3"mos_NonTornoIndietroDonadoni.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "in maiuscolo",
    )(
      mp3"mos_NotizieInMaiuscolo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "notizie"
    )(
      mp3"mos_NotizieScritteDaCul.mp3",
      mp3"mos_NotizieInMaiuscolo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "da capo",
      "venite dentro",
    )(
      mp3"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "orientarmi"
    )(
      mp3"mos_Orientarmi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "andrea de cesari",
      "romano",
      "dio canaia",
      "il pilota",
    )(
      mp3"mos_PilotaRomanoAndreaDeCesariDioCanariaDeDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "piovanelli",
      "atalanta"
    )(
      mp3"mos_PiovanelliPorcoDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dio bub[uù]".r.tr(8)
    )(
      mp3"mos_PiproviamoDioBubu.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "quotazioni"
    )(
      mp3"mos_QuotazioniPorcoDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ricominciamo da capo"
    )(
      mp3"mos_RicominciamoDaCapo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dalla tabella"
    )(
      mp3"mos_RiprendoDallaTabella.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "sbattere le porte"
    )(
      mp3"mos_SbattereLePorte.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "se non bestemmio"
    )(
      mp3"mos_SeNonBestemmioGuarda.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vi do un pu[g]?nio".r.tr(14),
      "se venite avanti"
    )(
      mp3"mos_SeVeniteAvantiAncoraViDoUnPunio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(sei|6) vantaggi".r.tr(10)
    )(
      mp3"mos_SeiVantaggiDiLunghezza.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tecnocop",
      "verona",
    )(
      mp3"mos_TecnocopVerona.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "telefono"
    )(
      mp3"mos_TelefonoPorcoDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tennis"
    )(
      mp3"mos_TennisVaffanculoDioPorcoMadonnaPuttana.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tocca ferro"
    )(
      mp3"mos_ToccaFerro.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "tutte le volte che"
    )(
      mp3"mos_TutteLeVolteCheParto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cortesia",
      "simpatia",
      "va in casino",
    )(
      mp3"mos_VaInMonaCortesiaSimpatiaPorcoDioVaInCasinoVaffanculo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vice sindaco"
    )(
      mp3"mos_ViceSindaco.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vedo tutto tranne",
      "quello che dovrei vedere"
    )(
      mp3"mos_VedoTuttoMenoQuelloCheDovreiVedere.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "cesena",
      "\\btorino\\b".r.tr(6),
      "avellino",
      "walter shakner",
      "austriaco"
    )(
      mp3"mos_WalterShaknerAustriacoCesenaTorinoAvellino.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "gaetano"
    )(
      mp3"mos_Gaetano.mp3"
    )
  )

  def messageRepliesGifData[
      F[_]
  ]: List[ReplyBundleMessage[F]] = List()

  def messageRepliesSpecialData[
      F[_]
  ]: List[ReplyBundleMessage[F]] = List()

  def messageRepliesData[
      F[_]: Applicative
  ]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesSpecialData[F])
      .sorted(ReplyBundle.orderingInstance[F])
      .reverse

  def commandRepliesData[
      F[_]: Async
  ](
      dbLayer: DBLayer[F]
  )(using
      log: LogWriter[F]
  ): List[ReplyBundleCommand[F]] = List(
    TriggerListCommand.triggerListReplyBundleCommand[F](triggerListUri),
    TriggerSearchCommand.triggerSearchReplyBundleCommand[F](
      botName = botName,
      ignoreMessagePrefix = M0sconiBot.ignoreMessagePrefix,
      mdr = messageRepliesData[F]
    ),
    StatisticsCommands.topTwentyReplyBundleCommand[F](
      botPrefix = botPrefix,
      dbMedia = dbLayer.dbMedia
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
      ignoreMessagePrefix = M0sconiBot.ignoreMessagePrefix,
      commandDescriptionsIta = List(
        TriggerListCommand.triggerListCommandDescriptionIta,
        TriggerSearchCommand.triggerSearchCommandDescriptionIta,
        StatisticsCommands.topTwentyTriggersCommandDescriptionIta,
        TimeoutCommand.timeoutCommandDescriptionIta,
        RandomDataCommand.randomDataCommandIta
      ),
      commandDescriptionsEng = List(
        TriggerListCommand.triggerListCommandDescriptionEng,
        TriggerSearchCommand.triggerSearchCommandDescriptionEng,
        StatisticsCommands.topTwentyTriggersCommandDescriptionEng,
        TimeoutCommand.timeoutCommandDescriptionEng,
        RandomDataCommand.randomDataCommandEng
      ),
    )
  )

  def buildPollingBot[F[_]: Parallel: Async: Network, A](
      action: M0sconiBotPolling[F] => F[A]
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
      new M0sconiBotPolling[F](
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
  )(using log: LogWriter[F]): Resource[F, M0sconiBotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botName = botName,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new M0sconiBotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        resAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(using Async[F], botSetup.api, log)
    }
}
