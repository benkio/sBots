package com.benkio.chatcore.messagefiltering

import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.LeftMemberTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageLengthTrigger
import com.benkio.chatcore.model.NewMemberTrigger
import com.benkio.chatcore.model.TextTrigger
import com.benkio.chatcore.model.TextTriggerValue
import com.benkio.chatcore.model.Trigger
import io.circe.*

enum MessageMatches {
  case ContainsOnce extends MessageMatches
  case ContainsAll  extends MessageMatches
}

object MessageMatches {

  given Decoder[MessageMatches] =
    Decoder.decodeString.emap(s =>
      s match {
        case "ContainsOnce" => Right(ContainsOnce)
        case "ContainsAll"  => Right(ContainsAll)
        case _              => Left(s"$s not recognized when decoding `MessageMatches`")
      }
    )
  given Encoder[MessageMatches] = Encoder[MessageMatches](using mm => Json.fromString(mm.toString))

  def doesMatch(
      replyBundleMessage: ReplyBundleMessage,
      message: Message,
      ignoreMessagePrefix: Option[String]
  ): Option[(Trigger, ReplyBundleMessage)] =
    (
      ignoreMessagePrefix,
      replyBundleMessage.matcher,
      replyBundleMessage.trigger,
      message.text.orElse(message.caption)
    ) match {
      case (Some(prefix), _, _, Some(messageText)) if messageText.startsWith(prefix)                           => None
      case (_, _, MessageLengthTrigger(messageLength), Some(messageText)) if messageText.size >= messageLength =>
        Some((MessageLengthTrigger(messageLength), replyBundleMessage))
      case (_, _, _: NewMemberTrigger.type, _) if message.newChatMembers.nonEmpty =>
        Some((NewMemberTrigger, replyBundleMessage))
      case (_, _, _: LeftMemberTrigger.type, _) if message.leftChatMember.nonEmpty =>
        Some((LeftMemberTrigger, replyBundleMessage))
      case (_, ContainsOnce, TextTrigger(triggers*), Some(messageText)) =>
        triggers
          .sorted(using TextTriggerValue.orderingInstance.reverse)
          .find(TextTriggerValue.matchValue(_, messageText.toLowerCase()))
          .map(t => (TextTrigger(t), replyBundleMessage))
      case (_, ContainsAll, TextTrigger(triggers*), Some(messageText))
          if triggers.forall(TextTriggerValue.matchValue(_, messageText.toLowerCase())) =>
        Some((TextTrigger(triggers.sorted(using TextTriggerValue.orderingInstance.reverse)*), replyBundleMessage))
      case _ => None
    }
}
