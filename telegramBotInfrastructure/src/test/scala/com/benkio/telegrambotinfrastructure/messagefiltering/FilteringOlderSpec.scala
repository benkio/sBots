package com.benkio.telegrambotinfrastructure.messagefiltering

//import java.time.temporal.{ChronoField, ChronoUnit}
import com.benkio.telegrambotinfrastructure.messagefiltering.FilteringOlder
import munit._
import telegramium.bots.{Chat, Message}

import java.time.Instant
import scala.concurrent.duration._

class FilteringOlderSpec extends FunSuite {
  val now = Instant.now
  val dateWithinThreshold : Long  = now.getEpochSecond() - FilteringOlder.olderThreshold.-(10.seconds).toSeconds
  val dateOutsideThreshold : Long = now.getEpochSecond() - FilteringOlder.olderThreshold.+(10.seconds).toSeconds

  test("FilteringOlder.filter should return true if the message date is within threshold") {
    val msg = Message(
      messageId = 0,
      date = dateWithinThreshold.toInt,
      chat = Chat(id = 0, `type` = "test")
    )
    assert(FilteringOlder.filter(msg))
  }
  test("FilteringOlder.filter should return false if the message date is outside the threshold") {
    val msg = Message(
      messageId = 0,
      date = dateOutsideThreshold.toInt,
      chat = Chat(id = 0, `type` = "test")
    )
    assert(!FilteringOlder.filter(msg))
  }
}
