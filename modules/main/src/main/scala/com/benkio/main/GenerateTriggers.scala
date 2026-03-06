package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.implicits.*
import com.benkio.main.Logger.given
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.DBLayerMock
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.repository.JsonDataRepository
import com.benkio.telegrambotinfrastructure.repository.ResourcesRepository
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.implicits.*
import org.http4s.HttpApp
import org.http4s.Response
import org.http4s.Status
import telegramium.bots.high.Api

import java.nio.file.Files
import java.nio.file.Path
import scala.jdk.CollectionConverters.*

object GenerateTriggers extends IOApp {

  private def forTriggerGeneration(sBotConfig: SBotConfig)(using log: LogWriter[IO]): IO[BotSetup[IO]] = {
    val repository = ResourcesRepository.fromResources[IO]()
    val stubClient  = Client.fromHttpApp(HttpApp[IO](_ => IO.pure(Response[IO](Status.Ok))))
    val dbLayer     = DBLayerMock.mock(sBotConfig.sBotInfo.botId)
    BackgroundJobManager[IO](
      dbLayer = dbLayer,
      sBotInfo = sBotConfig.sBotInfo,
      ttl = sBotConfig.messageTimeToLive
    )(using IO.asyncForIO, summon[Api[IO]], log).map { bjm =>
      BotSetup(
        token = "trigger-generation",
        httpClient = stubClient,
        repository = repository,
        jsonDataRepository = JsonDataRepository[IO]()(using IO.asyncForIO, log),
        dbLayer = dbLayer,
        backgroundJobManager = bjm,
        api = summon[Api[IO]],
        webhookUri = uri"https://localhost",
        webhookPath = uri"/",
        sBotConfig = sBotConfig
      )
    }
  }

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
    BotRegistry
      .toList(BotRegistry.value)
      .traverse_((botRegistryEntry: com.benkio.main.BotRegistryEntry[IO]) =>
        for {
          botSetup <- Resource.eval(forTriggerGeneration(botRegistryEntry.sBotConfig))
          botData  <- Resource.eval(
            botSetup.jsonDataRepository.loadData[ReplyBundleMessage](botRegistryEntry.sBotConfig.repliesJsonFilename)
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
