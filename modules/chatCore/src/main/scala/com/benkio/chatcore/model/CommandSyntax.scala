package com.benkio.chatcore.model

import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.SBotInfo.SBotName

/** How a platform writes the commands and the file requests of a bot.
  *
  * @param prefix
  *   what a command message starts with, e.g. `/` on Telegram, `!` on IRC
  * @param mentionSeparator
  *   what, between the command and the name of the bot, addresses the command to a bot when more bots share the chat:
  *   `Some("@")` for `/command@BotName` on Telegram, `None` when the platform has no such thing
  * @param fileRequestMarker
  *   what, before the id of the bot, starts a file request: nothing on Telegram (`botid_file.mp3`), it can not be the
  *   prefix of the commands
  */
final case class CommandSyntax(
    prefix: String,
    mentionSeparator: Option[String] = None,
    fileRequestMarker: String = ""
) {
  require(prefix.nonEmpty, "the command prefix must not be empty")
  require(fileRequestMarker != prefix, "the file request marker must differ from the command prefix")

  /** The command as it is written in a message, without the mention: `/command`. */
  def invocation(command: String): String = prefix + command

  def isCommand(text: String): Boolean = text.startsWith(prefix)

  def isFileRequest(text: String, botId: SBotId): Boolean = text.startsWith(fileRequestMarker + botId.value)

  /** What the file request asks for, the name of the file: the text without the marker. */
  def fileRequestContent(text: String): String = text.stripPrefix(fileRequestMarker)

  private def mention(command: String, botName: SBotName): Option[String] =
    mentionSeparator.map(separator => invocation(command) + separator + botName.value)

  /** Whether the text is the command, alone, with an input or addressed to the bot: `/command`, `/command input`,
    * `/command@BotName`.
    */
  def isInvocationOf(text: String, command: String, botName: SBotName): Boolean =
    text.startsWith(invocation(command) + " ")
      || text == invocation(command)
      || mention(command, botName).exists(text.startsWith)

  /** Whether the first word of a message, the command typed by the user, is this command, addressed to the bot or not.
    */
  def isCommandWord(word: String, command: String, botName: SBotName): Boolean =
    word == invocation(command) || mention(command, botName).contains(word)

  /** The name of the command written in the input, without the prefix, the mention and the input: `/command@BotName
    * input` is `command`.
    */
  def commandName(input: String): String = {
    val word = input.trim.stripPrefix(prefix).takeWhile(!_.isWhitespace)
    mentionSeparator.fold(word)(separator =>
      word.indexOf(separator) match {
        case -1    => word
        case index => word.take(index)
      }
    )
  }
}

object CommandSyntax {

  /** `/command`, `/command@BotName`, and `botid_file.mp3` for the file requests. */
  val Telegram: CommandSyntax = CommandSyntax(prefix = "/", mentionSeparator = Some("@"))
}
