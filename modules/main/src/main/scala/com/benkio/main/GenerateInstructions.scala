package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.implicits.*
import com.benkio.chatcore.model.reply.ReplyBundleCommand
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.patterns.CommandPatterns.InstructionsCommand
import com.benkio.chatcore.repository.JsonDataRepository
import com.benkio.main.Logger.given
import log.effect.LogWriter

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

object GenerateInstructions extends IOApp {

  def render(
      sBotConfig: com.benkio.chatcore.config.SBotConfig,
      messageReplies: List[ReplyBundleMessage],
      commandReplies: List[ReplyBundleCommand]
  ): String = {
    val commands = InstructionsCommand.describableCommands(
      sBotInfo = sBotConfig.sBotInfo,
      triggerListUri = sBotConfig.triggerListUri,
      ignoreMessagePrefix = sBotConfig.ignoreMessagePrefix,
      messageRepliesData = messageReplies,
      commandRepliesData = commandReplies
    )
    InstructionsCommand.instructionsFileContent(
      sBotInfo = sBotConfig.sBotInfo,
      ignoreMessagePrefix = sBotConfig.ignoreMessagePrefix,
      commands = commands
    )
  }

  def writeInstructionsFile(path: Path, content: String): IO[Unit] =
    IO.blocking {
      Option(path.getParent).foreach(Files.createDirectories(_))
      Files.writeString(path, content, StandardCharsets.UTF_8)
      ()
    }

  def generateForBot(
      jsonDataRepository: JsonDataRepository[IO],
      botRegistryEntry: BotRegistryEntry[IO],
      check: Boolean
  ): IO[Unit] = {
    val sBotConfig = botRegistryEntry.sBotConfig
    for {
      messageReplies <- jsonDataRepository.loadData[ReplyBundleMessage](sBotConfig.repliesJsonFilename)
      commandReplies <- jsonDataRepository.loadData[ReplyBundleCommand](sBotConfig.commandsJsonFilename)
      content = render(sBotConfig = sBotConfig, messageReplies = messageReplies, commandReplies = commandReplies)
      path    = sBotConfig.instructionsFilePath
      _ <-
        if check then IO
          .blocking(if Files.exists(path) then Files.readString(path, StandardCharsets.UTF_8) else "")
          .flatMap { existing =>
            IO.raiseUnless(existing == content)(
              new RuntimeException(s"[GenerateInstructions] Out of date: $path. Run generateInstructions to update it.")
            ) *> IO.println(s"[GenerateInstructions] Up to date: $path")
          }
        else
          IO.println(s"[GenerateInstructions] Generate instructions file: $path") *>
            writeInstructionsFile(path, content) *>
            IO.println(s"[GenerateInstructions] Generate $path done")
    } yield ()
  }

  def run(args: List[String]): IO[ExitCode] = {
    val check              = args.exists(a => a == "--check" || a == "check")
    val jsonDataRepository = JsonDataRepository[IO]()(using IO.asyncForIO, summon[LogWriter[IO]])
    BotRegistry
      .toList(BotRegistry.value)
      .traverse_(entry =>
        generateForBot(jsonDataRepository = jsonDataRepository, botRegistryEntry = entry, check = check)
      )
      .as(ExitCode.Success)
  }
}
