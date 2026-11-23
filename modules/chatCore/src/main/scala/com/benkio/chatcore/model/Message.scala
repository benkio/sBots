package com.benkio.chatcore.model

import com.benkio.chatcore.model.SBotInfo.SBotName

import java.security.MessageDigest

/** Someone named in the text of a message with a mention: by the platform id and/or by the name (Telegram username,
  * Discord username, IRC nick), whichever the platform gives.
  */
final case class Mention(id: Option[String] = None, name: Option[String] = None)

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
    authorId: Option[String] = None,        // who wrote it, when the platform says (Discord): the id of the user
    mentions: List[Mention] = List.empty,   // who the text mentions
    replyTo: Option[RepliedMessage] = None, // the message this one replies to
    quote: Option[String] = None            // the part of the replied message that is quoted
) {

  /** The author id in a form that does not identify the user but is stable, so the same author can be told apart. */
  def anonymisedAuthor(salt: String): Option[String] =
    authorId.map { id =>
      MessageDigest
        .getInstance("SHA-256")
        .digest(s"$salt:$id".getBytes("UTF-8"))
        .take(8)
        .map("%02x".format(_))
        .mkString
    }

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
