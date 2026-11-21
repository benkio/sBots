package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.implicits.*
import com.benkio.chatcore.dataentry.DataEntry
import com.benkio.chatcore.dataentry.TriggerFileWriter
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.repository.JsonDataRepository
import com.benkio.chatcore.Logger.given
import com.benkio.main.BotRegistryEntry
import log.effect.LogWriter

import java.nio.file.Path

object GenerateTriggers extends IOApp {

  // Trigger file generation ////////////////////////////////////////////////////

  def generateTriggerFile(
      triggerFilePath: Path,
      triggers: List[ReplyBundleMessage]
  ): Resource[IO, Unit] = {
    for {
      _ <- Resource.eval(IO.println(s"[GenerateTriggers] Generate trigger file: $triggerFilePath"))
      _ <- Resource.eval(TriggerFileWriter.write(triggerFilePath, triggers))
      _ <- Resource.eval(IO.println(s"[GenerateTriggers] Generate $triggerFilePath done"))
    } yield ()
  }

  def formatJsonsByEmptyDataEntry(botRegistryEntry: BotRegistryEntry[IO]): IO[Unit] =
    DataEntry.dataEntryLogic(input = List.empty, sBotConfig = botRegistryEntry.sBotConfig)

  def run(args: List[String]): IO[ExitCode] = {
    val jsonDataRepository: JsonDataRepository[IO] =
      JsonDataRepository[IO]()(using IO.asyncForIO, summon[LogWriter[IO]])
    BotRegistry
      .toList(BotRegistry.value)
      .traverse_((botRegistryEntry: BotRegistryEntry[IO]) =>
        for {
          botData <- Resource.eval(
            jsonDataRepository.loadData[ReplyBundleMessage](botRegistryEntry.sBotConfig.repliesJsonFilename)
          )
          _ <- generateTriggerFile(
            triggerFilePath = botRegistryEntry.sBotConfig.triggersFilePath,
            triggers = botData
          )
          _ <- Resource.eval(formatJsonsByEmptyDataEntry(botRegistryEntry))
        } yield ()
      )
      .as(ExitCode.Success)
      .use(_.pure)
  }

}
