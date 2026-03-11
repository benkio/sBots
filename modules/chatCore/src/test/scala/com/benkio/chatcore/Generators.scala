package com.benkio.chatcore

import org.scalacheck.Gen
import com.benkio.chatcore.model.CommandKey

object Generators {

  val commandKeyGen: Gen[CommandKey] = Gen.oneOf(CommandKey.values.toSeq)
}
