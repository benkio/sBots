package com.benkio.chatdiscordadapter.inbound

import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.Platform

/** A message written in a channel or in a DM, as the adapter needs it: no library types, so it is mapped by plain
  * functions.
  *
  * @param content
  *   the text; it is empty for what the bot is not allowed to read (the privileged message content intent is off)
  */
final case class DiscordMessageEvent(
    messageId: String,
    channelId: String,
    isDirect: Boolean,
    authorIsBot: Boolean,
    content: String,
    createdAtEpochSecond: Long
)

/** A slash command (`/name arguments`): it arrives as an interaction, not as a message.
  *
  * @param arguments
  *   what the user wrote in the free text option of the command
  */
final case class DiscordSlashCommandEvent(
    channelId: String,
    isDirect: Boolean,
    authorIsBot: Boolean,
    name: String,
    arguments: Option[String],
    createdAtEpochSecond: Long
)

/** From what Discord sends to the [[Message]] of the engine. */
object DiscordInbound {

  /** The name of the text option of the slash commands: what follows the command, like `/command arguments` on
    * Telegram.
    */
  val argumentsOption: String = "arguments"

  /** The message, unless it is to be ignored: written by a bot (the other bots and the bot itself), or without text
    * (only attachments, or a content the bot cannot read).
    */
  def message(event: DiscordMessageEvent): Option[Message] =
    Option.when(!event.authorIsBot && event.content.trim.nonEmpty)(
      Message(
        messageId = Some(MessageId(event.messageId)),
        date = event.createdAtEpochSecond,
        chatId = chatId(event.channelId),
        chatType = chatType(event.isDirect),
        text = Some(event.content)
      )
    )

  /** The command as the text of a message, written with the command syntax of the platform. It has no message id: the
    * interaction is not a message to reply to.
    */
  def slashCommand(event: DiscordSlashCommandEvent, syntax: CommandSyntax): Option[Message] =
    Option.when(!event.authorIsBot)(
      Message(
        messageId = None,
        date = event.createdAtEpochSecond,
        chatId = chatId(event.channelId),
        chatType = chatType(event.isDirect),
        text = Some((syntax.invocation(event.name) +: event.arguments.map(_.trim).filter(_.nonEmpty).toList).mkString(" "))
      )
    )

  private def chatId(channelId: String): ChatId = ChatId(Platform.Discord, channelId)

  /** Like Telegram's: `private` for the DMs, `group` for the channels of a server. */
  private def chatType(isDirect: Boolean): String = if isDirect then "private" else "group"
}
