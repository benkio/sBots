package com.benkio.m0sconibot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure._
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringTimeout
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.InstructionsCommand
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.StatisticsCommands
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
import org.http4s.ember.client._
import org.http4s.implicits._
import telegramium.bots.high._
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
      mf"mos_ChiudiQuellaPortaPerFavore.mp3",
      mf"mos_AspettaUnSecondoDioCane.mp3",
      mf"mos_DaiVaLaRipartiamoSubito.mp3",
      mf"mos_VaffanuloDai.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(quella|la) porta".r.tr(8)
    )(
      mf"mos_AvantiNdrioConQuellaPortaLi.mp3",
      mf"mos_ChiEQuelMonaSbatteLaPortaChiudeUrlando.mp3",
      mf"mos_ChiudiQuellaPortaDai.mp3",
      mf"mos_ChiudiQuellaPortaPerFavore.mp3",
      mf"mos_SerraLaPortaDioCane.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bmona\\b".r.tr(4)
    )(
      mf"mos_AndateInMona.mp3",
      mf"mos_ChiEQuelMonaSbatteLaPortaChiudeUrlando.mp3",
      mf"mos_EntraDentroMona.mp3",
      mf"mos_NoVaInMonaNonTornoIndrio.mp3",
      mf"mos_VaInMonaCortesiaSimpatiaPorcoDioVaInCasinoVaffanculo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"dio cane"
    )(
      mf"mos_AndateInMona.mp3",
      mf"mos_AntonioFossoCarteColla.mp3",
      mf"mos_AspettaUnSecondoDioCane.mp3",
      mf"mos_AvantiNdrioConQuellaPortaLi.mp3",
      mf"mos_BasketDioCaneDipenDipanDaCapo.mp3",
      mf"mos_DioCane.mp3",
      mf"mos_DioCane2.mp3",
      mf"mos_DioCaneLaMadonna.mp3",
      mf"mos_DioCaneMaNonEPossibile.mp3",
      mf"mos_DioCaneX3MadonnaPuttana.mp3",
      mf"mos_DioSchifosoCan.mp3",
      mf"mos_InPrimoPianoIlCalcioDioSchifosoCan.mp3",
      mf"mos_Innervosire.mp3",
      mf"mos_MagistratoCuratoreFallimentareExDioPorcoDioCane.mp3",
      mf"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3",
      mf"mos_SerraLaPortaDioCane.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"madonna"
    )(
      mf"mos_DioCaneLaMadonna.mp3",
      mf"mos_DioCaneX3MadonnaPuttana.mp3",
      mf"mos_DioPorcaMadonnaDeDio.mp3",
      mf"mos_MaEPossibilePortannaLaMadonna.mp3",
      mf"mos_MadonnaDeDio.mp3",
      mf"mos_MadonnaPuttana.mp3",
      mf"mos_MadonnaPuttina.mp3",
      mf"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3",
      mf"mos_TennisVaffanculoDioPorcoMadonnaPuttana.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"dio porco"
    )(
      mf"mos_BasketSeiVantaggiDioPorco.mp3",
      mf"mos_BresciaFoggia.mp3",
      mf"mos_CittaDioPorco.mp3",
      mf"mos_CoppaDelleCoppeSampdoriaDioPorco.mp3",
      mf"mos_DioPorcaMadonnaDeDio.mp3",
      mf"mos_DioPorco.mp3",
      mf"mos_DioPorco2.mp3",
      mf"mos_DioPorco3.mp3",
      mf"mos_DioPorcoCheNotiziaDioCaneBoia.mp3",
      mf"mos_MagistratoCuratoreFallimentareExDioPorcoDioCane.mp3",
      mf"mos_SeVeniteAvantiAncoraViDoUnPunio.mp3",
      mf"mos_TennisVaffanculoDioPorcoMadonnaPuttana.mp3",
      mf"mos_VaffanculoDioPorco.mp3",
      mf"mos_VaffanuloDai.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"dio boia"
    )(
      mf"mos_DioPorcoCheNotiziaDioCaneBoia.mp3",
      mf"mos_NoVaInMonaNonTornoIndrio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"de dio"
    )(
      mf"mos_BresciaFoggia.mp3",
      mf"mos_DioPorcaMadonnaDeDio.mp3",
      mf"mos_MadonnaDeDio.mp3",
      mf"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3",
      mf"mos_PilotaRomanoAndreaDeCesariDioCanariaDeDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "a[h]? n[o]?n lo so".r.tr(11)
    )(
      mf"mos_AhNonLoSo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non posso (mica )?improvvisar(e|me)".r.tr(21),
    )(
      mf"mos_AndateInMona.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"andiamo dai",
      stt"lo fa apposta"
    )(
      mf"mos_AndiamoDai.mp3",
      mf"mos_MarioFerrettoPorcoDioRumoreLiFaAposta.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"colla",
      stt"le carte"
    )(
      mf"mos_DioBonDiUnDioLeSeaTagaENonLeSeStaga.mp3",
      mf"mos_AntonioFossoCarteColla.mp3",
      mf"mos_GuardaCheRobaLaCollaAttaccataAlleCarte.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"dio bono"
    )(
      mf"mos_DioBonDiUnDioLeSeaTagaENonLeSeStaga.mp3",
      mf"mos_DioBono.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"antonio fosso",
      stt"neo brigatista",
    )(
      mf"mos_AntonioFossoCarteColla.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ostia",
    )(
      mf"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3",
      mf"mos_AntonioFossoCarteColla.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"aspetta un secondo",
    )(
      mf"mos_AspettaUnSecondoDioCane.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "avanti (indrio|indietro)".r.tr(13),
    )(
      mf"mos_AvantiNdrioConQuellaPortaLi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"marco bisagno",
      stt"la minoranza",
      stt"assemblea",
      stt"irregolare",
    )(
      mf"mos_AvvocatoMarcoBisagnoAssembleaIrregolareMinoranze.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"basket",
      stt"pallacanestro",
    )(
      mf"mos_BasketSeiVantaggiDioPorco.mp3",
      mf"mos_BasketDioCaneDipenDipanDaCapo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"palasport",
      stt"piazzale olimpia",
      stt"udine",
      "di[tp]an".r.tr(5),
      stt"diche",
      "\\bdipen\\b".r.tr(5),
    )(
      mf"mos_BasketDioCaneDipenDipanDaCapo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"manovre offersive",
      stt"imprecisi al tiro",
      stt"sei vantaggi"
    )(
      mf"mos_BasketSeiVantaggiDioPorco.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"quadro completo",
      stt"tanto tempo anche",
      "brescia(-| )foggia".r.tr(14),
      "cagliari(-| )roma".r.tr(13),
      "fiorentina(-| )milan".r.tr(16),
      "genova(-| )atalanta".r.tr(15),
      "\\binter\\b".r.tr(5)
    )(
      mf"mos_BresciaFoggia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"buongiorno"
    )(
      mf"mos_Buongiorno.mp3",
      mf"mos_Buongiorno2.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "but(tato|ta) per aria tutto".r.tr(22)
    )(
      mf"mos_ButtaPellAria.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"cos'è che è caduto"
    )(
      mf"mos_Caduto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non si riesce a capire un cazzo"
    )(
      mf"mos_CapireUnCazzo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"chiude urlando"
    )(
      mf"mos_ChiEQuelMonaSbatteLaPortaChiudeUrlando.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "chissà che non m(i |')incazzo".r.tr(26)
    )(
      mf"mos_ChissaCheNonMIncazzaEh.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mancalacqua",
      stt"lugagnano",
      stt"vigasio",
      stt"poveliano",
      stt"tosse",
    )(
      mf"mos_CiclismoAllieviDio.mp3",
      mf"mos_TosseDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ciclismo"
    )(
      mf"mos_CiclismoAllieviDio.mp3",
      mf"mos_CiclismoGianniBugnoRitardo.mp3",
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"nostra città"
    )(
      mf"mos_CittaDioPorco.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"coppa delle coppe",
      stt"sampdoria"
    )(
      mf"mos_CoppaDelleCoppeSampdoriaDioPorco.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"orco dio"
    )(
      mf"mos_CortiOrcoDio.mp3",
      mf"mos_Formula1DonningtonPrimaSessioneVaffanculo.mp3",
      mf"mos_OrcoDio.mp3",
      mf"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "che cazzo ([cg]he )è qua sotto".r.tr(25)
    )(
      mf"mos_CosaCheCazzoGheE.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ripartiamo subito"
    )(
      mf"mos_DaiVaLaRipartiamoSubito.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "\\bsigla\\b".r.tr(5)
    )(
      mf"mos_DallaSiglaDai.mp3",
      mf"mos_Sigla.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "[dfdba]{5,}".r.tr(5),
      "farfugl(i|a|iare|iamento)".r.tr(7)
    )(
      mf"mos_Difabbddffbbaa.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non è possibile"
    )(
      mf"mos_DioCaneMaNonEPossibile.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dio (pa){3,}".r.tr(6)
    )(
      mf"mos_DioPaPaPaPaPaPa.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"che notizia"
    )(
      mf"mos_DioPorcoCheNotiziaDioCaneBoia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"dio schifoso"
    )(
      mf"mos_DioSchifosoCan.mp3",
      mf"mos_InPrimoPianoIlCalcioDioSchifosoCan.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"dios"
    )(
      mf"mos_Dios.mp3",
      mf"mos_RiprendoDallaTabella.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"entra dentro"
    )(
      mf"mos_EntraDentroMona.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "formula (uno|1)".r.tr(9),
      stt"donnington",
      stt"prima sessione"
    )(
      mf"mos_Formula1DonningtonPrimaSessioneVaffanculo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"vaffanculo"
    )(
      mf"mos_Formula1DonningtonPrimaSessioneVaffanculo.mp3",
      mf"mos_TennisVaffanculoDioPorcoMadonnaPuttana.mp3",
      mf"mos_VaInMonaCortesiaSimpatiaPorcoDioVaInCasinoVaffanculo.mp3",
      mf"mos_Vaffanculo.mp3",
      mf"mos_VaffanculoDioPorco.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "ges[uù] cristo".r.tr(11),
      stt"no nessuno",
    )(
      mf"mos_GesuCristoNoNessuno.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"giochi olimpici",
      stt"giorgio mazzetta",
      stt"giorgio mazzetta",
      "m[ie] fermo un (secondo|attimo)".r.tr(18),
    )(
      mf"mos_GiochiOlimpiciAspettaCheMeFermoUnAttimo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"girato le foto"
    )(
      mf"mos_GiratoLeFoto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"imbecilli"
    )(
      mf"mos_Imbecilli.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"in primo piano"
    )(
      mf"mos_InPrimoPianoIlCalcioDioSchifosoCan.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mi ha fatto innervosire",
      stt"gli spacco la testa",
    )(
      mf"mos_Innervosire.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"juventus",
      stt"austria",
      stt"vienna",
    )(
      mf"mos_JuventusAustriaViennaPorcoDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"porco dio"
    )(
      mf"mos_MaPorcoDio.mp3",
      mf"mos_JuventusAustriaViennaPorcoDio.mp3",
      mf"mos_MarioFerrettoPorcoDioRumoreLiFaAposta.mp3",
      mf"mos_PiovanelliPorcoDio.mp3",
      mf"mos_PorcoDio4.mp3",
      mf"mos_QuotazioniPorcoDio.mp3",
      mf"mos_TelefonoPorcoDio.mp3",
      mf"mos_VaInMonaCortesiaSimpatiaPorcoDioVaInCasinoVaffanculo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"lo troverò",
      stt"prima o dopo",
    )(
      mf"mos_LoTroveroDeficiente.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ma è possibile"
    )(
      mf"mos_MaEPossibilePortannaLaMadonna.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"puttina"
    )(
      mf"mos_MadonnaPuttina.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"il commercialista",
      stt"il magistrato",
      stt"gianfranco bertani",
    )(
      mf"mos_MagistratoCuratoreFallimentareExDioPorcoDioCane.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mario ferretto",
      stt"accolto con sorpresa",
      stt"rumore li",
    )(
      mf"mos_MarioFerrettoPorcoDioRumoreLiFaAposta.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mi vedo già"
    )(
      mf"mos_MeVedoGia.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"mi sono distratto",
      stt"mi hai distratto"
    )(
      mf"mos_MiSonoDistratto.mp3",
      mf"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "non torno (indietro|indrio)".r.tr(16)
    )(
      mf"mos_NoVaInMonaNonTornoIndrio.mp3",
      mf"mos_NonTornoIndietroDonadoni.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"monitor"
    )(
      mf"mos_NonCapiscoPiuNienteMonitor.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"non capisco più niente",
      stt"non riesco a capire più niente"
    )(
      mf"mos_NonRiescoACapireNiente.mp3",
      mf"mos_NonCapiscoPiuNienteMonitor.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"donadoni"
    )(
      mf"mos_NonTornoIndietroDonadoni.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"in maiuscolo",
    )(
      mf"mos_NotizieInMaiuscolo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"notizie"
    )(
      mf"mos_NotizieScritteDaCul.mp3",
      mf"mos_NotizieInMaiuscolo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"da capo",
      stt"venite dentro",
    )(
      mf"mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"orientarmi"
    )(
      mf"mos_Orientarmi.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"andrea de cesari",
      stt"romano",
      stt"dio canaia",
      stt"il pilota",
    )(
      mf"mos_PilotaRomanoAndreaDeCesariDioCanariaDeDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"piovanelli",
      stt"atalanta"
    )(
      mf"mos_PiovanelliPorcoDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "dio bub[uù]".r.tr(8)
    )(
      mf"mos_PiproviamoDioBubu.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"quotazioni"
    )(
      mf"mos_QuotazioniPorcoDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"ricominciamo da capo"
    )(
      mf"mos_RicominciamoDaCapo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"dalla tabella"
    )(
      mf"mos_RiprendoDallaTabella.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"sbattere le porte"
    )(
      mf"mos_SbattereLePorte.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"se non bestemmio"
    )(
      mf"mos_SeNonBestemmioGuarda.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "vi do un pu[g]?nio".r.tr(14),
      stt"se venite avanti"
    )(
      mf"mos_SeVeniteAvantiAncoraViDoUnPunio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      "(sei|6) vantaggi".r.tr(10)
    )(
      mf"mos_SeiVantaggiDiLunghezza.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"tecnocop",
      stt"verona",
    )(
      mf"mos_TecnocopVerona.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"telefono"
    )(
      mf"mos_TelefonoPorcoDio.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"tennis"
    )(
      mf"mos_TennisVaffanculoDioPorcoMadonnaPuttana.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"tocca ferro"
    )(
      mf"mos_ToccaFerro.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"tutte le volte che"
    )(
      mf"mos_TutteLeVolteCheParto.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"cortesia",
      stt"simpatia",
      stt"va in casino",
    )(
      mf"mos_VaInMonaCortesiaSimpatiaPorcoDioVaInCasinoVaffanculo.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"vice sindaco"
    )(
      mf"mos_ViceSindaco.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"vedo tutto tranne",
      stt"quello che dovrei vedere"
    )(
      mf"mos_VedoTuttoMenoQuelloCheDovreiVedere.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"cesena",
      "\\btorino\\b".r.tr(6),
      stt"avellino",
      stt"walter shakner",
      stt"austriaco"
    )(
      mf"mos_WalterShaknerAustriacoCesenaTorinoAvellino.mp3"
    ),
    ReplyBundleMessage.textToMedia[F](
      stt"gaetano"
    )(
      mf"mos_Gaetano.mp3"
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
      ignoreMessagePrefix = ignoreMessagePrefix,
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
    InstructionsCommand.instructionsReplyBundleCommand[F](
      botName = botName,
      ignoreMessagePrefix = ignoreMessagePrefix,
      commandDescriptionsIta = List(
        TriggerListCommand.triggerListCommandDescriptionIta,
        TriggerSearchCommand.triggerSearchCommandDescriptionIta,
        StatisticsCommands.topTwentyTriggersCommandDescriptionIta,
        TimeoutCommand.timeoutCommandDescriptionIta
      ),
      commandDescriptionsEng = List(
        TriggerListCommand.triggerListCommandDescriptionEng,
        TriggerSearchCommand.triggerSearchCommandDescriptionEng,
        StatisticsCommands.topTwentyTriggersCommandDescriptionEng,
        TimeoutCommand.timeoutCommandDescriptionEng
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
      )(Parallel[F], Async[F], botSetup.api, log)
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
      )(Async[F], botSetup.api, log)
    }
}
