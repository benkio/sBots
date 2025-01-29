package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import com.benkio.abarberobot.ABarberoBot
import com.benkio.calandrobot.CalandroBot
import com.benkio.m0sconibot.M0sconiBot
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.xahleebot.XahLeeBot
import com.benkio.youtuboanchei0bot.YouTuboAncheI0Bot
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import org.http4s.server.Server
import telegramium.bots.high.WebhookBot

object MainWebhook extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

    def server(mainSetup: MainSetup[IO]): Resource[IO, Server] = for {
      xahWebhook <- XahLeeBot.buildWebhookBot[IO](
        httpClient = mainSetup.httpClient,
        webhookBaseUrl = mainSetup.webhookBaseUrl,
        webhookCertificate = mainSetup.webhookCertificate
      )
      youTuboAncheI0BotWebhook <- YouTuboAncheI0Bot.buildWebhookBot[IO](
        httpClient = mainSetup.httpClient,
        webhookBaseUrl = mainSetup.webhookBaseUrl,
        webhookCertificate = mainSetup.webhookCertificate
      )
      m0sconiWebhook <- M0sconiBot.buildWebhookBot[IO](
        httpClient = mainSetup.httpClient,
        webhookBaseUrl = mainSetup.webhookBaseUrl,
        webhookCertificate = mainSetup.webhookCertificate
      )
      calandroWebhook <- CalandroBot.buildWebhookBot[IO](
        httpClient = mainSetup.httpClient,
        webhookBaseUrl = mainSetup.webhookBaseUrl,
        webhookCertificate = mainSetup.webhookCertificate
      )
      richardPHJBensonWebhook <- RichardPHJBensonBot.buildWebhookBot[IO](
        httpClient = mainSetup.httpClient,
        webhookBaseUrl = mainSetup.webhookBaseUrl,
        webhookCertificate = mainSetup.webhookCertificate
      )
      aBarberoWebhook <- ABarberoBot.buildWebhookBot[IO](
        httpClient = mainSetup.httpClient,
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
