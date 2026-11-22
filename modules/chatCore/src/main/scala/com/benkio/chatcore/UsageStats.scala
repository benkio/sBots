package com.benkio.chatcore

import scala.concurrent.duration.FiniteDuration

/** The lines of the usage and health stats. They are logged at `info`, in the `logfmt` style, with the `[stats]` tag:
  * in Loki they are read with `{app="sbots"} | json | line_format "{{.message}}" | logfmt | type="message"`, see
  * `docs/monitoring.md`. Keep the keys stable: the dashboard depends on them. New keys are appended at the end.
  */
object UsageStats {

  /** Machine-level values for [[systemLine]]. `-1` means the value could not be read on this machine. */
  final case class HostStats(
      memTotalMb: Long = -1,
      memAvailableMb: Long = -1,
      load1: Double = -1,
      load5: Double = -1,
      load15: Double = -1,
      cpuUsagePct: Int = -1
  )

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

  def systemLine(
      heapUsedBytes: Long,
      heapMaxBytes: Long,
      uptime: FiniteDuration,
      dbSizeBytes: Option[Long],
      host: HostStats = HostStats()
  ): String =
    s"[stats] type=system heap_used_mb=${toMb(heapUsedBytes)} heap_max_mb=${toMb(heapMaxBytes)} " +
      s"uptime_s=${uptime.toSeconds} db_mb=${dbSizeBytes.fold(-1L)(toMb)} " +
      s"mem_total_mb=${host.memTotalMb} mem_available_mb=${host.memAvailableMb} " +
      s"load1=${fixed(host.load1)} load5=${fixed(host.load5)} load15=${fixed(host.load15)} " +
      s"cpu_usage_pct=${host.cpuUsagePct}"

  private def toMb(bytes: Long): Long = bytes / (1024 * 1024)

  private def fixed(value: Double): String =
    if !java.lang.Double.isFinite(value) then "-1.00"
    else java.math.BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString
}
