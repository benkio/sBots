package com.benkio.chatdiscordadapter.inbound

import cats.effect.std.Dispatcher
import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import log.effect.LogWriter
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/** Hands to `onMessage` what the bot receives, mapped by [[DiscordInbound]]. A failure of `onMessage` is logged: it
  * must not reach the library. It is a listener of [[com.benkio.chatdiscordadapter.gateway.DiscordGateway]].
  *
  * The slash command is acknowledged (Discord shows an error if the interaction gets no answer in 3 seconds) and the
  * acknowledgement is removed at once: the replies are normal messages of the channel.
  */
class DiscordInboundListener[F[_]: Async: LogWriter](
    dispatcher: Dispatcher[F],
    syntax: CommandSyntax,
    onMessage: Message => F[Unit]
) extends ListenerAdapter {

  override def onMessageReceived(event: MessageReceivedEvent): Unit =
    deliver(
      DiscordInbound.message(
        DiscordMessageEvent(
          messageId = event.getMessage.getId,
          channelId = event.getChannel.getId,
          isDirect = !event.isFromGuild,
          authorIsBot = event.getAuthor.isBot,
          content = event.getMessage.getContentRaw,
          createdAtEpochSecond = event.getMessage.getTimeCreated.toEpochSecond
        )
      )
    )

  override def onSlashCommandInteraction(event: SlashCommandInteractionEvent): Unit = {
    event.deferReply(true).queue(hook => { val _ = hook.deleteOriginal().queue() })
    deliver(
      DiscordInbound.slashCommand(
        DiscordSlashCommandEvent(
          channelId = event.getChannel.getId,
          isDirect = !event.isFromGuild,
          authorIsBot = event.getUser.isBot,
          name = event.getName,
          arguments = Option(event.getOption(DiscordInbound.argumentsOption)).map(_.getAsString),
          createdAtEpochSecond = event.getTimeCreated.toEpochSecond
        ),
        syntax
      )
    )
  }

  private def deliver(message: Option[Message]): Unit =
    message.foreach(m =>
      dispatcher.unsafeRunAndForget(
        onMessage(m).handleErrorWith(e => LogWriter.error(s"[DiscordInbound] Error while handling $m: $e"))
      )
    )
}
