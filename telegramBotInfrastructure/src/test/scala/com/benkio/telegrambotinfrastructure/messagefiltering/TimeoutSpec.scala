package com.benkio.telegrambotinfrastructure.messagefiltering

import munit.FunSuite

import java.time.Instant
import scala.concurrent.duration._

class TimeoutSpec extends FunSuite {

  test("isWithinTimeout should return true when the input date is within the current instant - duration. false otherwise") {
    val validInputDate: Int = Instant.now().getEpochSecond().toInt - 10
    val invalidInputDate: Int = Instant.now().getEpochSecond().toInt - 30
    val inputTimeout   = Some(20.seconds)
    assert(Timeout.isWithinTimeout(validInputDate, inputTimeout))
    assert(!Timeout.isWithinTimeout(invalidInputDate, inputTimeout))

  }
}
