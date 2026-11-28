package com.benkio.chatcore.model

import com.benkio.chatcore.model.SBotInfo.SBotName

/** Someone named in the text of a message with a mention: by the platform id and/or by the name (Telegram username,
  * Discord username, IRC nick), whichever the platform gives. `text` is the exact visible span the platform marked as a
  * mention, when it gives offsets: the name can differ from what the reader sees (a Telegram text mention shows the
  * user's display name, not necessarily its username).
  */
final case class Mention(
    id: Option[String] = None,
    name: Option[String] = None,
    text: Option[String] = None
)

/** The message another one replies to.
  *
  * @param authorName
  *   the name the author goes by (Telegram username, Discord username, IRC nick): how the bot recognises its own
  *   messages
  */
final case class RepliedMessage(
    messageId: Option[MessageId] = None,
    authorId: Option[String] = None,
    authorName: Option[String] = None,
    text: Option[String] = None
)

final case class Message(
    messageId: Option[MessageId],
    date: Long,
    chatId: ChatId,
    chatType: String,
    text: Option[String] = None,
    caption: Option[String] = None,
    newChatMembers: List[User] = List.empty, // the joined members other than the bot itself
    botAdded: Boolean = false,               // the bot itself is among the joined members
    leftChatMember: Option[User] = None,
    isForward: Boolean = false,
    authorId: Option[String] =
      None, // who wrote it, when the platform says: the id of the user (Discord, Telegram) or the nick (IRC)
    mentions: List[Mention] = List.empty,   // who the text mentions
    replyTo: Option[RepliedMessage] = None, // the message this one replies to
    quote: Option[String] = None            // the part of the replied message that is quoted
) {

  /** Whether the message is addressed to the bot: it is mentioned, one of its messages is replied to (or quoted), or
    * the text opens by calling it (`botName: ...`, the IRC way). The match is on the name, case insensitive.
    */
  def isDirectedAt(botName: SBotName): Boolean = {
    val name                                   = Message.normaliseName(botName.value)
    def is(candidate: Option[String]): Boolean = candidate.map(Message.normaliseName).contains(name)
    mentions.exists(m => is(m.name)) ||
    replyTo.exists(r => is(r.authorName)) ||
    text.exists(Message.callsBy(name, _))
  }
}

object Message {

  private def normaliseName(name: String): String = name.trim.stripPrefix("@").toLowerCase

  private def callsBy(name: String, text: String): Boolean = {
    val lower = text.trim.toLowerCase.stripPrefix("@")
    lower.startsWith(name) && lower.drop(name.length).headOption.exists(c => c == ':' || c == ',')
  }
}
