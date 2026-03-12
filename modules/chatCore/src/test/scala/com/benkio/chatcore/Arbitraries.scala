package com.benkio.chatcore

import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.Generators.commandKeyGen
import org.scalacheck.Arbitrary
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.Generators.mediaFileGen
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.Generators.replyValueGen

object Arbitraries {
  given Arbitrary[CommandKey] = Arbitrary(commandKeyGen)
  given Arbitrary[MediaFile] = Arbitrary(mediaFileGen)
  given Arbitrary[ReplyValue] = Arbitrary(replyValueGen)
}
