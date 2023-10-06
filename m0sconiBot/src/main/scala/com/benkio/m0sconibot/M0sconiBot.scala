package com.benkio.m0sconibot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
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

class M0sconiBotPolling[F[_]: Parallel: Async: Api: Action: LogWriter](
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F]
    with M0sconiBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = resAccess
  override def postComputation(implicit appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(implicit
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

class M0sconiBotWebhook[F[_]: Async: Api: Action: LogWriter](
    uri: Uri,
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends BotSkeletonWebhook[F](uri, path, webhookCertificate)
    with M0sconiBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = resAccess
  override def postComputation(implicit appF: Applicative[F]): Message => F[Unit] =
    PostComputationPatterns.timeoutPostComputation(dbTimeout = dbLayer.dbTimeout, botName = botName)
  override def filteringMatchesMessages(implicit
      applicativeF: Applicative[F]
  ): (ReplyBundleMessage[F], Message) => F[Boolean] =
    FilteringTimeout.filter(dbLayer, botName)
}

trait M0sconiBot[F[_]] extends BotSkeleton[F] {

  override val botName: String                     = M0sconiBot.botName
  override val botPrefix: String                   = M0sconiBot.botPrefix
  override val ignoreMessagePrefix: Option[String] = M0sconiBot.ignoreMessagePrefix
  val backgroundJobManager: BackgroundJobManager[F]

  override def messageRepliesDataF(implicit
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    M0sconiBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(implicit asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
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
      F[_]
  ]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("va l[aà]".r, 5),
      ),
      mediafiles = List(
        MediaFile("mos_ChiudiQuellaPortaPerFavore.mp3"),
        MediaFile("mos_AspettaUnSecondoDioCane.mp3"),
        MediaFile("mos_DaiVaLaRipartiamoSubito.mp3"),
        MediaFile("mos_VaffanuloDai.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("(quella|la) porta".r, 8)
      ),
      mediafiles = List(
        MediaFile("mos_AvantiNdrioConQuellaPortaLi.mp3"),
        MediaFile("mos_ChiEQuelMonaSbatteLaPortaChiudeUrlando.mp3"),
        MediaFile("mos_ChiudiQuellaPortaDai.mp3"),
        MediaFile("mos_ChiudiQuellaPortaPerFavore.mp3"),
        MediaFile("mos_SerraLaPortaDioCane.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("\\bmona\\b".r, 4)
      ),
      mediafiles = List(
        MediaFile("mos_AndateInMona.mp3"),
        MediaFile("mos_ChiEQuelMonaSbatteLaPortaChiudeUrlando.mp3"),
        MediaFile("mos_EntraDentroMona.mp3"),
        MediaFile("mos_NoVaInMonaNonTornoIndrio.mp3"),
        MediaFile("mos_VaInMonaCortesiaSimpatiaPorcoDioVaInCasinoVaffanculo.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("dio cane")
      ),
      mediafiles = List(
        MediaFile("mos_AndateInMona.mp3"),
        MediaFile("mos_AntonioFossoCarteColla.mp3"),
        MediaFile("mos_AspettaUnSecondoDioCane.mp3"),
        MediaFile("mos_AvantiNdrioConQuellaPortaLi.mp3"),
        MediaFile("mos_BasketDioCaneDipenDipanDaCapo.mp3"),
        MediaFile("mos_DioCane.mp3"),
        MediaFile("mos_DioCane2.mp3"),
        MediaFile("mos_DioCaneLaMadonna.mp3"),
        MediaFile("mos_DioCaneMaNonEPossibile.mp3"),
        MediaFile("mos_DioCaneX3MadonnaPuttana.mp3"),
        MediaFile("mos_DioSchifosoCan.mp3"),
        MediaFile("mos_InPrimoPianoIlCalcioDioSchifosoCan.mp3"),
        MediaFile("mos_Innervosire.mp3"),
        MediaFile("mos_MagistratoCuratoreFallimentareExDioPorcoDioCane.mp3"),
        MediaFile("mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3"),
        MediaFile("mos_SerraLaPortaDioCane.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("madonna")
      ),
      mediafiles = List(
        MediaFile("mos_DioCaneLaMadonna.mp3"),
        MediaFile("mos_DioCaneX3MadonnaPuttana.mp3"),
        MediaFile("mos_DioPorcaMadonnaDeDio.mp3"),
        MediaFile("mos_MaEPossibilePortannaLaMadonna.mp3"),
        MediaFile("mos_MadonnaDeDio.mp3"),
        MediaFile("mos_MadonnaPuttana.mp3"),
        MediaFile("mos_MadonnaPuttina.mp3"),
        MediaFile("mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3"),
        MediaFile("mos_TennisVaffanculoDioPorcoMadonnaPuttana.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("dio porco")
      ),
      mediafiles = List(
        MediaFile("mos_BasketSeiVantaggiDioPorco.mp3"),
        MediaFile("mos_BresciaFoggia.mp3"),
        MediaFile("mos_CittaDioPorco.mp3"),
        MediaFile("mos_CoppaDelleCoppeSampdoriaDioPorco.mp3"),
        MediaFile("mos_DioPorcaMadonnaDeDio.mp3"),
        MediaFile("mos_DioPorco.mp3"),
        MediaFile("mos_DioPorco2.mp3"),
        MediaFile("mos_DioPorco3.mp3"),
        MediaFile("mos_DioPorcoCheNotiziaDioCaneBoia.mp3"),
        MediaFile("mos_MagistratoCuratoreFallimentareExDioPorcoDioCane.mp3"),
        MediaFile("mos_SeVeniteAvantiAncoraViDoUnPunio.mp3"),
        MediaFile("mos_TennisVaffanculoDioPorcoMadonnaPuttana.mp3"),
        MediaFile("mos_VaffanculoDioPorco.mp3"),
        MediaFile("mos_VaffanuloDai.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("dio boia")
      ),
      mediafiles = List(
        MediaFile("mos_DioPorcoCheNotiziaDioCaneBoia.mp3"),
        MediaFile("mos_NoVaInMonaNonTornoIndrio.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("de dio")
      ),
      mediafiles = List(
        MediaFile("mos_BresciaFoggia.mp3"),
        MediaFile("mos_DioPorcaMadonnaDeDio.mp3"),
        MediaFile("mos_MadonnaDeDio.mp3"),
        MediaFile("mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3"),
        MediaFile("mos_PilotaRomanoAndreaDeCesariDioCanariaDeDio.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("a[h]? n[o]?n lo so".r, 11)
      ),
      mediafiles = List(
        MediaFile("mos_AhNonLoSo.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("non posso (mica )?improvvisar(e|me)".r, 21),
      ),
      mediafiles = List(
        MediaFile("mos_AndateInMona.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("andiamo dai"),
        StringTextTriggerValue("lo fa apposta")
      ),
      mediafiles = List(
        MediaFile("mos_AndiamoDai.mp3"),
        MediaFile("mos_MarioFerrettoPorcoDioRumoreLiFaAposta.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("colla"),
        StringTextTriggerValue("le carte")
      ),
      mediafiles = List(
        MediaFile("mos_DioBonDiUnDioLeSeaTagaENonLeSeStaga.mp3"),
        MediaFile("mos_AntonioFossoCarteColla.mp3"),
        MediaFile("mos_GuardaCheRobaLaCollaAttaccataAlleCarte.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("dio bono")
      ),
      mediafiles = List(
        MediaFile("mos_DioBonDiUnDioLeSeaTagaENonLeSeStaga.mp3"),
        MediaFile("mos_DioBono.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("antonio fosso"),
        StringTextTriggerValue("neo brigatista"),
      ),
      mediafiles = List(
        MediaFile("mos_AntonioFossoCarteColla.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ostia"),
      ),
      mediafiles = List(
        MediaFile("mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3"),
        MediaFile("mos_AntonioFossoCarteColla.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("aspetta un secondo"),
      ),
      mediafiles = List(
        MediaFile("mos_AspettaUnSecondoDioCane.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("avanti (indrio|indietro)".r, 13),
      ),
      mediafiles = List(
        MediaFile("mos_AvantiNdrioConQuellaPortaLi.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("marco bisagno"),
        StringTextTriggerValue("la minoranza"),
        StringTextTriggerValue("assemblea"),
        StringTextTriggerValue("irregolare"),
      ),
      mediafiles = List(
        MediaFile("mos_AvvocatoMarcoBisagnoAssembleaIrregolareMinoranze.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("basket"),
        StringTextTriggerValue("pallacanestro"),
      ),
      mediafiles = List(
        MediaFile("mos_BasketSeiVantaggiDioPorco.mp3"),
        MediaFile("mos_BasketDioCaneDipenDipanDaCapo.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("palasport"),
        StringTextTriggerValue("piazzale olimpia"),
        StringTextTriggerValue("udine"),
        RegexTextTriggerValue("di[tp]an".r, 5),
        StringTextTriggerValue("diche"),
        RegexTextTriggerValue("\\bdipen\\b".r, 5),
      ),
      mediafiles = List(
        MediaFile("mos_BasketDioCaneDipenDipanDaCapo.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("manovre offersive"),
        StringTextTriggerValue("imprecisi al tiro"),
        StringTextTriggerValue("sei vantaggi")
      ),
      mediafiles = List(
        MediaFile("mos_BasketSeiVantaggiDioPorco.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("quadro completo"),
        StringTextTriggerValue("tanto tempo anche"),
        RegexTextTriggerValue("brescia(-| )foggia".r, 14),
        RegexTextTriggerValue("cagliari(-| )roma".r, 13),
        RegexTextTriggerValue("fiorentina(-| )milan".r, 16),
        RegexTextTriggerValue("genova(-| )atalanta".r, 15),
        RegexTextTriggerValue("\\binter\\b".r, 5)
      ),
      mediafiles = List(
        MediaFile("mos_BresciaFoggia.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("buongiorno")
      ),
      mediafiles = List(
        MediaFile("mos_Buongiorno.mp3"),
        MediaFile("mos_Buongiorno2.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("but(tato|ta) per aria tutto".r, 22)
      ),
      mediafiles = List(
        MediaFile("mos_ButtaPellAria.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("cos'è che è caduto")
      ),
      mediafiles = List(
        MediaFile("mos_Caduto.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non si riesce a capire un cazzo")
      ),
      mediafiles = List(
        MediaFile("mos_CapireUnCazzo.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("chiude urlando")
      ),
      mediafiles = List(
        MediaFile("mos_ChiEQuelMonaSbatteLaPortaChiudeUrlando.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("chissà che non m(i |')incazzo".r, 26)
      ),
      mediafiles = List(
        MediaFile("mos_ChissaCheNonMIncazzaEh.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("mancalacqua"),
        StringTextTriggerValue("lugagnano"),
        StringTextTriggerValue("vigasio"),
        StringTextTriggerValue("poveliano"),
        StringTextTriggerValue("tosse"),
      ),
      mediafiles = List(
        MediaFile("mos_CiclismoAllieviDio.mp3"),
        MediaFile("mos_TosseDio.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ciclismo")
      ),
      mediafiles = List(
        MediaFile("mos_CiclismoAllieviDio.mp3"),
        MediaFile("mos_CiclismoGianniBugnoRitardo.mp3"),
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("nostra città")
      ),
      mediafiles = List(
        MediaFile("mos_CittaDioPorco.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("coppa delle coppe"),
        StringTextTriggerValue("sampdoria")
      ),
      mediafiles = List(
        MediaFile("mos_CoppaDelleCoppeSampdoriaDioPorco.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("orco dio")
      ),
      mediafiles = List(
        MediaFile("mos_CortiOrcoDio.mp3"),
        MediaFile("mos_Formula1DonningtonPrimaSessioneVaffanculo.mp3"),
        MediaFile("mos_OrcoDio.mp3"),
        MediaFile("mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("che cazzo ([cg]he )è qua sotto".r, 25)
      ),
      mediafiles = List(
        MediaFile("mos_CosaCheCazzoGheE.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ripartiamo subito")
      ),
      mediafiles = List(
        MediaFile("mos_DaiVaLaRipartiamoSubito.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("\\bsigla\\b".r, 5)
      ),
      mediafiles = List(
        MediaFile("mos_DallaSiglaDai.mp3"),
        MediaFile("mos_Sigla.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("[dfdba]{5,}".r, 5),
        RegexTextTriggerValue("farfugl(i|a|iare|iamento)".r, 7)
      ),
      mediafiles = List(
        MediaFile("mos_Difabbddffbbaa.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non è possibile")
      ),
      mediafiles = List(
        MediaFile("mos_DioCaneMaNonEPossibile.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("dio (pa){3,}".r, 6)
      ),
      mediafiles = List(
        MediaFile("mos_DioPaPaPaPaPaPa.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("che notizia")
      ),
      mediafiles = List(
        MediaFile("mos_DioPorcoCheNotiziaDioCaneBoia.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("dio schifoso")
      ),
      mediafiles = List(
        MediaFile("mos_DioSchifosoCan.mp3"),
        MediaFile("mos_InPrimoPianoIlCalcioDioSchifosoCan.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("dios")
      ),
      mediafiles = List(
        MediaFile("mos_Dios.mp3"),
        MediaFile("mos_RiprendoDallaTabella.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("entra dentro")
      ),
      mediafiles = List(
        MediaFile("mos_EntraDentroMona.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("formula (uno|1)".r, 9),
        StringTextTriggerValue("donnington"),
        StringTextTriggerValue("prima sessione")
      ),
      mediafiles = List(
        MediaFile("mos_Formula1DonningtonPrimaSessioneVaffanculo.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("vaffanculo")
      ),
      mediafiles = List(
        MediaFile("mos_Formula1DonningtonPrimaSessioneVaffanculo.mp3"),
        MediaFile("mos_TennisVaffanculoDioPorcoMadonnaPuttana.mp3"),
        MediaFile("mos_VaInMonaCortesiaSimpatiaPorcoDioVaInCasinoVaffanculo.mp3"),
        MediaFile("mos_Vaffanculo.mp3"),
        MediaFile("mos_VaffanculoDioPorco.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("ges[uù] cristo".r, 11),
        StringTextTriggerValue("no nessuno"),
      ),
      mediafiles = List(
        MediaFile("mos_GesuCristoNoNessuno.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("giochi olimpici"),
        StringTextTriggerValue("giorgio mazzetta"),
        StringTextTriggerValue("giorgio mazzetta"),
        RegexTextTriggerValue("m[ie] fermo un (secondo|attimo)".r, 18),
      ),
      mediafiles = List(
        MediaFile("mos_GiochiOlimpiciAspettaCheMeFermoUnAttimo.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("girato le foto")
      ),
      mediafiles = List(
        MediaFile("mos_GiratoLeFoto.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("imbecilli")
      ),
      mediafiles = List(
        MediaFile("mos_Imbecilli.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("in primo piano")
      ),
      mediafiles = List(
        MediaFile("mos_InPrimoPianoIlCalcioDioSchifosoCan.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("mi ha fatto innervosire"),
        StringTextTriggerValue("gli spacco la testa"),
      ),
      mediafiles = List(
        MediaFile("mos_Innervosire.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("juventus"),
        StringTextTriggerValue("austria"),
        StringTextTriggerValue("vienna"),
      ),
      mediafiles = List(
        MediaFile("mos_JuventusAustriaViennaPorcoDio.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("porco dio")
      ),
      mediafiles = List(
        MediaFile("mos_MaPorcoDio.mp3"),
        MediaFile("mos_JuventusAustriaViennaPorcoDio.mp3"),
        MediaFile("mos_MarioFerrettoPorcoDioRumoreLiFaAposta.mp3"),
        MediaFile("mos_PiovanelliPorcoDio.mp3"),
        MediaFile("mos_PorcoDio4.mp3"),
        MediaFile("mos_QuotazioniPorcoDio.mp3"),
        MediaFile("mos_TelefonoPorcoDio.mp3"),
        MediaFile("mos_VaInMonaCortesiaSimpatiaPorcoDioVaInCasinoVaffanculo.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("lo troverò"),
        StringTextTriggerValue("prima o dopo"),
      ),
      mediafiles = List(
        MediaFile("mos_LoTroveroDeficiente.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ma è possibile")
      ),
      mediafiles = List(
        MediaFile("mos_MaEPossibilePortannaLaMadonna.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("puttina")
      ),
      mediafiles = List(
        MediaFile("mos_MadonnaPuttina.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("il commercialista"),
        StringTextTriggerValue("il magistrato"),
        StringTextTriggerValue("gianfranco bertani"),
      ),
      mediafiles = List(
        MediaFile("mos_MagistratoCuratoreFallimentareExDioPorcoDioCane.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("mario ferretto"),
        StringTextTriggerValue("accolto con sorpresa"),
        StringTextTriggerValue("rumore li"),
      ),
      mediafiles = List(
        MediaFile("mos_MarioFerrettoPorcoDioRumoreLiFaAposta.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("mi vedo già")
      ),
      mediafiles = List(
        MediaFile("mos_MeVedoGia.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("mi sono distratto"),
        StringTextTriggerValue("mi hai distratto")
      ),
      mediafiles = List(
        MediaFile("mos_MiSonoDistratto.mp3"),
        MediaFile("mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("non torno (indietro|indrio)".r, 16)
      ),
      mediafiles = List(
        MediaFile("mos_NoVaInMonaNonTornoIndrio.mp3"),
        MediaFile("mos_NonTornoIndietroDonadoni.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("monitor")
      ),
      mediafiles = List(
        MediaFile("mos_NonCapiscoPiuNienteMonitor.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("non capisco più niente"),
        StringTextTriggerValue("non riesco a capire più niente")
      ),
      mediafiles = List(
        MediaFile("mos_NonRiescoACapireNiente.mp3"),
        MediaFile("mos_NonCapiscoPiuNienteMonitor.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("donadoni")
      ),
      mediafiles = List(
        MediaFile("mos_NonTornoIndietroDonadoni.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("in maiuscolo"),
      ),
      mediafiles = List(
        MediaFile("mos_NotizieInMaiuscolo.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("notizie")
      ),
      mediafiles = List(
        MediaFile("mos_NotizieScritteDaCul.mp3"),
        MediaFile("mos_NotizieInMaiuscolo.mp3")
      ),
      replySelection = RandomSelection
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("da capo"),
        StringTextTriggerValue("venite dentro"),
      ),
      mediafiles = List(
        MediaFile("mos_OrcoDioMadonnaDeDioCaneTuttoDaCapoNonVeniteDentroDistrattoDioBonoDeDio.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("orientarmi")
      ),
      mediafiles = List(
        MediaFile("mos_Orientarmi.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("andrea de cesari"),
        StringTextTriggerValue("romano"),
        StringTextTriggerValue("dio canaia"),
        StringTextTriggerValue("il pilota"),
      ),
      mediafiles = List(
        MediaFile("mos_PilotaRomanoAndreaDeCesariDioCanariaDeDio.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("piovanelli"),
        StringTextTriggerValue("atalanta")
      ),
      mediafiles = List(
        MediaFile("mos_PiovanelliPorcoDio.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("dio bub[uù]".r, 8)
      ),
      mediafiles = List(
        MediaFile("mos_PiproviamoDioBubu.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("quotazioni")
      ),
      mediafiles = List(
        MediaFile("mos_QuotazioniPorcoDio.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("ricominciamo da capo")
      ),
      mediafiles = List(
        MediaFile("mos_RicominciamoDaCapo.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("dalla tabella")
      ),
      mediafiles = List(
        MediaFile("mos_RiprendoDallaTabella.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("sbattere le porte")
      ),
      mediafiles = List(
        MediaFile("mos_SbattereLePorte.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("se non bestemmio")
      ),
      mediafiles = List(
        MediaFile("mos_SeNonBestemmioGuarda.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("vi do un pu[g]?nio".r, 14),
        StringTextTriggerValue("se venite avanti")
      ),
      mediafiles = List(
        MediaFile("mos_SeVeniteAvantiAncoraViDoUnPunio.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        RegexTextTriggerValue("(sei|6) vantaggi".r, 10)
      ),
      mediafiles = List(
        MediaFile("mos_SeiVantaggiDiLunghezza.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("tecnocop"),
        StringTextTriggerValue("verona"),
      ),
      mediafiles = List(
        MediaFile("mos_TecnocopVerona.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("telefono")
      ),
      mediafiles = List(
        MediaFile("mos_TelefonoPorcoDio.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("tennis")
      ),
      mediafiles = List(
        MediaFile("mos_TennisVaffanculoDioPorcoMadonnaPuttana.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("tocca ferro")
      ),
      mediafiles = List(
        MediaFile("mos_ToccaFerro.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("tutte le volte che")
      ),
      mediafiles = List(
        MediaFile("mos_TutteLeVolteCheParto.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("cortesia"),
        StringTextTriggerValue("simpatia"),
        StringTextTriggerValue("va in casino"),
      ),
      mediafiles = List(
        MediaFile("mos_VaInMonaCortesiaSimpatiaPorcoDioVaInCasinoVaffanculo.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("vice sindaco")
      ),
      mediafiles = List(
        MediaFile("mos_ViceSindaco.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("vedo tutto tranne"),
        StringTextTriggerValue("quello che dovrei vedere")
      ),
      mediafiles = List(
        MediaFile("mos_VedoTuttoMenoQuelloCheDovreiVedere.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("cesena"),
        RegexTextTriggerValue("\\btorino\\b".r, 6),
        StringTextTriggerValue("avellino"),
        StringTextTriggerValue("walter shakner"),
        StringTextTriggerValue("austriaco")
      ),
      mediafiles = List(
        MediaFile("mos_WalterShaknerAustriacoCesenaTorinoAvellino.mp3")
      )
    ),
    ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("gaetano")
      ),
      mediafiles = List(
        MediaFile("mos_Gaetano.mp3")
      )
    )
  )

  def messageRepliesGifData[
      F[_]
  ]: List[ReplyBundleMessage[F]] = List()

  def messageRepliesSpecialData[
      F[_]
  ]: List[ReplyBundleMessage[F]] = List()

  def messageRepliesData[
      F[_]
  ]: List[ReplyBundleMessage[F]] =
    (messageRepliesAudioData[F] ++ messageRepliesGifData[F] ++ messageRepliesSpecialData[F])
      .sorted(ReplyBundle.orderingInstance[F])
      .reverse

  def commandRepliesData[
      F[_]: Async
  ](
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
      )
    ),
  )

  def buildPollingBot[F[_]: Parallel: Async: Network, A](
      action: M0sconiBotPolling[F] => F[A]
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
      new M0sconiBotPolling[F](
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
  )(implicit log: LogWriter[F]): Resource[F, M0sconiBotWebhook[F]] =
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
      )(Async[F], botSetup.api, botSetup.action, log)
    }
}
