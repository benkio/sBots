package com.benkio.calandrobot

import cats.*
import cats.effect.*
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.model.reply.mp3
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.CommandInstructionData
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotName
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

class CalandroBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    override val messageRepliesData: List[ReplyBundleMessage]
) extends SBotPolling[F](sBotSetup)
    with CalandroBot[F] {}

class CalandroBotWebhook[F[_]: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    override val messageRepliesData: List[ReplyBundleMessage],
    webhookCertificate: Option[InputPartFile] = None
) extends SBotWebhook[F](sBotSetup, webhookCertificate)
    with CalandroBot[F] {}

trait CalandroBot[F[_]] extends SBot[F] {

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
    messageRepliesData <- Resource.eval(
      botSetup.jsonRepliesRepository.loadReplies(CalandroBot.sBotConfig.repliesJsonFilename)
    )
  } yield (botSetup, messageRepliesData)).use { case (botSetup, messageRepliesData) =>
    action(
      new CalandroBotPolling[F](sBotSetup = botSetup, messageRepliesData = messageRepliesData)(using
        Parallel[F],
        Async[F],
        botSetup.api,
        log
      )
    )
  }

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, CalandroBotWebhook[F]] = for {
    botSetup <- BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      sBotConfig = sBotConfig,
      webhookBaseUrl = webhookBaseUrl
    )
    messageRepliesData <- Resource.eval(
      botSetup.jsonRepliesRepository.loadReplies(CalandroBot.sBotConfig.repliesJsonFilename)
    )
  } yield new CalandroBotWebhook[F](botSetup, messageRepliesData, webhookCertificate)(using Async[F], botSetup.api, log)
}
