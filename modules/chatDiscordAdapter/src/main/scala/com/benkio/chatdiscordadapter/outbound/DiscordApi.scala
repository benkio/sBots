package com.benkio.chatdiscordadapter.outbound

import java.nio.file.Path

/** The message a stored prompt points at no longer exists on Discord. Editing it can never succeed again, so the caller
  * has to replace the handle with a newly sent prompt.
  */
final class DiscordUnknownMessage(val messageId: String)
    extends RuntimeException(s"[DiscordApi] The message $messageId does not exist anymore")

/** What the adapter does on Discord, with the ids as the strings they are in a [[com.benkio.chatcore.model.ChatId]] and
  * a [[com.benkio.chatcore.model.MessageId]]: the replies are written against it, and [[JdaDiscordApi]] is the real
  * one.
  */
trait DiscordApi[F[_]] {

  /** Sends a text, as a reply to the message if there is one. Gives the id of the message sent. */
  def sendText(channelId: String, text: String, replyToMessageId: Option[String]): F[String]

  /** Uploads a file, as a reply to the message if there is one. Gives the id of the message sent. */
  def sendFile(channelId: String, fileName: String, file: Path, replyToMessageId: Option[String]): F[String]

  def deleteMessage(channelId: String, messageId: String): F[Unit]

  /** Sends a text with rows of buttons, as a reply to the message if there is one. Gives the id of the message sent. */
  def sendButtons(channelId: String, message: DiscordButtonMessage, replyToMessageId: Option[String]): F[String]

  /** Replaces the text and the buttons of a message. */
  def editButtons(channelId: String, messageId: String, message: DiscordButtonMessage): F[Unit]
}
