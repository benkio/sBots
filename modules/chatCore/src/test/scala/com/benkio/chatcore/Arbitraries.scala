package com.benkio.chatcore

import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Trigger
import com.benkio.chatcore.model.User
import com.benkio.chatcore.Generators.commandKeyGen
import com.benkio.chatcore.Generators.coreReplyValueGen
import com.benkio.chatcore.Generators.mediaFileGen
import com.benkio.chatcore.Generators.messageGen
import com.benkio.chatcore.Generators.triggerGen
import com.benkio.chatcore.Generators.userGen
import org.scalacheck.Arbitrary

object Arbitraries {
  given Arbitrary[CommandKey] = Arbitrary(commandKeyGen)
  given Arbitrary[MediaFile]  = Arbitrary(mediaFileGen)
  given Arbitrary[ReplyValue] = Arbitrary(coreReplyValueGen)
  given Arbitrary[Message]    = Arbitrary(messageGen)
  given Arbitrary[User]       = Arbitrary(userGen)
  given Arbitrary[Trigger]    = Arbitrary(triggerGen)
}
