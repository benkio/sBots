package com.benkio.chattelegramadapter

import com.benkio.chatcore.model.reply.PagedListAction
import org.scalacheck.Arbitrary
import telegramium.bots.MaybeInaccessibleMessage

trait Arbitraries extends Generators with com.benkio.chatcore.Arbitraries {
  given Arbitrary[PagedListAction]          = Arbitrary(callbackDataGen)
  given Arbitrary[MaybeInaccessibleMessage] = Arbitrary(maybeInaccessibleMessageGen)
}

object Arbitraries extends Arbitraries
