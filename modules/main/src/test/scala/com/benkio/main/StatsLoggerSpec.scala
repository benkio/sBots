package com.benkio.main

import cats.effect.IO
import munit.CatsEffectSuite

import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

class StatsLoggerSpec extends CatsEffectSuite {

  test("systemLine logs the heap and the uptime, and no DB size when the file is missing") {
    val missing = Path.of(s"missing-${UUID.randomUUID()}.sqlite3")

    StatsLogger.systemLine[IO](missing.toString).map { line =>
      assert(line.startsWith("[stats] type=system heap_used_mb="), line)
      assert(line.contains(" heap_max_mb="), line)
      assert(line.contains(" uptime_s="), line)
      assert(line.contains(" db_mb=-1 "), line)
      assert(line.contains(" mem_total_mb="), line)
      assert(line.contains(" mem_available_mb="), line)
      assert(line.contains(" load1="), line)
      assert(line.contains(" load5="), line)
      assert(line.contains(" load15="), line)
      assert(line.contains(" cpu_usage_pct="), line)
    }
  }

  test("systemLine reports the size of the DB file in MB") {
    val file = Files.createTempFile("sbots-stats", ".sqlite3")
    Files.write(file, Array.fill[Byte](1024 * 1024)(0))

    StatsLogger
      .systemLine[IO](file.toString)
      .map(line => assert(line.contains(" db_mb=1 "), line))
      .guarantee(IO.blocking(Files.deleteIfExists(file)).void)
  }
}
