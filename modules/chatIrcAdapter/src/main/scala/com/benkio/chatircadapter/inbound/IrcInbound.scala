package com.benkio.chatircadapter.inbound

import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Platform

/** A message written to a channel or privately to the bot, as the adapter needs it: no library types, so it is mapped by
  * plain functions.
  *
  * @param channel
  *   the channel it was written to, none for a private message
  */
final case class IrcMessageEvent(
    sender: String,
    channel: Option[String],
    text: String,
    createdAtEpochSecond: Long
)

/** From what IRC sends to the [[Message]] of the engine. */
object IrcInbound {

  private val colors      = "\u0003(\\d{1,2}(,\\d{1,2})?)?".r
  private val hexColors   = "\u0004([0-9A-Fa-f]{6}(,[0-9A-Fa-f]{6})?)?".r
  private val styleCodes  = "[\u0002\u000f\u0011\u0016\u001d\u001e\u001f]".r

  /** The text without the formatting codes of IRC (colors, bold, italic, underline, ...): the bot reads what is written. */
  def stripFormatting(text: String): String =
    styleCodes.replaceAllIn(hexColors.replaceAllIn(colors.replaceAllIn(text, ""), ""), "")

  /** The message, unless it is to be ignored: written by the bot itself (a server can echo it), or without text.
    *
    * The chat is the channel, or the sender for a private message: a nick or a channel is the same whatever the case, so
    * the ids are lowercase and the same chat is always the same id. IRC has no message ids, nor a time for the messages:
    * the one of the event is used.
    */
  def message(event: IrcMessageEvent, ownNick: String): Option[Message] = {
    val text = stripFormatting(event.text).trim
    Option.when(!event.sender.equalsIgnoreCase(ownNick) && text.nonEmpty)(
      Message(
        messageId = None,
        date = event.createdAtEpochSecond,
        chatId = ChatId(Platform.Irc, event.channel.getOrElse(event.sender).toLowerCase),
        chatType = if event.channel.isDefined then "group" else "private",
        text = Some(text)
      )
    )
  }
}
