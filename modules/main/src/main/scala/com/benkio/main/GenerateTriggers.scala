package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.repository.JsonDataRepository
import com.benkio.main.Logger.given
import log.effect.LogWriter

import java.nio.file.Files
import java.nio.file.Path
import scala.jdk.CollectionConverters.*

object GenerateTriggers extends IOApp {

  // Trigger file generation ////////////////////////////////////////////////////

  def generateTriggerFile(
      botModuleRelativeFolderPath: String,
      triggerFilename: String,
      triggers: List[ReplyBundleMessage]
  ): Resource[IO, Unit] = {
    val triggerFilesPath = Path.of(botModuleRelativeFolderPath + s"/$triggerFilename")

    for {
      _ <- Resource.eval(IO.println(s"[GenerateTriggers] Generate $botModuleRelativeFolderPath Trigger file"))
      triggersStringList = triggers.map(s => s.prettyPrint().stripLineEnd)
      _ <- Resource.eval(IO.println(s"[GenerateTriggers] Generate $botModuleRelativeFolderPath done"))
      _ = Files.write(triggerFilesPath, triggersStringList.asJava)
    } yield ()
  }

  def run(args: List[String]): IO[ExitCode] = {
    val jsonDataRepository: JsonDataRepository[IO] =
      JsonDataRepository[IO]()(using IO.asyncForIO, summon[LogWriter[IO]])
    BotRegistry
      .toList(BotRegistry.value)
      .traverse_((botRegistryEntry: com.benkio.main.BotRegistryEntry[IO]) =>
        for {
          botData <- Resource.eval(
            jsonDataRepository.loadData[ReplyBundleMessage](botRegistryEntry.sBotConfig.repliesJsonFilename)
          )
          _ <- generateTriggerFile(
            botModuleRelativeFolderPath = s"../bots/${botRegistryEntry.sBotInfo.botName.value}/",
            triggerFilename = botRegistryEntry.sBotConfig.triggerFilename,
            triggers = botData
          )
        } yield ()
      )
      .as(ExitCode.Success)
      .use(_.pure)
  }

}
