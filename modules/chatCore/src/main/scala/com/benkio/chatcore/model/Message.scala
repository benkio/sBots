package com.benkio.chatcore.model

import com.benkio.chatcore.model.SBotInfo.SBotName

import java.security.MessageDigest
import java.util.regex.Pattern

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

  /** The content of this message in the form the LLM context stores it: a stable anonymous author label, no real
    * usernames or nicks, and no embedded newline that could forge another labelled turn in a flat prompt.
    */
  def anonymisedContent(salt: String): String = {
    val label = anonymisedAuthor(salt).map(id => s"user-$id").getOrElse("user")
    s"$label: ${Message.singleLine(Message.redactMentions(text.orElse(caption).getOrElse(""), mentions))}"
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

  private val atMention      = "@[A-Za-z0-9_.-]+".r
  private val discordMention = "<@!?\\d+>".r
  private val anonymousUser  = "@user"

  /** Replaces every real name the adapters could recognise with a generic label. The visible spans come from the
    * platform's mention offsets and are removed literally first, since the displayed text need not match the name; the
    * names come from the same mention data; the two patterns also catch `@nick` text and Discord's raw `<@id>` mentions
    * when the adapter gives neither. Name matching is case insensitive: platform names are.
    */
  def redactMentions(content: String, mentions: List[Mention]): String = {
    val spans        = mentions.flatMap(_.text).map(_.trim).filter(_.nonEmpty).distinct
    val named        = mentions.flatMap(_.name).map(_.trim.stripPrefix("@")).filter(_.nonEmpty).distinct
    val withoutSpans = spans.foldLeft(content)(_.replace(_, anonymousUser))
    val withoutNames = named.foldLeft(withoutSpans) { (text, name) =>
      text.replaceAll(s"(?i)(?<!\\w)@?${Pattern.quote(name)}(?!\\w)", anonymousUser)
    }
    atMention.replaceAllIn(discordMention.replaceAllIn(withoutNames, anonymousUser), anonymousUser)
  }

  /** Characters that can start a new line in a prompt. Besides CR and LF, this includes the Unicode separators that
    * `String.linesIterator` does not split on, but a model's tokenizer may still treat as a line break.
    */
  private val lineTerminators: List[String] =
    List("\r", "\n", 0x85.toChar.toString, 0x2028.toChar.toString, 0x2029.toChar.toString)

  /** Keeps one message on one prompt line, so user text cannot introduce a `role: ` line of its own. Every terminator
    * becomes its visible escape (`\r` for CR, `\n` for the others), including the Unicode separators.
    */
  def singleLine(content: String): String =
    lineTerminators.foldLeft(content) { (text, terminator) =>
      text.replace(terminator, if terminator == "\r" then "\\r" else "\\n")
    }

  private def normaliseName(name: String): String = name.trim.stripPrefix("@").toLowerCase

  private def callsBy(name: String, text: String): Boolean = {
    val lower = text.trim.toLowerCase.stripPrefix("@")
    lower.startsWith(name) && lower.drop(name.length).headOption.exists(c => c == ':' || c == ',')
  }
}
