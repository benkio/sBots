package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.implicits._
import com.benkio.abarberobot.ABarberoBot
import com.benkio.calandrobot.CalandroBot
import com.benkio.mosconibot.MosconiBot
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.xahbot.XahBot
import com.benkio.youtuboancheiobot.YoutuboAncheIoBot
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import org.http4s.ember.client._
import org.http4s.server.Server
import telegramium.bots.InputPartFile
import telegramium.bots.high.WebhookBot

import java.io.File

object MainWebhook extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    implicit val log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

    val server: Resource[IO, Server] = for {
      mainSetup <- MainSetup[IO]()
      xahWebhook <- XahBot.buildWebhookBot[IO](
        httpClient = mainSetup.httpClient,
        webhookBaseUrl = mainSetup.webhookBaseUrl,
        webhookCertificate = mainSetup.webhookCertificate
      )
      youtuboAncheIoWebhook <- YoutuboAncheIoBot.buildWebhookBot[IO](
        httpClient = mainSetup.httpClient,
        webhookBaseUrl = mainSetup.webhookBaseUrl,
        webhookCertificate = mainSetup.webhookCertificate
      )
      mosconiWebhook <- MosconiBot.buildWebhookBot[IO](
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
          youtuboAncheIoWebhook,
          mosconiWebhook
        ),
        port = mainSetup.port,
        host = mainSetup.hostUrl
      )
    } yield server

    GeneralErrorHandling.dbLogAndRestart[IO, Server](server).useForever *> ExitCode.Success.pure[IO]
  }
}
