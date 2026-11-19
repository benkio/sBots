package com.benkio.chatcore

import scala.concurrent.duration.FiniteDuration

/** The lines of the usage and health stats. They are logged at `info`, in the `logfmt` style, with the `[stats]` tag:
  * in Loki they are read with `{app="sbots"} | json | line_format "{{.message}}" | logfmt | type="message"`, see
  * `docs/monitoring.md`. Keep the keys stable: the dashboard depends on them.
  */
object UsageStats {

  /** What happened to a message. */
  enum Outcome(val label: String) {

    /** The message was processed, whether it matched something to reply to or not. */
    case Handled extends Outcome("handled")

    /** The platform delivered the message again: it was skipped. */
    case Duplicate extends Outcome("duplicate")

    /** The processing took more than the request timeout and was dropped. */
    case TimedOut extends Outcome("timeout")

    /** The processing ended with an error. */
    case Failed extends Outcome("failed")
  }

  def messageLine(bot: String, platform: String, outcome: Outcome, latency: FiniteDuration): String =
    s"[stats] type=message bot=$bot platform=$platform outcome=${outcome.label} latency_ms=${latency.toMillis}"

  def systemLine(heapUsedBytes: Long, heapMaxBytes: Long, uptime: FiniteDuration, dbSizeBytes: Option[Long]): String =
    s"[stats] type=system heap_used_mb=${toMb(heapUsedBytes)} heap_max_mb=${toMb(heapMaxBytes)} " +
      s"uptime_s=${uptime.toSeconds} db_mb=${dbSizeBytes.fold(-1L)(toMb)}"

  private def toMb(bytes: Long): Long = bytes / (1024 * 1024)
}
