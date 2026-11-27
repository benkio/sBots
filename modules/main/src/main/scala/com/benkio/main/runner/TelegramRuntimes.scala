package com.benkio.main.runner

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.model.Platform
import com.benkio.chattelegramadapter.polling.TelegramPollingRuntime
import com.benkio.chattelegramadapter.webhook.TelegramWebhookServer
import com.benkio.chattelegramadapter.SBot
import com.benkio.chattelegramadapter.SBotWebhook
import com.benkio.main.BotRegistryEntry
import com.benkio.main.MainSetup
import log.effect.LogWriter

/** The Telegram bots receive their messages through the webhook, on the HTTP server that serves all of them. */
object TelegramWebhookPlatformRuntime extends PlatformRuntime {

  override val platform: Platform = Platform.Telegram

  override def start(entries: List[BotRegistryEntry[IO]], mainSetup: MainSetup[IO])(using
      log: LogWriter[IO]
  ): Resource[IO, IO[Unit]] =
    for {
      // a bot that cannot be set up is reported and left out: the server serves the others
      attempts <- entries.traverse(entry =>
        SBot
          .buildWebhookBot[IO](
            httpClient = mainSetup.httpClient,
            sBotInfo = entry.sBotInfo,
            webhookBaseUrl = mainSetup.telegram.webhookBaseUrl,
            webhookCertificate = mainSetup.telegram.webhookCertificate,
            commandEffectfulCallback = entry.commandEffectfulCallback,
            opsChatId = mainSetup.telegram.opsChatId,
            llmDependencies = Some(mainSetup.llmDependencies)
          )
          .attempt
          .flatMap(
            _.fold(
              e =>
                Resource
                  .eval(reportStopped(entry, mainSetup)(e))
                  .as(Option.empty[SBotWebhook[IO]]),
              bot => Resource.pure[IO, Option[SBotWebhook[IO]]](Some(bot))
            )
          )
      )
      bots = attempts.flatten
      _ <-
        if bots.isEmpty then Resource.unit[IO]
        else
          TelegramWebhookServer
            .compose[IO](
              bots = bots,
              port = mainSetup.http.port,
              host = mainSetup.http.hostUrl,
              keystorePath = mainSetup.http.keystorePath,
              keystorePassword = mainSetup.http.keystorePassword
            )
            .void
    } yield IO.never
}

/** The Telegram bots ask Telegram for their messages (long polling): no HTTP server. */
object TelegramPollingPlatformRuntime extends PlatformRuntime {

  override val platform: Platform = Platform.Telegram

  override def start(entries: List[BotRegistryEntry[IO]], mainSetup: MainSetup[IO])(using
      log: LogWriter[IO]
  ): Resource[IO, IO[Unit]] =
    Resource.pure(
      entries.parTraverse_(entry =>
        isolated(entry, mainSetup)(
          TelegramPollingRuntime
            .run(
              sBotInfo = entry.sBotInfo,
              opsChatId = mainSetup.telegram.opsChatId,
              llmDependencies = Some(mainSetup.llmDependencies)
            )
            .void
        )
      )
    )
}
