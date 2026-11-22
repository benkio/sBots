package com.benkio.chatdiscordadapter.outbound

import cats.effect.Async
import cats.syntax.all.*
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.JDA

import java.nio.file.Path
import java.util.concurrent.CompletionException
import scala.jdk.CollectionConverters.*

/** [[DiscordApi]] with JDA. The channel has to be known to JDA: the channels of the servers the bot is in are, the DM
  * channels are once the bot has seen them (it always has, a reply follows a message).
  */
class JdaDiscordApi[F[_]: Async](jda: JDA) extends DiscordApi[F] {

  /** JDA reports a deleted message as the permanent UNKNOWN_MESSAGE error, sometimes wrapped by the future. */
  private def isUnknownMessage(error: Throwable): Boolean =
    error match {
      case e: ErrorResponseException                    => e.getErrorResponse == ErrorResponse.UNKNOWN_MESSAGE
      case e: CompletionException if e.getCause != null => isUnknownMessage(e.getCause)
      case _                                            => false
    }

  private def channel(channelId: String): F[MessageChannel] =
    Async[F].fromOption(
      Option(jda.getChannelById(classOf[MessageChannel], channelId)),
      new NoSuchElementException(s"[JdaDiscordApi] The channel $channelId is not known to JDA")
    )

  override def sendText(channelId: String, text: String, replyToMessageId: Option[String]): F[String] =
    channel(channelId).flatMap(c =>
      Async[F]
        .fromCompletableFuture(
          Async[F].delay(
            replyToMessageId.fold(c.sendMessage(text))(c.sendMessage(text).setMessageReference(_)).submit()
          )
        )
        .map(_.getId)
    )

  override def sendFile(
      channelId: String,
      fileName: String,
      file: Path,
      replyToMessageId: Option[String]
  ): F[String] =
    channel(channelId).flatMap(c =>
      Async[F]
        .fromCompletableFuture(
          Async[F].delay {
            val action = c.sendFiles(FileUpload.fromData(file.toFile, fileName))
            replyToMessageId.fold(action)(action.setMessageReference(_)).submit()
          }
        )
        .map(_.getId)
    )

  private def rows(message: DiscordButtonMessage): java.util.List[ActionRow] =
    message.rows
      .map(row => ActionRow.of(row.map(b => Button.secondary(b.id, b.label).withDisabled(b.disabled)).asJava))
      .asJava

  override def sendButtons(
      channelId: String,
      message: DiscordButtonMessage,
      replyToMessageId: Option[String]
  ): F[String] =
    channel(channelId).flatMap(c =>
      Async[F]
        .fromCompletableFuture(
          Async[F].delay {
            val action = c.sendMessage(message.text).setComponents(rows(message))
            replyToMessageId.fold(action)(action.setMessageReference(_)).submit()
          }
        )
        .map(_.getId)
    )

  override def editButtons(channelId: String, messageId: String, message: DiscordButtonMessage): F[Unit] =
    channel(channelId).flatMap(c =>
      Async[F]
        .fromCompletableFuture(
          Async[F].delay(c.editMessageById(messageId, message.text).setComponents(rows(message)).submit())
        )
        .void
        .adaptError {
          case e if isUnknownMessage(e) => new DiscordUnknownMessage(messageId)
        }
    )

  override def deleteMessage(channelId: String, messageId: String): F[Unit] =
    channel(channelId).flatMap(c =>
      Async[F].fromCompletableFuture(Async[F].delay(c.deleteMessageById(messageId).submit())).void
    )
}
