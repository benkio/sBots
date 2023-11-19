package com.benkio.calandrobot

import com.benkio.telegrambotinfrastructure.model.TextReplyM
import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure._
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering._
import com.benkio.telegrambotinfrastructure.model._
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
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.MediaByKindCommand

import scala.util.Random

class CalandroBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    resourceAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F]
) extends BotSkeletonPolling[F](resourceAccess)
    with CalandroBot[F] {
  override def resourceAccess(using syncF: Sync[F]): ResourceAccess[F] = resourceAccess
}

class CalandroBotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    resourceAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends BotSkeletonWebhook[F](uri, path, webhookCertificate, resourceAccess)
    with CalandroBot[F] {
  override def resourceAccess(using syncF: Sync[F]): ResourceAccess[F] = resourceAccess
}

trait CalandroBot[F[_]] extends BotSkeleton[F] {

  override val botName: String   = CalandroBot.botName
  override val botPrefix: String = CalandroBot.botPrefix

  override def messageRepliesDataF(using
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    CalandroBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(using asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    CalandroBot.commandRepliesData[F](dbLayer = dbLayer, botName = botName).pure[F]
}

object CalandroBot {

  val botName: String         = "CalandroBot"
  val botPrefix: String       = "cala"
  val tokenFilename: String   = "cala_CalandroBot.token"
  val configNamespace: String = "calaDB"

  def messageRepliesData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToText[F](
      stt"sbrighi"
    )("Passo"),
    ReplyBundleMessage.textToText[F](
      stt"gay",
      "froc[io]".r.tr(5),
      stt"culattone",
      stt"ricchione"
    )("CHE SCHIFO!!!"),
    ReplyBundleMessage.textToText[F](
      stt"caldo",
      stt"scotta"
    )("Come i carbofreni della Brembo!!"),
    ReplyBundleMessage.textToText[F](
      stt"ciao",
      stt"buongiorno",
      "\\bsalve\\b".r.tr(5)
    )("Buongiorno Signori"),
    ReplyBundleMessage.textToText[F](
      stt"film"
    )("Lo riguardo volentieri"),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stasera"),
        StringTextTriggerValue("?")
      ),
      reply = TextReplyM[F](_ => Applicative[F].pure(List(Text("Facciamo qualcosa tutti assieme?"))), false),
      matcher = ContainsAll
    ),
    ReplyBundleMessage.textToText[F](
      "\\bhd\\b".r.tr(2),
      "nitid(o|ezza)".r.tr(6),
      stt"alta definizione"
    )("Eh sì, vedi...si nota l'indecisione dell'immagine"),
    ReplyBundleMessage.textToText[F](
      stt"qualità"
    )("A 48x masterizza meglio"),
    ReplyBundleMessage.textToText[F](
      stt"macchina",
      stt"automobile"
    )("Hai visto l'ultima puntata di \"Top Gear\"?"),
    ReplyBundleMessage.textToText[F](
      "\\bfiga\\b".r.tr(4),
      "\\bfregna\\b".r.tr(6),
      "\\bgnocca\\b".r.tr(6),
      "\\bpatacca\\b".r.tr(7)
    )("Io so come fare con le donne...ho letto tutto..."),
    ReplyBundleMessage.textToText[F](
      stt"ambulanza",
      stt"🚑"
    )(
      "😤",
      "🤘",
      "🤞",
      "🤞",
      "🤘",
      "😤"
    ),
    ReplyBundleMessage.textToText[F](
      stt"pc",
      stt"computer"
    )("Il fisso performa meglio rispetto al portatile!!!"),
    ReplyBundleMessage.textToText[F](
      stt"videogioc",
      stt"🎮"
    )(s"GIOCHI PER IL MIO PC #${Random.nextInt(Int.MaxValue)}??No ma io non lo compro per i giochi!!!"),
    ReplyBundleMessage.textToText[F](
      stt" hs",
      stt"hearthstone"
    )("BASTA CON QUESTI TAUNT!!!"),
    ReplyBundleMessage(
      MessageLengthTrigger(280),
      reply = TextReplyM[F](
        (msg: Message) =>
          Applicative[F].pure(
            List(Text(s"""wawaaa rischio calandrico in aumento(${msg.text.getOrElse("").length} / 280)"""))
          ),
        true
      )
    )
  )

  def commandRepliesData[F[_]: Async: LogWriter](dbLayer: DBLayer[F], botName: String): List[ReplyBundleCommand[F]] =
    List(
      MediaByKindCommand.mediaCommandByKind(
        dbMedia = dbLayer.dbMedia,
        botName = botName,
        commandName = "randomcard",
        kind = "cards".some
      ),
      ReplyBundleCommand.textToMedia[F]("porcoladro")(mf"cala_PorcoLadro.mp3"),
      ReplyBundleCommand.textToMedia[F]("unoduetre")(mf"cala_Unoduetre.mp3"),
      ReplyBundleCommand.textToMedia[F]("ancorauna")(mf"cala_AncoraUnaDoveLaMetto.mp3"),
      ReplyBundleCommand.textToMedia[F]("lacipolla")(mf"cala_CipollaCalandrica.mp3"),
      ReplyBundleCommand.textToMedia[F]("lavorogiusto")(mf"cala_IlLavoroVaPagato.mp3"),
      ReplyBundleCommand.textToMedia[F]("motivazioniinternet")(mf"cala_InternetMotivazioniCalandriche.mp3"),
      ReplyBundleCommand.textToMedia[F]("cazzomene")(mf"cala_IoSonVaccinato.mp3"),
      ReplyBundleCommand.textToMedia[F]("arrivoarrivo")(mf"cala_SubmissionCalandra.mp3"),
      ReplyBundleCommand.textToMedia[F]("vaginadepilata")(mf"cala_VaginaDepilataCalandra.mp3"),
      ReplyBundleCommand.textToMedia[F]("whawha_fallout4")(mf"cala_Waawahaawha.mp3"),
      ReplyBundleCommand.textToMedia[F]("whawha_short")(mf"cala_WwhaaawhaaaSingolo.mp3"),
      ReplyBundleCommand.textToMedia[F]("daccordissimo")(mf"cala_D_accordissimo.mp3"),
      ReplyBundleCommand.textToMedia[F]("stocazzo")(mf"cala_Stocazzo.mp3"),
      ReplyBundleCommand.textToMedia[F]("cazzodibudda")(mf"cala_CazzoDiBudda.mp3"),
      ReplyBundleCommand.textToMedia[F]("personapulita")(mf"cala_PersonaPulita.mp3"),
      ReplyBundleCommand.textToMedia[F]("losquirt")(mf"cala_LoSquirt.mp3"),
      ReplyBundleCommand.textToMedia[F]("fuoridalmondo")(mf"cala_FuoriDalMondo.mp3"),
      ReplyBundleCommand.textToMedia[F]("qualitaolive")(mf"cala_QualitaOlive.mp3"),
      ReplyBundleCommand.textToMedia[F]("gioielli")(mf"cala_Gioielli.mp3"),
      ReplyBundleCommand.textToMedia[F]("risata")(mf"cala_RisataCalandrica.mp3"),
      ReplyBundleCommand.textToMedia[F]("sonocosternato")(mf"cala_SonoCosternato.mp3"),
      ReplyBundleCommand.textToMedia[F]("demenza")(mf"cala_LaDemenzaDiUnUomo.mp3"),
      ReplyBundleCommand.textToMedia[F]("wha")(mf"cala_WhaSecco.mp3"),
      ReplyBundleCommand.textToMedia[F]("imparatounafava")(mf"cala_ImparatoUnaFava.mp3"),
      ReplyBundleCommand.textToMedia[F]("lesbiche")(mf"cala_SieteLesbiche.mp3"),
      ReplyBundleCommand.textToMedia[F]("firstlesson")(mf"cala_FirstLessonPlease.mp3"),
      ReplyBundleCommand.textToMedia[F]("noprogrammato")(mf"cala_NoGrazieProgrammato.mp3"),
      ReplyBundleCommand.textToMedia[F]("fiammeinferno")(mf"cala_Fiamme.mp3")
    )

  def buildPollingBot[F[_]: Parallel: Async: Network, A](
      action: CalandroBotPolling[F] => F[A]
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
      new CalandroBotPolling[F](
        resourceAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer
      )(Parallel[F], Async[F], botSetup.api, log)
    )
  }

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(implicit log: LogWriter[F]): Resource[F, CalandroBotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botName = botName,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new CalandroBotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        resourceAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        webhookCertificate = webhookCertificate
      )(Async[F], botSetup.api, log)
    }
}
