package com.benkio.telegrambotinfrastructure.messagefiltering

import com.benkio.telegrambotinfrastructure.model.ChatId
import com.benkio.telegrambotinfrastructure.model.Message
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
      chatType = "test",
      text = None,
      caption = None,
      newChatMembers = List.empty,
      leftChatMember = None,
      forwardOrigin = None
    )
    assert(FilteringOlder.filter(msg))
  }
  test("FilteringOlder.filter should return false if the message date is outside the threshold") {
    val msg = Message(
      messageId = 0,
      date = dateOutsideThreshold,
      chatId = ChatId(0L),
      chatType = "test",
      text = None,
      caption = None,
      newChatMembers = List.empty,
      leftChatMember = None,
      forwardOrigin = None
    )
    assert(!FilteringOlder.filter(msg))
  }
}
