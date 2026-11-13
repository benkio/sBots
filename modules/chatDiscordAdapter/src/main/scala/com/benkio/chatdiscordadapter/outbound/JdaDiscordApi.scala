package com.benkio.chatdiscordadapter.outbound

import cats.effect.Async
import cats.syntax.all.*
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.JDA

import java.nio.file.Path

/** [[DiscordApi]] with JDA. The channel has to be known to JDA: the channels of the servers the bot is in are, the DM
  * channels are once the bot has seen them (it always has, a reply follows a message).
  */
class JdaDiscordApi[F[_]: Async](jda: JDA) extends DiscordApi[F] {

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

  override def deleteMessage(channelId: String, messageId: String): F[Unit] =
    channel(channelId).flatMap(c =>
      Async[F].fromCompletableFuture(Async[F].delay(c.deleteMessageById(messageId).submit())).void
    )
}
