package com.benkio.chattelegramadapter

import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chattelegramadapter.Generators.telegramReplyValueGen
import org.scalacheck.Arbitrary

object Arbitraries {
  given Arbitrary[ReplyValue] = Arbitrary(telegramReplyValueGen)
}
