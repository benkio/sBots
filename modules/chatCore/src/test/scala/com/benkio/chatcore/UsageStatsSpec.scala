package com.benkio.chatcore

import com.benkio.chatcore.UsageStats.Outcome
import munit.FunSuite

import scala.concurrent.duration.*

class UsageStatsSpec extends FunSuite {

  test("messageLine has the bot, the platform, the outcome and the latency in milliseconds") {
    assertEquals(
      UsageStats.messageLine("CalandroBot", "telegram", Outcome.Handled, 125.millis),
      "[stats] type=message bot=CalandroBot platform=telegram outcome=handled latency_ms=125"
    )
  }

  test("every outcome has its own label") {
    assertEquals(Outcome.values.map(_.label).toList, List("handled", "duplicate", "timeout", "failed"))
  }

  test("systemLine has the heap and the DB size in megabytes and the uptime in seconds") {
    assertEquals(
      UsageStats.systemLine(
        heapUsedBytes = 100L * 1024 * 1024,
        heapMaxBytes = 256L * 1024 * 1024,
        uptime = 90.seconds,
        dbSizeBytes = Some(42L * 1024 * 1024)
      ),
      "[stats] type=system heap_used_mb=100 heap_max_mb=256 uptime_s=90 db_mb=42"
    )
  }

  test("systemLine reports -1 when the DB file does not exist") {
    assert(UsageStats.systemLine(0L, 0L, 0.seconds, None).endsWith("db_mb=-1"))
  }
}
