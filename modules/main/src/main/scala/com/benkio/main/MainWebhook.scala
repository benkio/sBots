package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import com.benkio.telegrambotinfrastructure.SBot
import com.benkio.ABarberoBot.ABarberoBot
import com.benkio.CalandroBot.CalandroBot
import com.benkio.M0sconiBot.M0sconiBot
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import com.benkio.XahLeeBot.XahLeeBot
import com.benkio.YouTuboAncheI0Bot.YouTuboAncheI0Bot
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import org.http4s.server.Server
import telegramium.bots.high.WebhookBot

object MainWebhook extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

    def server(mainSetup: MainSetup[IO]): Resource[IO, Server] = for {
      xahWebhook <- SBot.buildWebhookBot[IO](
        httpClient = mainSetup.httpClient,
        sBotInfo = XahLeeBot.sBotInfo,
        webhookBaseUrl = mainSetup.webhookBaseUrl,
        webhookCertificate = mainSetup.webhookCertificate
      )
      youTuboAncheI0BotWebhook <- SBot.buildWebhookBot[IO](
        httpClient = mainSetup.httpClient,
        sBotInfo = YouTuboAncheI0Bot.sBotInfo,
        webhookBaseUrl = mainSetup.webhookBaseUrl,
        webhookCertificate = mainSetup.webhookCertificate
      )
      m0sconiWebhook <- SBot.buildWebhookBot[IO](
        httpClient = mainSetup.httpClient,
        sBotInfo = M0sconiBot.sBotInfo,
        webhookBaseUrl = mainSetup.webhookBaseUrl,
        webhookCertificate = mainSetup.webhookCertificate
      )
      calandroWebhook <- SBot.buildWebhookBot[IO](
        httpClient = mainSetup.httpClient,
        sBotInfo = CalandroBot.sBotInfo,
        webhookBaseUrl = mainSetup.webhookBaseUrl,
        webhookCertificate = mainSetup.webhookCertificate
      )
      richardPHJBensonWebhook <- RichardPHJBensonBot.buildWebhookBot[IO](
        httpClient = mainSetup.httpClient,
        webhookBaseUrl = mainSetup.webhookBaseUrl,
        webhookCertificate = mainSetup.webhookCertificate
      )
      aBarberoWebhook <- SBot.buildWebhookBot[IO](
        httpClient = mainSetup.httpClient,
        sBotInfo = ABarberoBot.sBotInfo,
        webhookBaseUrl = mainSetup.webhookBaseUrl,
        webhookCertificate = mainSetup.webhookCertificate
      )
      server <- WebhookBot.compose[IO](
        bots = List(
          xahWebhook,
          calandroWebhook,
          richardPHJBensonWebhook,
          aBarberoWebhook,
          youTuboAncheI0BotWebhook,
          m0sconiWebhook
        ),
        port = mainSetup.port,
        host = mainSetup.host,
        keystorePath = mainSetup.keystorePath,
        keystorePassword = mainSetup.keystorePassword
      )
    } yield server

    (for {
      mainSetup <- MainSetup[IO]()
      _         <- GeneralErrorHandling.dbLogAndRestart[IO, Server](mainSetup.dbLayer.dbLog, server(mainSetup))
    } yield ExitCode.Success).useForever

  }
}
