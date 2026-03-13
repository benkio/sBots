package com.benkio.chattelegramadapter

import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chattelegramadapter.Generators.maybeInaccessibleMessageGen
import com.benkio.chattelegramadapter.Generators.telegramReplyValueGen
import org.scalacheck.Arbitrary
import telegramium.bots.MaybeInaccessibleMessage

object Arbitraries {
  given Arbitrary[ReplyValue]               = Arbitrary(telegramReplyValueGen)
  given Arbitrary[MaybeInaccessibleMessage] = Arbitrary(maybeInaccessibleMessageGen)
}
