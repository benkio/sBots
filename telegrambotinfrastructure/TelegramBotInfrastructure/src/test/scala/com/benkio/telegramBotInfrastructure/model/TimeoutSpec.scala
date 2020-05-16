package com.benkio.telegramBotInfrastructure.model

import java.time.Instant
import scala.concurrent.duration._
import org.scalatest._

class TimeoutSpec extends WordSpec with Matchers {

  "isWithinTimeout" should {
    "return true" when {
      "the input date is within the current instant - duration" in {
        val inputDate: Int = Instant.now().getEpochSecond().toInt - 10
        val inputTimeout = Some(20 seconds)
        Timeout.isWithinTimeout(inputDate, inputTimeout) shouldBe true
      }
    }
  }
}
