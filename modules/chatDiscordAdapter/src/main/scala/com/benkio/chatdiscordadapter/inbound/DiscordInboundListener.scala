package com.benkio.chatdiscordadapter.inbound

import cats.effect.std.Dispatcher
import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Mention
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageId
import com.benkio.chatcore.model.RepliedMessage
import com.benkio.chatdiscordadapter.outbound.DiscordButtonPress
import log.effect.LogWriter
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

import scala.jdk.CollectionConverters.*

/** The bot joined a server: the channels it can write to and the number of members of the server, bots included. */
final case class DiscordGuildJoin(channelIds: List[String], memberCount: Int)

/** Hands to `onMessage` what the bot receives, mapped by [[DiscordInbound]]. A failure of `onMessage` is logged: it
  * must not reach the library. It is a listener of [[com.benkio.chatdiscordadapter.gateway.DiscordGateway]].
  *
  * The slash command is acknowledged (Discord shows an error if the interaction gets no answer in 3 seconds) and the
  * acknowledgement is removed at once: the replies are normal messages of the channel.
  */
class DiscordInboundListener[F[_]: Async: LogWriter](
    dispatcher: Dispatcher[F],
    syntax: CommandSyntax,
    onMessage: Message => F[Unit],
    onGuildJoin: DiscordGuildJoin => F[Unit],
    onButton: DiscordButtonPress => F[Either[String, F[Unit]]]
) extends ListenerAdapter {

  override def onMessageReceived(event: MessageReceivedEvent): Unit =
    deliver(
      DiscordInbound.message(
        DiscordMessageEvent(
          messageId = event.getMessage.getId,
          channelId = event.getChannel.getId,
          isDirect = !event.isFromGuild,
          authorIsBot = event.getAuthor.isBot,
          authorId = event.getAuthor.getId,
          content = event.getMessage.getContentRaw,
          createdAtEpochSecond = event.getMessage.getTimeCreated.toEpochSecond,
          mentions = event.getMessage.getMentions.getUsers.asScala.toList
            .map(u => Mention(id = Some(u.getId), name = Some(u.getName))),
          replyTo = Option(event.getMessage.getReferencedMessage).map(r =>
            RepliedMessage(
              messageId = Some(MessageId(r.getId)),
              authorId = Some(r.getAuthor.getId),
              authorName = Some(r.getAuthor.getName),
              text = Some(r.getContentRaw).filter(_.nonEmpty)
            )
          )
        )
      )
    )

  /** The timeouts are per channel, so the join is handed over with every channel the bot can write to. */
  override def onGuildJoin(event: GuildJoinEvent): Unit =
    dispatcher.unsafeRunAndForget(
      onGuildJoin(
        DiscordGuildJoin(
          channelIds = event.getGuild.getTextChannels.asScala.filter(_.canTalk).map(_.getId).toList,
          memberCount = event.getGuild.getMemberCount
        )
      ).handleErrorWith(e => LogWriter.error(s"[DiscordInbound] Error while handling the guild join: $e"))
    )

  override def onSlashCommandInteraction(event: SlashCommandInteractionEvent): Unit = {
    event.deferReply(true).queue(hook => { val _ = hook.deleteOriginal().queue() })
    deliver(
      DiscordInbound.slashCommand(
        DiscordSlashCommandEvent(
          channelId = event.getChannel.getId,
          isDirect = !event.isFromGuild,
          authorIsBot = event.getUser.isBot,
          authorId = event.getUser.getId,
          name = event.getName,
          arguments = Option(event.getOption(DiscordInbound.argumentsOption)).map(_.getAsString),
          createdAtEpochSecond = event.getTimeCreated.toEpochSecond
        ),
        syntax
      )
    )
  }

  /** A press is decided at once: not accepted, only who pressed sees why; accepted, the interaction is acknowledged
    * (the message is edited later) and the work goes on in the background.
    */
  override def onButtonInteraction(event: ButtonInteractionEvent): Unit = {
    val press = DiscordButtonPress(
      channelId = event.getChannel.getId,
      messageId = event.getMessageId,
      userId = event.getUser.getId,
      id = event.getComponentId
    )
    dispatcher.unsafeRunSync(
      onButton(press).handleErrorWith(e =>
        LogWriter.error(s"[DiscordInbound] Error while deciding the button $press: $e") >>
          Async[F].pure(Left("Something went wrong: try again."))
      )
    ) match {
      case Left(text)  => event.reply(text).setEphemeral(true).queue()
      case Right(work) =>
        event.deferEdit().queue()
        dispatcher.unsafeRunAndForget(
          work.handleErrorWith(e => LogWriter.error(s"[DiscordInbound] Error while handling the button $press: $e"))
        )
    }
  }

  private def deliver(message: Option[Message]): Unit =
    message.foreach(m =>
      dispatcher.unsafeRunAndForget(
        onMessage(m).handleErrorWith(e => LogWriter.error(s"[DiscordInbound] Error while handling $m: $e"))
      )
    )
}
