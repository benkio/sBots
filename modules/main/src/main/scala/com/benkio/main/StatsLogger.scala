package com.benkio.main

import cats.effect.syntax.all.*
import cats.effect.Async
import cats.effect.Sync
import cats.syntax.all.*
import com.benkio.chatcore.UsageStats
import fs2.Stream
import log.effect.LogWriter

import java.io.File
import java.lang.management.ManagementFactory
import scala.concurrent.duration.*

/** Logs the `[stats] type=system` line (heap, uptime, DB size and machine RAM/load) every minute, see [[UsageStats]].
  */
object StatsLogger {

  /** Starts logging in the background. An error is logged and does not stop the next lines. */
  def start[F[_]: Async](dbName: String, every: FiniteDuration = 1.minute)(using log: LogWriter[F]): F[Unit] =
    Stream
      .fixedRate[F](every)
      .evalMap(_ =>
        systemLine[F](dbName)
          .flatMap(line => log.info(line))
          .handleErrorWith(e => log.warn(s"[StatsLogger] The system stats could not be collected: $e"))
      )
      .compile
      .drain
      .start
      .void

  def systemLine[F[_]: Sync](dbName: String): F[String] = Sync[F].delay {
    val runtime = Runtime.getRuntime
    UsageStats.systemLine(
      heapUsedBytes = runtime.totalMemory() - runtime.freeMemory(),
      heapMaxBytes = runtime.maxMemory(),
      uptime = ManagementFactory.getRuntimeMXBean.getUptime.millis,
      dbSizeBytes = Option(new File(dbName)).filter(_.isFile).map(_.length()),
      host = HostStatsReader.read()
    )
  }
}
