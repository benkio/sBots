package com.benkio.calandrobot

import cats.*
import cats.effect.*
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.model.reply.mp3
import com.benkio.telegrambotinfrastructure.model.reply.txt
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.reply.TextReply
import com.benkio.telegrambotinfrastructure.model.CommandInstructionData
import com.benkio.telegrambotinfrastructure.model.MessageLengthTrigger
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotName
import com.benkio.telegrambotinfrastructure.model.StringTextTriggerValue
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns.MediaByKindCommand
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

import scala.util.Random

class CalandroBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F]
) extends SBotPolling[F](sBotSetup)
    with CalandroBot[F] {}

class CalandroBotWebhook[F[_]: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    webhookCertificate: Option[InputPartFile] = None
) extends SBotWebhook[F](sBotSetup, webhookCertificate)
    with CalandroBot[F] {}

trait CalandroBot[F[_]: Applicative] extends SBot[F] {

  override val messageRepliesData: F[List[ReplyBundleMessage]] =
    Applicative[F].pure(CalandroBot.messageRepliesData)

  override val commandRepliesData: List[ReplyBundleCommand] =
    CalandroBot.commandRepliesData
}

object CalandroBot {

  val tokenFilename: String   = "cala_CalandroBot.token"
  val configNamespace: String = "cala"
  val sBotConfig: SBotConfig  = SBotConfig(
    sBotInfo = SBotInfo(SBotId("cala"), SBotName("CalandroBot")),
    triggerFilename = "cala_triggers.txt",
    triggerListUri = uri"https://github.com/benkio/sBots/blob/main/modules/bots/CalandroBot/cala_triggers.txt",
    repliesJsonFilename = "cala_replies.json"
  )

