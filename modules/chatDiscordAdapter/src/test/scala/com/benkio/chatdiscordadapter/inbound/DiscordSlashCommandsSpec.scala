package com.benkio.chatdiscordadapter.inbound

import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.TextReply
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandTrigger
import munit.FunSuite

class DiscordSlashCommandsSpec extends FunSuite {

  private def command(name: String, instructions: CommandInstructionData): ReplyBundleCommand =
    ReplyBundleCommand(CommandTrigger(name), TextReply.fromList("reply")(true), instructions)

  private def withHelp(name: String, eng: String = "Does something"): ReplyBundleCommand =
    command(name, CommandInstructionData.Instructions("Fa qualcosa", eng))

  test("a command with instructions becomes a slash command described by them") {
    assertEquals(
      DiscordSlashCommands.specs(List(withHelp("hello", "Says hello\nand more"))),
      List(SlashCommandSpec("hello", "Says hello"))
    )
  }

  test("the commands without instructions are hidden, as from the help") {
    assertEquals(DiscordSlashCommands.specs(List(command("secret", CommandInstructionData.NoInstructions))), List.empty)
  }

  test("the names are lowercase, and the ones Discord does not accept are left out") {
    val result = DiscordSlashCommands.specs(
      List(withHelp("Hello"), withHelp("two words"), withHelp("a" * 33), withHelp("a-b_c"), withHelp("ok"))
    )
    assertEquals(result.map(_.name), List("hello", "a-b_c", "ok"))
  }

  test("a name is registered once") {
    assertEquals(DiscordSlashCommands.specs(List(withHelp("hello"), withHelp("HELLO"))).map(_.name), List("hello"))
  }

  test("at most 100 commands are registered") {
    val many = (1 to 150).toList.map(i => withHelp(s"c$i"))
    assertEquals(DiscordSlashCommands.specs(many).size, DiscordSlashCommands.maxCommands)
  }

  test("a long description is cut to the limit, an empty one is the name of the command") {
    val long = DiscordSlashCommands.description("hello", "x" * 150)
    assertEquals(long.length, DiscordSlashCommands.maxDescription)
    assert(long.endsWith("…"))
    assertEquals(DiscordSlashCommands.description("hello", "  \n "), "hello")
  }
}
