package com.benkio.chatcore

import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.Generators.commandKeyGen
import org.scalacheck.Arbitrary

object Arbitraries {
  given Arbitrary[CommandKey] = Arbitrary(commandKeyGen)
}
