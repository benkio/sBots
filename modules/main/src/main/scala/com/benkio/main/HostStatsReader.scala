package com.benkio.main

import com.benkio.chatcore.UsageStats.HostStats

import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean
import java.nio.file.Files
import java.nio.file.Path
import scala.jdk.CollectionConverters.*
import scala.util.Try

/** Reads the machine-level values of the `[stats] type=system` line. On Linux `/proc` gives `MemAvailable` and all three
  * load averages; elsewhere the JDK MX bean supplies what it can. Unknown values stay at the `-1` sentinel so the line
  * always has the same keys and the Grafana queries keep matching.
  */
object HostStatsReader {

  private val MiB       = 1024 * 1024
  private val memInfo   = Path.of("/proc/meminfo")
  private val loadAvg   = Path.of("/proc/loadavg")

  def read(): HostStats = {
    val mem  = readLines(memInfo).flatMap(parseMemInfo)
    val load = readLines(loadAvg).flatMap(lines => lines.headOption.flatMap(parseLoadAvg))
    val bean = ManagementFactory.getOperatingSystemMXBean

    HostStats(
      memTotalMb     = mem.map(_._1).getOrElse(-1L),
      memAvailableMb = mem.map(_._2).getOrElse(freeMemoryMb(bean)),
      load1          = load.map(_._1).orElse(oneMinuteLoad(bean)).getOrElse(-1.0),
      load5          = load.map(_._2).getOrElse(-1.0),
      load15         = load.map(_._3).getOrElse(-1.0),
      cpuUsagePct    = cpuLoadPct(bean)
    )
  }

  private def readLines(path: Path): Option[List[String]] =
    Try(Files.readAllLines(path).asScala.toList).toOption

  private[main] def parseMemInfo(lines: Seq[String]): Option[(Long, Long)] =
    for {
      total     <- valueKb(lines, "MemTotal")
      available <- valueKb(lines, "MemAvailable")
    } yield total -> available

  private def valueKb(lines: Seq[String], key: String): Option[Long] =
    lines
      .collectFirst { case line if line.startsWith(s"$key:") => Try(line.split("\\s+")(1).toLong / 1024).toOption }
      .flatten

  private[main] def parseLoadAvg(line: String): Option[(Double, Double, Double)] = {
    val fields = line.trim.split("\\s+")
    if fields.length >= 3 then
      Try((fields(0).toDouble, fields(1).toDouble, fields(2).toDouble)).toOption
    else None
  }

  private def oneMinuteLoad(bean: OperatingSystemMXBean): Option[Double] =
    Try(Option(bean.getSystemLoadAverage)).toOption.flatten.filter(_ >= 0)

  private def freeMemoryMb(bean: OperatingSystemMXBean): Long = bean match {
    case b: com.sun.management.OperatingSystemMXBean =>
      Try(b.getFreeMemorySize / MiB).getOrElse(-1L)
    case _ => -1L
  }

  private def cpuLoadPct(bean: OperatingSystemMXBean): Int = bean match {
    case b: com.sun.management.OperatingSystemMXBean =>
      Try(b.getCpuLoad)
        .toOption
        .filter(load => load >= 0 && load <= 1)
        .map(load => (load * 100).round.toInt)
        .getOrElse(-1)
    case _ => -1
  }
}
