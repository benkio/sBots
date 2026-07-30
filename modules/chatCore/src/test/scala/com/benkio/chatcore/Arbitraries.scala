package com.benkio.chatcore

import com.benkio.chatcore.messagefiltering.MessageMatches
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.reply.ReplyValueCore
import com.benkio.chatcore.model.show.Show
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MimeType
import com.benkio.chatcore.model.RegexTextTriggerValue
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTriggerValue
import com.benkio.chatcore.model.Trigger
import com.benkio.chatcore.model.User
import org.scalacheck.Arbitrary

import scala.concurrent.duration.FiniteDuration

trait Arbitraries extends Generators {
  given Arbitrary[CommandKey]             = Arbitrary(commandKeyGen)
  given Arbitrary[MediaFile]              = Arbitrary(mediaFileGen)
  given Arbitrary[ReplyValueCore]         = Arbitrary(coreReplyValueCoreGen)
  given Arbitrary[ReplyValue]             = Arbitrary(coreReplyValueGen)
  given Arbitrary[Message]                = Arbitrary(messageGen)
  given Arbitrary[User]                   = Arbitrary(userGen)
  given Arbitrary[Trigger]                = Arbitrary(triggerGen)
  given Arbitrary[FiniteDuration]         = Arbitrary(youtubeTimestampFiniteDurationGen)
  given Arbitrary[Show]                   = Arbitrary(showGen)
  given Arbitrary[MessageMatches]         = Arbitrary(messageMatchesGen)
  given Arbitrary[ReplyBundleMessage]     = Arbitrary(replyBundleMessageGen)
  given Arbitrary[MimeType]               = Arbitrary(mimeTypeGen)
  given Arbitrary[SBotId]                 = Arbitrary(sBotIdGen)
  given Arbitrary[StringTextTriggerValue] = Arbitrary(stringTextTriggerValueGen)
  given Arbitrary[RegexTextTriggerValue]  = Arbitrary(regexTextTriggerValueGen)
  given Arbitrary[TextTriggerValue]       = Arbitrary(triggerValueGen)
}

object Arbitraries extends Arbitraries
