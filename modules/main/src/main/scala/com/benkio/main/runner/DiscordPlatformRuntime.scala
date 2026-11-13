package com.benkio.main.runner

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.Notifier
import com.benkio.chatdiscordadapter.DiscordBot
import com.benkio.main.BotRegistryEntry
import com.benkio.main.MainSetup
import log.effect.LogWriter

/** The Discord bots keep a connection to the gateway open: no HTTP server. A bot needs its token, a document named
  * `<botId>_<botName>_discord.token`. A bot that fails on Discord (no token, a refused connection) is logged and stops
  * there: it must not take down the bots of the other platforms, which run in the same `IO`. The notifications of the
  * operators go to the log for now.
  */
object DiscordPlatformRuntime extends PlatformRuntime {

  override val platform: Platform = Platform.Discord

  override def start(entries: List[BotRegistryEntry[IO]], mainSetup: MainSetup[IO])(using
      log: LogWriter[IO]
  ): Resource[IO, IO[Unit]] =
    Resource.pure(
      entries.parTraverse_(entry =>
        DiscordBot
          .run[IO](
            httpClient = mainSetup.httpClient,
            sBotInfo = entry.sBotInfo,
            commandEffectfulCallback = entry.commandEffectfulCallback,
            gatewayConfig = mainSetup.discord,
            notifier = Notifier.logging[IO]
          )
          .handleErrorWith(e =>
            log.error(
              s"[DiscordPlatformRuntime] ${entry.sBotInfo.botName} stopped on Discord, the other bots keep running: $e"
            )
          )
      )
    )
}
