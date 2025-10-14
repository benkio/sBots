package com.benkio.calandrobot

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.model.reply.mp3
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.model.reply.TextReply
import com.benkio.telegrambotinfrastructure.model.reply.TextReplyM
import com.benkio.telegrambotinfrastructure.model.tr
import com.benkio.telegrambotinfrastructure.model.CommandInstructionData
import com.benkio.telegrambotinfrastructure.model.MessageLengthTrigger
import com.benkio.telegrambotinfrastructure.model.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotName
import com.benkio.telegrambotinfrastructure.model.StringTextTriggerValue
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.MediaByKindCommand
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.SBot
import com.benkio.telegrambotinfrastructure.SBotPolling
import com.benkio.telegrambotinfrastructure.SBotWebhook
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.ember.client.*
import org.http4s.implicits.*
import org.http4s.Uri
import telegramium.bots.high.*
import telegramium.bots.InputPartFile
import telegramium.bots.Message

import scala.util.Random

class CalandroBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    repositoryInput: Repository[F],
    val dbLayer: DBLayer[F]
) extends SBotPolling[F](repositoryInput)
    with CalandroBot[F] {
  override def repository: Repository[F] =
    repositoryInput
}

class CalandroBotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    repositoryInput: Repository[F],
    val dbLayer: DBLayer[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends SBotWebhook[F](uri, path, webhookCertificate, repositoryInput)
    with CalandroBot[F] {
  override def repository: Repository[F] =
    repositoryInput
}

trait CalandroBot[F[_]: Async: LogWriter] extends SBot[F] {

  override val botName: SBotName       = CalandroBot.botName
  override val botId: SBotId           = CalandroBot.botId
  override val triggerFilename: String = CalandroBot.triggerFilename
  override val triggerListUri: Uri     = CalandroBot.triggerListUri

  override def messageRepliesDataF: F[List[ReplyBundleMessage[F]]] =
    CalandroBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF: F[List[ReplyBundleCommand[F]]] =
    CalandroBot.commandRepliesData[F](dbLayer = dbLayer).pure[F]
}

object CalandroBot {

  val botName: SBotName       = SBotName("CalandroBot")
  val botId: SBotId           = SBotId("cala")
  val tokenFilename: String   = "cala_CalandroBot.token"
  val configNamespace: String = "cala"
  val triggerFilename: String = "cala_triggers.txt"
  val triggerListUri: Uri = uri"https://github.com/benkio/sBots/blob/main/modules/bots/CalandroBot/abar_triggers.txt"

  def messageRepliesData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List(
    ReplyBundleMessage.textToText[F](
      "sbrighi"
    )("Passo"),
    ReplyBundleMessage.textToText[F](
      "gay",
      "froc[io]".r.tr(5),
      "culattone",
      "ricchione"
    )("CHE SCHIFO!!!"),
    ReplyBundleMessage.textToText[F](
      "caldo",
      "scotta"
    )("Come i carbofreni della Brembo!!"),
    ReplyBundleMessage.textToText[F](
      "ciao",
      "buongiorno",
      "\\bsalve\\b".r.tr(5)
    )("Buongiorno Signori"),
    ReplyBundleMessage.textToText[F](
      "film"
    )("Lo riguardo volentieri"),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stasera"),
        StringTextTriggerValue("?")
      ),
      reply = TextReply[F](List(Text("Facciamo qualcosa tutti assieme?")), false),
      matcher = MessageMatches.ContainsAll
    ),
    ReplyBundleMessage.textToText[F](
      "\\bhd\\b".r.tr(2),
      "nitid(o|ezza)".r.tr(6),
      "alta definizione"
    )("Eh sì, vedi...si nota l'indecisione dell'immagine"),
    ReplyBundleMessage.textToText[F](
      "qualità"
    )("A 48x masterizza meglio"),
    ReplyBundleMessage.textToText[F](
      "macchina",
      "automobile"
    )("Hai visto l'ultima puntata di \"Top Gear\"?"),
    ReplyBundleMessage.textToText[F](
      "\\bfiga\\b".r.tr(4),
      "\\bfregna\\b".r.tr(6),
      "\\bgnocca\\b".r.tr(6),
      "\\bpatacca\\b".r.tr(7)
    )("Io so come fare con le donne...ho letto tutto..."),
    ReplyBundleMessage.textToText[F](
      "ambulanza",
      "🚑"
    )(
      "😤",
      "🤘",
      "🤞",
      "🤞",
      "🤘",
      "😤"
    ),
    ReplyBundleMessage.textToText[F](
      "pc",
      "computer"
    )("Il fisso performa meglio rispetto al portatile!!!"),
    ReplyBundleMessage.textToText[F](
      "videogioc",
      "🎮"
    )(s"GIOCHI PER IL MIO PC #${Random.nextInt(Int.MaxValue)}??No ma io non lo compro per i giochi!!!"),
    ReplyBundleMessage.textToText[F](
      " hs",
      "hearthstone"
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

  def commandRepliesData[F[_]: Async: LogWriter](dbLayer: DBLayer[F]): List[ReplyBundleCommand[F]] =
    List(
      MediaByKindCommand.mediaCommandByKind(
        dbMedia = dbLayer.dbMedia,
        commandName = "randomcard",
        kind = "cards".some,
        instruction = CommandInstructionData.NoInstructions,
        botId = CalandroBot.botId
      ),
      ReplyBundleCommand.textToMedia[F]("porcoladro", CommandInstructionData.NoInstructions)(
        mp3"cala_PorcoLadro.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("unoduetre", CommandInstructionData.NoInstructions)(
        mp3"cala_Unoduetre.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("ancorauna", CommandInstructionData.NoInstructions)(
        mp3"cala_AncoraUnaDoveLaMetto.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("lacipolla", CommandInstructionData.NoInstructions)(
        mp3"cala_CipollaCalandrica.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("lavorogiusto", CommandInstructionData.NoInstructions)(
        mp3"cala_IlLavoroVaPagato.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("motivazioniinternet", CommandInstructionData.NoInstructions)(
        mp3"cala_InternetMotivazioniCalandriche.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("cazzomene", CommandInstructionData.NoInstructions)(
        mp3"cala_IoSonVaccinato.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("arrivoarrivo", CommandInstructionData.NoInstructions)(
        mp3"cala_SubmissionCalandra.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("vaginadepilata", CommandInstructionData.NoInstructions)(
        mp3"cala_VaginaDepilataCalandra.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("whawha_fallout4", CommandInstructionData.NoInstructions)(
        mp3"cala_Waawahaawha.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("whawha_short", CommandInstructionData.NoInstructions)(
        mp3"cala_WwhaaawhaaaSingolo.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("daccordissimo", CommandInstructionData.NoInstructions)(
        mp3"cala_D_accordissimo.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("stocazzo", CommandInstructionData.NoInstructions)(
        mp3"cala_Stocazzo.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("cazzodibudda", CommandInstructionData.NoInstructions)(
        mp3"cala_CazzoDiBudda.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("personapulita", CommandInstructionData.NoInstructions)(
        mp3"cala_PersonaPulita.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("losquirt", CommandInstructionData.NoInstructions)(
        mp3"cala_LoSquirt.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("fuoridalmondo", CommandInstructionData.NoInstructions)(
        mp3"cala_FuoriDalMondo.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("qualitaolive", CommandInstructionData.NoInstructions)(
        mp3"cala_QualitaOlive.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("gioielli", CommandInstructionData.NoInstructions)(
        mp3"cala_Gioielli.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("risata", CommandInstructionData.NoInstructions)(
        mp3"cala_RisataCalandrica.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("sonocosternato", CommandInstructionData.NoInstructions)(
        mp3"cala_SonoCosternato.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("demenza", CommandInstructionData.NoInstructions)(
        mp3"cala_LaDemenzaDiUnUomo.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("wha", CommandInstructionData.NoInstructions)(
        mp3"cala_WhaSecco.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("imparatounafava", CommandInstructionData.NoInstructions)(
        mp3"cala_ImparatoUnaFava.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("lesbiche", CommandInstructionData.NoInstructions)(
        mp3"cala_SieteLesbiche.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("firstlesson", CommandInstructionData.NoInstructions)(
        mp3"cala_FirstLessonPlease.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("noprogrammato", CommandInstructionData.NoInstructions)(
        mp3"cala_NoGrazieProgrammato.mp3"
      ),
      ReplyBundleCommand.textToMedia[F]("fiammeinferno", CommandInstructionData.NoInstructions)(
        mp3"cala_Fiamme.mp3"
      )
    )

  def buildPollingBot[F[_]: Parallel: Async: Network, A](
      action: CalandroBotPolling[F] => F[A]
  )(using log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
    botSetup   <- BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botId = botId
    )
  } yield botSetup).use { botSetup =>
    action(
      new CalandroBotPolling[F](
        repositoryInput = botSetup.repository,
        dbLayer = botSetup.dbLayer
      )(using Parallel[F], Async[F], botSetup.api, log)
    )
  }

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, CalandroBotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botId = botId,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new CalandroBotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        repositoryInput = botSetup.repository,
        dbLayer = botSetup.dbLayer,
        webhookCertificate = webhookCertificate
      )(using Async[F], botSetup.api, log)
    }
}
