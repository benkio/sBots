package com.benkio.chatcore.messagefiltering

import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import munit.*

import java.time.Instant
import scala.concurrent.duration.*

class FilteringOlderSpec extends FunSuite {
  val now                        = Instant.now
  val dateWithinThreshold: Long  = now.getEpochSecond() - FilteringOlder.olderThreshold.-(10.seconds).toSeconds
  val dateOutsideThreshold: Long = now.getEpochSecond() - FilteringOlder.olderThreshold.+(10.seconds).toSeconds

  test("FilteringOlder.filter should return true if the message date is within threshold") {
    val msg = Message(
      messageId = 0,
      date = dateWithinThreshold,
      chatId = ChatId(0L),
      chatType = "test"
    )
    assert(FilteringOlder.filter(msg))
  }
  test("FilteringOlder.filter should return false if the message date is outside the threshold") {
    val msg = Message(
      messageId = 0,
      date = dateOutsideThreshold,
      chatId = ChatId(0L),
      chatType = "test"
    )
    assert(!FilteringOlder.filter(msg))
  }
}
