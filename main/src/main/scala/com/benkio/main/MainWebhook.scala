package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.implicits._
import com.benkio.abarberobot.ABarberoBot
import com.benkio.calandrobot.CalandroBot
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.xahbot.XahBot
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import org.http4s.blaze.client._
import telegramium.bots.high.WebhookBot

object MainWebhook extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    implicit val log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)

    val server = for {
      config     <- Resource.eval(Config.loadConfig)
      httpClient <- BlazeClientBuilder[IO].resource
      xahWebhook <- XahBot.buildWebhookBot[IO](
        httpClient = httpClient,
        webhookBaseUrl = config.webhookBaseUrl
      )
      calandroWebhook <- CalandroBot.buildWebhookBot[IO](
        httpClient = httpClient,
        webhookBaseUrl = config.webhookBaseUrl
      )
      richardPHJBensonWebhook <- RichardPHJBensonBot.buildWebhookBot[IO](
        httpClient = httpClient,
        webhookBaseUrl = config.webhookBaseUrl
      )
      aBarberoWebhook <- ABarberoBot.buildWebhookBot[IO](
        httpClient = httpClient,
        webhookBaseUrl = config.webhookBaseUrl
      )
      server <- WebhookBot.compose[IO](
        bots = List(xahWebhook, calandroWebhook, richardPHJBensonWebhook, aBarberoWebhook),
        port = config.port,
        host = config.hostUrl
      )
    } yield server

    server.useForever *> ExitCode.Success.pure[IO]
  }
}
