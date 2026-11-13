package com.benkio.chatdiscordadapter.inbound

import cats.effect.Async
import cats.syntax.all.*
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.toEng
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.JDA

import scala.jdk.CollectionConverters.*

/** A slash command of a bot, as it is registered with Discord. */
final case class SlashCommandSpec(name: String, description: String)

/** The slash commands of a bot: one per command that has instructions (the ones without are hidden from the help, so
  * they are from Discord too), each with a free text option for what follows the command, see
  * [[DiscordInbound.argumentsOption]].
  */
object DiscordSlashCommands {

  /** What Discord accepts: at most 100 commands, a lowercase name of 1 to 32 letters, digits, `-` and `_`, a description
    * of 1 to 100 characters.
    */
  val maxCommands: Int       = 100
  val maxDescription: Int    = 100
  private val validName      = "^[-_\\p{L}\\p{N}]{1,32}$".r
  private val argumentsLabel = "What follows the command"

  def specs(commands: List[ReplyBundleCommand]): List[SlashCommandSpec] =
    commands
      .flatMap(command => command.instruction.toEng.map(instructions => (command.trigger.command, instructions)))
      .map((name, instructions) => SlashCommandSpec(name.toLowerCase, description(name, instructions)))
      .filter(spec => validName.matches(spec.name))
      .distinctBy(_.name)
      .take(maxCommands)

  /** The first line of the instructions, cut to the limit; the name of the command if there is nothing else. */
  def description(name: String, instructions: String): String = {
    val firstLine = instructions.linesIterator.map(_.trim).find(_.nonEmpty).getOrElse(name)
    if firstLine.length <= maxDescription then firstLine else firstLine.take(maxDescription - 1) + "…"
  }

  /** Replaces the commands of the bot on Discord with these. They are global: Discord can take up to an hour to show
    * the changes.
    */
  def register[F[_]: Async](jda: JDA, specs: List[SlashCommandSpec]): F[Unit] =
    Async[F]
      .fromCompletableFuture(
        Async[F].delay(
          jda
            .updateCommands()
            .addCommands(
              specs
                .map(spec =>
                  Commands
                    .slash(spec.name, spec.description)
                    .addOption(OptionType.STRING, DiscordInbound.argumentsOption, argumentsLabel, false)
                )
                .asJava
            )
            .submit()
        )
      )
      .void
}
