package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import com.benkio.chattelegramadapter.Arbitraries.given
import com.benkio.chattelegramadapter.model.TelegramMessageIds
import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import telegramium.bots.MaybeInaccessibleMessage

class MediaSpec extends ScalaCheckSuite {

  property("modelMessageFromCallback copies ids and chat type from TelegramMessageIds") {
    forAll { (msg: MaybeInaccessibleMessage) =>
      val ids    = TelegramMessageIds.getIds(msg)
      val result = Media.modelMessageFromCallback(msg)
      assertEquals(result.messageId, ids.messageId)
      assertEquals(result.chatId.value, ids.chatId)
      assertEquals(result.chatType, ids.chatType)
      assertEquals(result.date, 0L)
    }
  }
}
