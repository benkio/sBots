package com.benkio.chattelegramadapter.conversions

import com.benkio.chattelegramadapter.conversions.MessageConversions.toModelMessage
import com.benkio.chattelegramadapter.Arbitraries.given
import munit.FunSuite
import munit.ScalaCheckSuite
import org.scalacheck.Prop.*
import telegramium.bots.InaccessibleMessage
import telegramium.bots.MaybeInaccessibleMessage
import telegramium.bots.Message as TelegramMessage

class MessageConversionsSpec extends ScalaCheckSuite {
  test("toModelMessage should convert MaybeInaccessibleMessage to core Message when possible") {
    {
      forAll { (maybeInaccessibleMessage: MaybeInaccessibleMessage) =>
        val message = maybeInaccessibleMessage.toModelMessage
        assert(message.isDefined)
        assertEquals(
          message.get.messageId,
          maybeInaccessibleMessage match {
            case msg: TelegramMessage     => msg.messageId
            case msg: InaccessibleMessage => msg.messageId
          }
        )
      }
    }
  }
}
