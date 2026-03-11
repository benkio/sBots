package com.benkio.chatcore

import org.scalacheck.Arbitrary
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.Generators.commandKeyGen

object Arbitraries {
  given Arbitrary[CommandKey] = Arbitrary(commandKeyGen)
}
