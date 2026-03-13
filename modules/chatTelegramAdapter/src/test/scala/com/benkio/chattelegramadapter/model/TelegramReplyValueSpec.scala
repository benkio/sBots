package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chattelegramadapter.Arbitraries.given
import munit.ScalaCheckSuite
import org.scalacheck.Prop.*

class TelegramReplyValueSpec extends ScalaCheckSuite {
  test("TelegramInlineKeyboard.from should downcast ReplyValue to TelegramInlineKeyboard when possible") {
    forAll { (replyValue: ReplyValue) =>
      replyValue match {
        case telegramInlineKeyboard: TelegramInlineKeyboard =>
          assertEquals(TelegramInlineKeyboard.from(replyValue), Some(telegramInlineKeyboard))
        case _ => assertEquals(TelegramInlineKeyboard.from(replyValue), None)
      }
    }
  }
}
