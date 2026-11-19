package com.benkio.main.runner

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.model.Platform
import com.benkio.main.BotRegistryEntry
import com.benkio.main.HealthcheckPing
import com.benkio.main.MainSetup
import log.effect.LogWriter

/** What it takes to run the bots of the registry on a platform: its adapters, and the HTTP server if the platform
  * receives its messages through it.
  */
trait PlatformRuntime {

  def platform: Platform

  /** Starts what the platform needs for the bots that run on it. They stay started while the resource is acquired and
    * the `IO` it gives is the work to keep going, which ends only when it fails or it is cancelled.
    */
  def start(entries: List[BotRegistryEntry[IO]], mainSetup: MainSetup[IO])(using
      log: LogWriter[IO]
  ): Resource[IO, IO[Unit]]

  /** Runs the work of a bot of this platform with its failure kept its own and reported to the health check. */
  protected final def isolated(entry: BotRegistryEntry[IO], mainSetup: MainSetup[IO])(run: IO[Unit])(using
      log: LogWriter[IO]
  ): IO[Unit] =
    PlatformRuntime.isolated(
      entry = entry,
      platformName = platform.id,
      onFailure = PlatformRuntime.healthCheckFailure(mainSetup)
    )(run)

  /** Reports a bot of this platform that could not be set up: it is left out, the others run. */
  protected final def reportStopped(entry: BotRegistryEntry[IO], mainSetup: MainSetup[IO])(e: Throwable)(using
      log: LogWriter[IO]
  ): IO[Unit] =
    PlatformRuntime.reportStopped(
      entry = entry,
      platformName = platform.id,
      onFailure = PlatformRuntime.healthCheckFailure(mainSetup)
    )(e)
}

object PlatformRuntime {

  /** What a bot that stopped signals: the health check fails. */
  def healthCheckFailure(mainSetup: MainSetup[IO])(using log: LogWriter[IO]): String => IO[Unit] =
    reason => HealthcheckPing.fail(mainSetup.httpClient, mainSetup.healthcheckEndpoint, reason)

  /** A bot that fails on a platform is logged and reported through `onFailure`, and stops there: it must not take down
    * the other bots, which run in the same `IO`.
    */
  def reportStopped(entry: BotRegistryEntry[IO], platformName: String, onFailure: String => IO[Unit])(
      e: Throwable
  )(using log: LogWriter[IO]): IO[Unit] = {
    val reason = s"${entry.sBotInfo.botName} stopped on $platformName, the other bots keep running: $e"
    log.error(s"[PlatformRuntime] $reason") *> onFailure(reason)
  }

  /** Runs the work of a bot with its failure kept its own, see `reportStopped`. */
  def isolated(entry: BotRegistryEntry[IO], platformName: String, onFailure: String => IO[Unit])(run: IO[Unit])(using
      log: LogWriter[IO]
  ): IO[Unit] =
    run.handleErrorWith(reportStopped(entry = entry, platformName = platformName, onFailure = onFailure))
}
