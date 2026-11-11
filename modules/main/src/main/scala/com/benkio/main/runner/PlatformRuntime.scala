package com.benkio.main.runner

import cats.effect.IO
import cats.effect.Resource
import com.benkio.chatcore.model.Platform
import com.benkio.main.BotRegistryEntry
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
}
