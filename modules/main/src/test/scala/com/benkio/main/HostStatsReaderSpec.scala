package com.benkio.main

import munit.FunSuite

class HostStatsReaderSpec extends FunSuite {

  test("parseMemInfo reads total, available and free memory in MB") {
    val lines = List(
      "MemTotal:        1024000 kB",
      "MemFree:          102400 kB",
      "MemAvailable:     512000 kB",
      "SwapTotal:             0 kB"
    )
    assertEquals(HostStatsReader.parseMemInfo(lines), Some((1000L, 500L, 100L)))
  }

  test("parseMemInfo returns None when MemAvailable is missing") {
    assertEquals(HostStatsReader.parseMemInfo(List("MemTotal: 1024000 kB", "MemFree: 100000 kB")), None)
  }

  test("parseMemInfo returns None when MemFree is missing") {
    assertEquals(HostStatsReader.parseMemInfo(List("MemTotal: 1024000 kB", "MemAvailable: 512000 kB")), None)
  }

  test("parseStatus reads VmRSS in MB") {
    val lines = List("Name:\tjava", "VmRSS:\t  307200 kB", "VmSize:\t 4000000 kB")
    assertEquals(HostStatsReader.parseStatus(lines), Some(300L))
  }

  test("parseStatus returns None when VmRSS is missing") {
    assertEquals(HostStatsReader.parseStatus(List("Name:\tjava")), None)
  }

  test("parseLoadAvg reads the three load averages and ignores the rest of the line") {
    assertEquals(
      HostStatsReader.parseLoadAvg(" 0.42 0.31 0.20 2/100 1234"),
      Some((0.42, 0.31, 0.20))
    )
  }

  test("parseLoadAvg rejects a malformed line") {
    assertEquals(HostStatsReader.parseLoadAvg("0.42"), None)
  }
}
