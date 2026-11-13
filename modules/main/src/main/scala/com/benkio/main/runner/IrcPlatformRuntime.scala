package com.benkio.main.runner

import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.Notifier
import com.benkio.chatircadapter.IrcBot
import com.benkio.main.BotRegistryEntry
import com.benkio.main.MainSetup
import log.effect.LogWriter

/** The IRC bots keep a connection to the server open (one each, the nick is the name of the bot): no HTTP server. The
  * optional password of the account is the document `<botId>_<botName>_irc.token`. A bot that fails on IRC (no
  * connection in time, ...) is logged and stops there: it must not take down the bots of the other platforms, which run
  * in the same `IO`. The notifications of the operators go to the log for now.
  */
object IrcPlatformRuntime extends PlatformRuntime {

  override val platform: Platform = Platform.Irc

  override def start(entries: List[BotRegistryEntry[IO]], mainSetup: MainSetup[IO])(using
      log: LogWriter[IO]
  ): Resource[IO, IO[Unit]] =
    Resource.pure(
      entries.parTraverse_(entry =>
        IrcBot
          .run[IO](
            httpClient = mainSetup.httpClient,
            sBotInfo = entry.sBotInfo,
            commandEffectfulCallback = entry.commandEffectfulCallback,
            config = mainSetup.irc,
            notifier = Notifier.logging[IO]
          )
          .handleErrorWith(e =>
            log.error(
              s"[IrcPlatformRuntime] ${entry.sBotInfo.botName} stopped on IRC, the other bots keep running: $e"
            )
          )
      )
    )
}
