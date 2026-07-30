package com.benkio.chattelegramadapter

import com.benkio.chattelegramadapter.model.CallbackData
import org.scalacheck.Arbitrary
import telegramium.bots.MaybeInaccessibleMessage

trait Arbitraries extends Generators with com.benkio.chatcore.Arbitraries {
  given Arbitrary[CallbackData]             = Arbitrary(callbackDataGen)
  given Arbitrary[MaybeInaccessibleMessage] = Arbitrary(maybeInaccessibleMessageGen)
}

object Arbitraries extends Arbitraries
