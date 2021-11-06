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
import org.http4s.blaze.client._
import telegramium.bots.high.WebhookBot

import scala.concurrent.ExecutionContext.global

object MainWebhook extends IOApp {
  def run(args: List[String]): IO[ExitCode] = (for {
    config     <- Resource.eval(Config.loadConfig)
    httpClient <- BlazeClientBuilder[IO](global).resource
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
  } yield server).useForever *> ExitCode.Success.pure[IO]
}
