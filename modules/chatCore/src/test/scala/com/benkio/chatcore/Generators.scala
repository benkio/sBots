package com.benkio.chatcore

import com.benkio.chatcore.model.CommandKey
import org.scalacheck.Gen

object Generators {

  val commandKeyGen: Gen[CommandKey] = Gen.oneOf(CommandKey.values.toSeq)
}