  val messageRepliesData: List[ReplyBundleMessage] = List(
    ReplyBundleMessage.textToText(
      "sbrighi"
    )("Passo"),
    ReplyBundleMessage.textToText(
      "gay",
      "froc[io]".r,
      "culattone",
      "ricchione"
    )("CHE SCHIFO!!!"),
    ReplyBundleMessage.textToText(
      "caldo",
      "scotta"
    )("Come i carbofreni della Brembo!!"),
    ReplyBundleMessage.textToText(
      "ciao",
      "buongiorno",
      "\\bsalve\\b".r
    )("Buongiorno Signori"),
    ReplyBundleMessage.textToText(
      "film"
    )("Lo riguardo volentieri"),
    ReplyBundleMessage(
      TextTrigger(
        StringTextTriggerValue("stasera"),
        StringTextTriggerValue("?")
      ),
      reply = TextReply.fromList("Facciamo qualcosa tutti assieme?")(false),
      matcher = MessageMatches.ContainsAll
    ),
    ReplyBundleMessage.textToText(
      "\\bhd\\b".r,
      "nitid(o|ezza)".r,
      "alta definizione"
    )("Eh sì, vedi...si nota l'indecisione dell'immagine"),
    ReplyBundleMessage.textToText(
      "qualità"
    )("A 48x masterizza meglio"),
    ReplyBundleMessage.textToText(
      "macchina",
      "automobile"
    )("Hai visto l'ultima puntata di \"Top Gear\"?"),
    ReplyBundleMessage.textToText(
      "\\bfiga\\b".r,
      "\\bfregna\\b".r,
      "\\bgnocca\\b".r,
      "\\bpatacca\\b".r
    )("Io so come fare con le donne...ho letto tutto..."),
    ReplyBundleMessage.textToText(
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
    ReplyBundleMessage.textToText(
      "pc",
      "computer"
    )("Il fisso performa meglio rispetto al portatile!!!"),
    ReplyBundleMessage.textToText(
      "videogioc",
      "🎮"
    )(s"GIOCHI PER IL MIO PC #${Random.nextInt(Int.MaxValue)}??No ma io non lo compro per i giochi!!!"),
    ReplyBundleMessage.textToText(
      " hs",
      "hearthstone"
    )("BASTA CON QUESTI TAUNT!!!"),
    ReplyBundleMessage(
      MessageLengthTrigger(280),
      reply = TextReply(
        text = List(txt"wawaaa rischio calandrico in aumento!!!"),
        replyToMessage = true
      ),
      matcher = MessageMatches.ContainsOnce
    )
  )

  val commandRepliesData: List[ReplyBundleCommand] =
    List(
      MediaByKindCommand.mediaCommandByKind(
        commandName = "randomcard",
        instruction = CommandInstructionData.NoInstructions,
        sBotInfo = sBotConfig.sBotInfo
      ),
      ReplyBundleCommand.textToMedia("porcoladro", CommandInstructionData.NoInstructions)(
        mp3"cala_PorcoLadro.mp3"
      ),
      ReplyBundleCommand.textToMedia("unoduetre", CommandInstructionData.NoInstructions)(
        mp3"cala_Unoduetre.mp3"
      ),
      ReplyBundleCommand.textToMedia("ancorauna", CommandInstructionData.NoInstructions)(
        mp3"cala_AncoraUnaDoveLaMetto.mp3"
      ),
      ReplyBundleCommand.textToMedia("lacipolla", CommandInstructionData.NoInstructions)(
        mp3"cala_CipollaCalandrica.mp3"
      ),
      ReplyBundleCommand.textToMedia("lavorogiusto", CommandInstructionData.NoInstructions)(
        mp3"cala_IlLavoroVaPagato.mp3"
      ),
      ReplyBundleCommand.textToMedia("motivazioniinternet", CommandInstructionData.NoInstructions)(
        mp3"cala_InternetMotivazioniCalandriche.mp3"
      ),
      ReplyBundleCommand.textToMedia("cazzomene", CommandInstructionData.NoInstructions)(
        mp3"cala_IoSonVaccinato.mp3"
      ),
      ReplyBundleCommand.textToMedia("arrivoarrivo", CommandInstructionData.NoInstructions)(
        mp3"cala_SubmissionCalandra.mp3"
      ),
      ReplyBundleCommand.textToMedia("vaginadepilata", CommandInstructionData.NoInstructions)(
        mp3"cala_VaginaDepilataCalandra.mp3"
      ),
      ReplyBundleCommand.textToMedia("whawha_fallout4", CommandInstructionData.NoInstructions)(
        mp3"cala_Waawahaawha.mp3"
      ),
      ReplyBundleCommand.textToMedia("whawha_short", CommandInstructionData.NoInstructions)(
        mp3"cala_WwhaaawhaaaSingolo.mp3"
      ),
      ReplyBundleCommand.textToMedia("daccordissimo", CommandInstructionData.NoInstructions)(
        mp3"cala_DAccordissimo.mp3"
      ),
      ReplyBundleCommand.textToMedia("stocazzo", CommandInstructionData.NoInstructions)(
        mp3"cala_Stocazzo.mp3"
      ),
      ReplyBundleCommand.textToMedia("cazzodibudda", CommandInstructionData.NoInstructions)(
        mp3"cala_CazzoDiBudda.mp3"
      ),
      ReplyBundleCommand.textToMedia("personapulita", CommandInstructionData.NoInstructions)(
        mp3"cala_PersonaPulita.mp3"
      ),
      ReplyBundleCommand.textToMedia("losquirt", CommandInstructionData.NoInstructions)(
        mp3"cala_LoSquirt.mp3"
      ),
      ReplyBundleCommand.textToMedia("fuoridalmondo", CommandInstructionData.NoInstructions)(
        mp3"cala_FuoriDalMondo.mp3"
      ),
      ReplyBundleCommand.textToMedia("qualitaolive", CommandInstructionData.NoInstructions)(
        mp3"cala_QualitaOlive.mp3"
      ),
      ReplyBundleCommand.textToMedia("gioielli", CommandInstructionData.NoInstructions)(
        mp3"cala_Gioielli.mp3"
      ),
      ReplyBundleCommand.textToMedia("risata", CommandInstructionData.NoInstructions)(
        mp3"cala_RisataCalandrica.mp3"
      ),
      ReplyBundleCommand.textToMedia("sonocosternato", CommandInstructionData.NoInstructions)(
        mp3"cala_SonoCosternato.mp3"
      ),
      ReplyBundleCommand.textToMedia("demenza", CommandInstructionData.NoInstructions)(
        mp3"cala_LaDemenzaDiUnUomo.mp3"
      ),
      ReplyBundleCommand.textToMedia("wha", CommandInstructionData.NoInstructions)(
        mp3"cala_WhaSecco.mp3"
      ),
      ReplyBundleCommand.textToMedia("imparatounafava", CommandInstructionData.NoInstructions)(
        mp3"cala_ImparatoUnaFava.mp3"
      ),
      ReplyBundleCommand.textToMedia("lesbiche", CommandInstructionData.NoInstructions)(
        mp3"cala_SieteLesbiche.mp3"
      ),
      ReplyBundleCommand.textToMedia("firstlesson", CommandInstructionData.NoInstructions)(
        mp3"cala_FirstLessonPlease.mp3"
      ),
      ReplyBundleCommand.textToMedia("noprogrammato", CommandInstructionData.NoInstructions)(
        mp3"cala_NoGrazieProgrammato.mp3"
      ),
      ReplyBundleCommand.textToMedia("fiammeinferno", CommandInstructionData.NoInstructions)(
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
      sBotConfig = sBotConfig
    )
  } yield botSetup).use(botSetup =>
    action(new CalandroBotPolling[F](botSetup)(using Parallel[F], Async[F], botSetup.api, log))
  )

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, CalandroBotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      sBotConfig = sBotConfig,
      webhookBaseUrl = webhookBaseUrl
    ).map(botSetup =>
      new CalandroBotWebhook[F](botSetup, webhookCertificate)(using Async[F], botSetup.api, log)
    )
}
