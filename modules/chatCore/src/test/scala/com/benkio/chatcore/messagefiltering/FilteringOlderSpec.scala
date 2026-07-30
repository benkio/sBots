package com.benkio.chatcore.messagefiltering

import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*

import java.time.Instant

class FilteringOlderSpec extends ScalaCheckSuite {

  property("filter accepts messages within olderThreshold and rejects older ones") {
    forAll(Gen.choose(0L, FilteringOlder.olderThreshold.toSeconds - 1)) { (ageSeconds: Long) =>
      val now     = Instant.now.getEpochSecond
      val within  = Message(0, now - ageSeconds, ChatId(0L), "test")
      val outside =
        Message(0, now - FilteringOlder.olderThreshold.toSeconds - 10, ChatId(0L), "test")
      assert(FilteringOlder.filter(within))
      assert(!FilteringOlder.filter(outside))
    }
  }
}
