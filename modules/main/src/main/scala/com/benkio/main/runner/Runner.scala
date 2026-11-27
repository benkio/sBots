package com.benkio.main.runner

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.main.entriesOn
import com.benkio.main.entriesWithoutRuntime
import com.benkio.main.BotRegistry
import com.benkio.main.BotRegistryEntry
import com.benkio.main.GeneralErrorHandling
import com.benkio.main.HealthcheckPing
import com.benkio.main.MainSetup
import com.benkio.main.StatsLogger
import log.effect.LogWriter

/** Runs the bots of the registry: the runtime of every platform they run on, in one resource, with the HTTP server only
  * where a platform needs it.
  */
object Runner {

  final case class PlatformWithoutRuntime(entry: BotRegistryEntry[IO], platformName: String)
      extends Throwable(
        s"The bot ${entry.sBotInfo.botName} runs on $platformName, which has no runtime to run it"
      )

  /** How the platforms that have a choice get their messages: Telegram, through the webhook or by asking for them. */
  enum RunMode {
    case Webhook, Polling
  }

  /** The runtimes of the platforms that keep their own connection open (IRC, Discord, Slack, ...): they have no webhook
    * or polling to choose from, so they run in every mode. The place to register the runtime of a new one.
    */
  val connectionRuntimes: List[PlatformRuntime] = List(DiscordPlatformRuntime, IrcPlatformRuntime)

  /** The runtimes to start in a mode: the Telegram one of the mode and the ones of the platforms with a connection. */
  def runtimes(mode: RunMode, connection: List[PlatformRuntime] = connectionRuntimes): List[PlatformRuntime] =
    mode match {
      case RunMode.Webhook => TelegramWebhookPlatformRuntime :: connection
      case RunMode.Polling => TelegramPollingPlatformRuntime :: connection
    }

  /** Starts the runtimes with the bots that run on their platform: the `IO` is the work of all of them to keep going. A
    * bot on a platform with no runtime or a runtime whose platform has no bots is not started (e.g. no HTTP server for
    * the Telegram webhook when no bot runs on Telegram).
    */
  private[runner] def start(
      registry: BotRegistry[IO],
      runtimes: List[PlatformRuntime],
      mainSetup: MainSetup[IO]
  )(using log: LogWriter[IO]): Resource[IO, IO[Unit]] =
    for {
      started <- runtimes.traverse(runtime =>
        Option(registry.entriesOn(runtime.platform)).filter(_.nonEmpty).traverse(runtime.start(_, mainSetup))
      )
    } yield started.flatten.parSequence_

  /** A bot on a platform with no runtime is an error. */
  private def checkRuntimes(registry: BotRegistry[IO], runtimes: List[PlatformRuntime]): IO[Unit] = {
    registry
      .entriesWithoutRuntime(runtimes.map(_.platform).toSet)
      .headOption
      .traverse_((entry, platform) => IO.raiseError(PlatformWithoutRuntime(entry, platform.id)))
  }

  /** The registry is checked before the boot is reported: a refused start only signals the failure, never the start.
    */
  private[main] def startReporting(
      registry: BotRegistry[IO],
      runtimes: List[PlatformRuntime],
      mainSetup: MainSetup[IO],
      heartbeat: IO[Unit]
  )(using log: LogWriter[IO]): Resource[IO, IO[Unit]] =
    Resource.eval(checkRuntimes(registry, runtimes) >> heartbeat) >> start(
      registry = registry,
      runtimes = runtimes,
      mainSetup = mainSetup
    )

  /** The bots listen to the platform through the webhook, on an HTTP server, and the health of the application is
    * reported. It never ends but for an error, which is logged.
    */
  def runWebhook(registry: BotRegistry[IO])(using log: LogWriter[IO]): IO[ExitCode] =
    (for {
      mainSetup <- MainSetup[IO]()
      running   <- GeneralErrorHandling.logAndDie[IO, IO[Unit]](
        startReporting(
          registry = registry,
          runtimes = runtimes(RunMode.Webhook),
          mainSetup = mainSetup,
          heartbeat = heartbeat(mainSetup)
        ),
        onError = signalFailure(mainSetup)
      )
    } yield running).use(identity).as(ExitCode.Success)

  /** The bots ask the platform for their messages, no HTTP server. An error is logged and is the exit code. */
  def runPolling(registry: BotRegistry[IO])(using log: LogWriter[IO]): IO[ExitCode] =
    MainSetup[IO]().use { mainSetup =>
      GeneralErrorHandling.logAndDie(
        startReporting(
          registry = registry,
          runtimes = runtimes(RunMode.Polling),
          mainSetup = mainSetup,
          heartbeat = heartbeat(mainSetup)
        )
          .use(identity)
          .as(ExitCode.Success),
        onError = signalFailure(mainSetup)
      )
    }

  /** Signals the boot to the health check and keeps the heartbeat and the system stats going, in every mode. */
  private def heartbeat(mainSetup: MainSetup[IO])(using log: LogWriter[IO]): IO[Unit] =
    HealthcheckPing.healthcheckPing(
      mainSetup.httpClient,
      mainSetup.healthcheckEndpoint,
      mainSetup.healthcheckCron
    ) >> StatsLogger.start[IO](mainSetup.mainDBName)

  private def signalFailure(mainSetup: MainSetup[IO])(reason: String)(using log: LogWriter[IO]): IO[Unit] =
    HealthcheckPing.fail(mainSetup.httpClient, mainSetup.healthcheckEndpoint, reason)
}
