package com.benkio.telegrambotinfrastructure.model

import munit.FunSuite

import java.time.Instant
import scala.concurrent.duration._

class TimeoutSpec extends FunSuite {

  test("isWithinTimeout should return true when the input date is within the current instant - duration") {
    val inputDate: Int = Instant.now().getEpochSecond().toInt - 10
    val inputTimeout   = Some(20.seconds)
    assert(Timeout.isWithinTimeout(inputDate, inputTimeout))
  }
}
