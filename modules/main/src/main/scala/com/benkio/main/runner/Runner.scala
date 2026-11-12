package com.benkio.main.runner

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.main.entriesOn
import com.benkio.main.BotRegistry
import com.benkio.main.BotRegistryEntry
import com.benkio.main.GeneralErrorHandling
import com.benkio.main.HealthcheckPing
import com.benkio.main.MainSetup
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
    * or polling to choose from, so they run in every mode. The place to register the runtime of a new one: none yet.
    */
  val connectionRuntimes: List[PlatformRuntime] = List.empty

  /** The runtimes to start in a mode: the Telegram one of the mode and the ones of the platforms with a connection. */
  def runtimes(mode: RunMode, connection: List[PlatformRuntime] = connectionRuntimes): List[PlatformRuntime] =
    mode match {
      case RunMode.Webhook => TelegramWebhookPlatformRuntime :: connection
      case RunMode.Polling => TelegramPollingPlatformRuntime :: connection
    }

  /** Starts the runtimes with the bots that run on their platform: the `IO` is the work of all of them to keep going. A
    * bot on a platform with no runtime is an error; a runtime whose platform has no bots is not started (e.g. no HTTP
    * server for the Telegram webhook when no bot runs on Telegram).
    */
  def start(
      registry: BotRegistry[IO],
      runtimes: List[PlatformRuntime],
      mainSetup: MainSetup[IO]
  )(using log: LogWriter[IO]): Resource[IO, IO[Unit]] = {
    val runnablePlatforms = runtimes.map(_.platform).toSet
    val withoutRuntime    = for {
      entry    <- BotRegistry.toList(registry)
      platform <- entry.platforms if !runnablePlatforms.contains(platform)
    } yield PlatformWithoutRuntime(entry, platform.id)
    for {
      _       <- Resource.eval(withoutRuntime.headOption.traverse_(IO.raiseError))
      started <- runtimes.traverse(runtime =>
        Option(registry.entriesOn(runtime.platform)).filter(_.nonEmpty).traverse(runtime.start(_, mainSetup))
      )
    } yield started.flatten.parSequence_
  }

  /** The bots listen to the platform through the webhook, on an HTTP server, and the health of the application is
    * reported. It never ends but for an error, which is logged.
    */
  def runWebhook(registry: BotRegistry[IO])(using log: LogWriter[IO]): IO[ExitCode] =
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
      running <- GeneralErrorHandling.dbLogAndDie[IO, IO[Unit]](
        mainSetup.dbLayer.dbLog,
        start(registry = registry, runtimes = runtimes(RunMode.Webhook), mainSetup = mainSetup)
      )
    } yield running).use(identity).as(ExitCode.Success)

  /** The bots ask the platform for their messages, no HTTP server. An error is logged and is the exit code. */
  def runPolling(registry: BotRegistry[IO])(using log: LogWriter[IO]): IO[ExitCode] =
    MainSetup[IO]().use { mainSetup =>
      GeneralErrorHandling.dbLogAndDie(
        mainSetup.dbLayer.dbLog,
        start(registry = registry, runtimes = runtimes(RunMode.Polling), mainSetup = mainSetup)
          .use(identity)
          .as(ExitCode.Success)
      )
    }
}
