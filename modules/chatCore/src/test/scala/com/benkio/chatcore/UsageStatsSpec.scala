package com.benkio.chatcore

import com.benkio.chatcore.UsageStats.Outcome
import munit.FunSuite

import scala.concurrent.duration.*

class UsageStatsSpec extends FunSuite {

  test("messageLine has the bot, the platform, the outcome and the latency in milliseconds") {
    assertEquals(
      UsageStats.messageLine("CalandroBot", "telegram", Outcome.ReactiveReplied, 125.millis),
      "[stats] type=message bot=CalandroBot platform=telegram outcome=reactive-replied latency_ms=125"
    )
  }

  test("every outcome has its own label") {
    assertEquals(
      Outcome.values.map(_.label).toList,
      List("reactive-replied", "duplicate", "timeout", "failed", "llm-replied", "unreplied")
    )
  }

  private val host = UsageStats.HostStats(
    memTotalMb = 4096,
    memAvailableMb = 1024,
    memFreeMb = 512,
    procRssMb = 300,
    load1 = 0.5,
    load5 = 0.25,
    load15 = 0.125,
    cpuUsagePct = 7,
    diskTotalMb = 50000,
    diskFreeMb = 12000
  )

  test("systemLine has the heap, the DB size, the machine memory and the load") {
    assertEquals(
      UsageStats.systemLine(
        heapUsedBytes = 100L * 1024 * 1024,
        heapMaxBytes = 256L * 1024 * 1024,
        uptime = 90.seconds,
        dbSizeBytes = Some(42L * 1024 * 1024),
        host = host
      ),
      "[stats] type=system heap_used_mb=100 heap_max_mb=256 uptime_s=90 db_mb=42 " +
        "mem_total_mb=4096 mem_available_mb=1024 load1=0.50 load5=0.25 load15=0.13 cpu_usage_pct=7 mem_free_mb=512 proc_rss_mb=300 " +
        "disk_total_mb=50000 disk_free_mb=12000"
    )
  }

  test("systemLine reports -1 when the DB file does not exist") {
    val line = UsageStats.systemLine(0L, 0L, 0.seconds, None, host)
    assert(line.contains(" db_mb=-1 "), line)
  }

  test("systemLine keeps every key when a machine value is unavailable") {
    assertEquals(
      UsageStats.systemLine(0L, 0L, 0.seconds, None).split(" ").length,
      16
    )
    assert(
      UsageStats
        .systemLine(0L, 0L, 0.seconds, None)
        .endsWith(
          "mem_total_mb=-1 mem_available_mb=-1 load1=-1.00 load5=-1.00 load15=-1.00 cpu_usage_pct=-1 mem_free_mb=-1 proc_rss_mb=-1 " +
            "disk_total_mb=-1 disk_free_mb=-1"
        )
    )
  }
}
