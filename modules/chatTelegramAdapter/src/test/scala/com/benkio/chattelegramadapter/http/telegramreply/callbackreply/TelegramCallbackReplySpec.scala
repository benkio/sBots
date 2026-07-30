package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*

class TelegramCallbackReplySpec extends ScalaCheckSuite {

  property("nextPage increments the current page") {
    forAll(Gen.choose(-10, 100)) { (page: Int) =>
      assertEquals(TelegramCallbackReply.nextPage(page), page + 1)
    }
  }

  property("previousPage decrements and floors at zero") {
    forAll(Gen.choose(-10, 100)) { (page: Int) =>
      assertEquals(TelegramCallbackReply.previousPage(page), (page - 1).max(0))
    }
  }
}
