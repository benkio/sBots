package com.benkio.chattelegramadapter.model

import com.benkio.chattelegramadapter.Arbitraries.given
import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import telegramium.bots.InaccessibleMessage
import telegramium.bots.MaybeInaccessibleMessage
import telegramium.bots.Message as TelegramMessage

class TelegramMessageIdsSpec extends ScalaCheckSuite {

  property("getIds extracts chat id, message id and chat type from both message variants") {
    forAll { (msg: MaybeInaccessibleMessage) =>
      val result = TelegramMessageIds.getIds(msg)
      msg match {
        case m: TelegramMessage =>
          assertEquals(result.chatId, m.chat.id)
          assertEquals(result.messageId, m.messageId)
          assertEquals(result.chatType, m.chat.`type`)
        case m: InaccessibleMessage =>
          assertEquals(result.chatId, m.chat.id)
          assertEquals(result.messageId, m.messageId)
          assertEquals(result.chatType, m.chat.`type`)
      }
    }
  }
}
