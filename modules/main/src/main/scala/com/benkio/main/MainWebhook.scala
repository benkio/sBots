package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import com.benkio.chattelegramadapter.http.LogTelegramChat
import com.benkio.chattelegramadapter.webhook.TelegramWebhookServer
import com.benkio.main.Logger.given
import org.http4s.server.Server
import telegramium.bots.high.Api

object MainWebhook extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {

    def server(mainSetup: MainSetup[IO]): Resource[IO, Server] = for {
      bots   <- BotRegistry.value.webhookBots(mainSetup)
      server <- TelegramWebhookServer.compose[IO](
        bots = bots,
        port = mainSetup.port,
        host = mainSetup.host,
        keystorePath = mainSetup.keystorePath,
        keystorePassword = mainSetup.keystorePassword
      )
      _ <- Resource.eval(
        bots.foldLeft(IO.unit) { (acc, bot) =>
          acc >> {
            given Api[IO] = bot.sBotSetup.api
            LogTelegramChat.sendText(
              msg = "Start Webook Bot Successful ✅",
              sBotInfo = bot.sBotSetup.sBotConfig.sBotInfo
            )
          }
        }
      )
    } yield server

    (for {
      mainSetup <- MainSetup[IO]()
      _         <- Resource.eval(
        HealthcheckPing.healthcheckPing(
          mainSetup.dbLayer.dbLog,
          mainSetup.httpClient,
          mainSetup.healthcheckEndpoint,
          mainSetup.healthcheckCron
        )
      )
      _ <- GeneralErrorHandling.dbLogAndDie[IO, Server](mainSetup.dbLayer.dbLog, server(mainSetup))
    } yield ExitCode.Success).useForever

  }
}
