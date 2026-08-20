package com.benkio.chattelegramadapter.http.telegramreply.callbackreply

import com.benkio.chattelegramadapter.model.TelegramMessageIds
import com.benkio.chattelegramadapter.Arbitraries.given
import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import telegramium.bots.MaybeInaccessibleMessage

class ModelMessageFromCallbackSpec extends ScalaCheckSuite {

  property("build copies ids and chat type from TelegramMessageIds") {
    forAll { (msg: MaybeInaccessibleMessage) =>
      val ids    = TelegramMessageIds.getIds(msg)
      val result = ModelMessageFromCallback.build(msg)
      assertEquals(result.messageId, ids.messageId)
      assertEquals(result.chatId.value, ids.chatId)
      assertEquals(result.chatType, ids.chatType)
      assertEquals(result.date, 0L)
    }
  }
}
