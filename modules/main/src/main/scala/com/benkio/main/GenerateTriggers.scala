package com.benkio.main

import cats.data.NonEmptyList
import cats.effect.Async
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.implicits.*
import com.benkio.ABarberoBot.ABarberoBot
import com.benkio.CalandroBot.CalandroBot
import com.benkio.m0sconibot.M0sconiBot
import com.benkio.richardphjbensonbot.RichardPHJBensonBot
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.show.ShowQuery
import com.benkio.telegrambotinfrastructure.model.show.SimpleShowQuery
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.repository.db.*
import com.benkio.telegrambotinfrastructure.repository.JsonDataRepository
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.repository.Repository.RepositoryError
import com.benkio.telegrambotinfrastructure.repository.ResourcesRepository
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.youtuboanchei0bot.YouTuboAncheI0Bot
import io.circe.syntax.*
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.implicits.*
import org.http4s.HttpApp
import org.http4s.Response
import org.http4s.Status
import telegramium.bots.client.Method
import telegramium.bots.high.Api

import java.io.*
import java.util.UUID

object GenerateTriggers extends IOApp {

  /** Stubs used only for trigger generation. Never used at runtime for message handling. */
  private object TriggerGenerationStubs {

    def stubRepository[F[_]: Async]: Repository[F] =
      new Repository[F] {
        override def getResourceFile(
            mediaFile: MediaFile
        ): Resource[F, Either[RepositoryError, NonEmptyList[MediaResource[F]]]] =
          Resource.eval(Async[F].pure(Left(RepositoryError.NoResourcesFoundFile(mediaFile))))
        override def getResourcesByKind(
            criteria: String,
            botId: SBotId
        ): Resource[F, Either[RepositoryError, NonEmptyList[NonEmptyList[MediaResource[F]]]]] =
          Resource.eval(Async[F].pure(Left(RepositoryError.NoResourcesFoundKind(criteria, botId))))
      }

    def stubClient[F[_]: Async]: Client[F] =
      Client.fromHttpApp(HttpApp[F](_ => Async[F].pure(Response[F](Status.Ok))))

    def stubApi[F[_]: Async]: Api[F] =
      new Api[F] {
        override def execute[Res](method: Method[Res]): F[Res] =
          Async[F].raiseError(
            new UnsupportedOperationException(
              s"TriggerGenerationStubs.stubApi: execute(${method.payload.name}) not supported"
            )
          )
      }

    private def unsupported[F[_]: Async, A](name: String): F[A] =
      Async[F].raiseError(new UnsupportedOperationException(s"TriggerGenerationStubs: $name not supported"))

    def stubDBSubscription[F[_]: Async]: DBSubscription[F] =
      new DBSubscription[F] {
        override def getSubscriptions(botId: SBotId, chatId: Option[Long] = None): F[List[DBSubscriptionData]] =
          Async[F].pure(List.empty)
        override def getSubscription(id: String): F[Option[DBSubscriptionData]] =
          Async[F].pure(None)
        override def getRandomSubscription(): F[Option[DBSubscriptionData]] =
          Async[F].pure(None)
        override def insertSubscription(subscription: DBSubscriptionData): F[Unit] =
          unsupported("insertSubscription")
        override def deleteSubscription(subscriptionId: UUID): F[Unit] =
          unsupported("deleteSubscription")
        override def deleteSubscriptions(chatId: Long): F[Unit] =
          unsupported("deleteSubscriptions")
      }

    def stubDBTimeout[F[_]: Async](): DBTimeout[F] =
      new DBTimeout[F] {
        override def getOrDefault(chatId: Long, botId: SBotId): F[DBTimeoutData] =
          unsupported("getOrDefault")
        override def setTimeout(timeout: DBTimeoutData): F[Unit] =
          unsupported("setTimeout")
        override def removeTimeout(chatId: Long, botId: SBotId): F[Unit] =
          unsupported("removeTimeout")
        override def logLastInteraction(chatId: Long, botId: SBotId): F[Unit] =
          unsupported("logLastInteraction")
      }

    def stubDBMedia[F[_]: Async]: DBMedia[F] =
      new DBMedia[F] {
        override def getMedia(filename: String, cache: Boolean = true): F[Option[DBMediaData]] =
          Async[F].pure(None)
        override def getRandomMedia(botId: SBotId): F[Option[DBMediaData]] =
          Async[F].pure(None)
        override def getMediaByKind(kind: String, botId: SBotId, cache: Boolean = true): F[List[DBMediaData]] =
          Async[F].pure(List.empty)
        override def getMediaByMediaCount(limit: Int = 20, botId: Option[SBotId] = None): F[List[DBMediaData]] =
          Async[F].pure(List.empty)
        override def incrementMediaCount(filename: String): F[Unit] =
          unsupported("incrementMediaCount")
        override def decrementMediaCount(filename: String): F[Unit] =
          unsupported("decrementMediaCount")
        override def insertMedia(dbMediaData: DBMediaData): F[Unit] =
          unsupported("insertMedia")
      }

    def stubDBLog[F[_]: Async]: DBLog[F] =
      new DBLog[F] {
        override def writeLog(logMessage: String): F[Unit] =
          Async[F].unit
        override def getLastLog(): F[Option[DBLogData]] =
          Async[F].pure(None)
      }

    def stubDBShow[F[_]: Async]: DBShow[F] =
      new DBShow[F] {
        override def getShows(botId: SBotId): F[List[DBShowData]] =
          Async[F].pure(List.empty)
        override def getRandomShow(botId: SBotId): F[Option[DBShowData]] =
          Async[F].pure(None)
        override def getShowByShowQuery(query: ShowQuery, botId: SBotId): F[List[DBShowData]] =
          Async[F].pure(List.empty)
        override def getShowBySimpleShowQuery(query: SimpleShowQuery, botId: SBotId): F[List[DBShowData]] =
          Async[F].pure(List.empty)
        override def insertShow(dbShowData: DBShowData): F[Unit] =
          unsupported("insertShow")
        override def deleteShow(dbShowData: DBShowData): F[Unit] =
          unsupported("deleteShow")
      }

    def stubDBLayer[F[_]: Async](): DBLayer[F] =
      DBLayer(
        dbTimeout = stubDBTimeout[F](),
        dbMedia = stubDBMedia[F],
        dbSubscription = stubDBSubscription[F],
        dbLog = stubDBLog[F],
        dbShow = stubDBShow[F]
      )
  }

  private def forTriggerGeneration(sBotConfig: SBotConfig)(using log: LogWriter[IO]): IO[BotSetup[IO]] = {
    val resourcesRepository = ResourcesRepository.fromResources[IO]()
    val client              = TriggerGenerationStubs.stubClient[IO]
    val api                 = TriggerGenerationStubs.stubApi[IO]
    val dbLayer             = TriggerGenerationStubs.stubDBLayer[IO]()
    BackgroundJobManager[IO](
      dbLayer = dbLayer,
      sBotInfo = sBotConfig.sBotInfo,
      ttl = sBotConfig.messageTimeToLive
    )(using IO.asyncForIO, api, log).map { bjm =>
      BotSetup(
        token = "trigger-generation",
        httpClient = client,
        repository = resourcesRepository,
        jsonDataRepository = JsonDataRepository[IO](),
        dbLayer = dbLayer,
        backgroundJobManager = bjm,
        api = api,
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
    val triggerFilesPath = new File(botModuleRelativeFolderPath).getCanonicalPath + s"/$triggerFilename"

    for {
      _ <- Resource.eval(IO.println(s"[GenerateTriggers] Generate $botModuleRelativeFolderPath Trigger file"))
      triggersStringList = triggers.map(_.prettyPrint())
      _  <- Resource.eval(IO.println(s"[GenerateTriggers] Generate $botModuleRelativeFolderPath done"))
      pw <- Resource.fromAutoCloseable(IO(new PrintWriter(triggerFilesPath)))
    } yield pw.write(triggersStringList.mkString(""))
  }

  def generateTriggersJsonFile(
      botModuleRelativeFolderPath: String,
      commandsJsonFilename: String,
      commands: List[ReplyBundleCommand]
  ): Resource[IO, Unit] = {
    val commandFilesPath = new File(botModuleRelativeFolderPath).getCanonicalPath + s"/$commandsJsonFilename"

    for {
      _ <- Resource.eval(IO.println(s"[GenerateTriggers] Generate $botModuleRelativeFolderPath JSON command file"))
      commandsJson = commands.asJson
      _  <- Resource.eval(IO.println(s"[GenerateTriggers] Generate $botModuleRelativeFolderPath done"))
      pw <- Resource.fromAutoCloseable(IO(new PrintWriter(commandFilesPath)))
    } yield pw.write(commandsJson.spaces2)
  }

  def run(args: List[String]): IO[ExitCode] = {
    given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)
    (for {
      calandroSetup <- Resource.eval(forTriggerGeneration(CalandroBot.sBotConfig)(using log))
      calandroData  <- Resource.eval(
        calandroSetup.jsonDataRepository.loadData[ReplyBundleMessage](CalandroBot.sBotConfig.repliesJsonFilename)
      )
      _ <- generateTriggerFile(
        botModuleRelativeFolderPath = "../bots/CalandroBot/",
        triggerFilename = CalandroBot.sBotConfig.triggerFilename,
        triggers = calandroData
      )
      //     _ <- generateTriggersJsonFile(
      //       botModuleRelativeFolderPath = "../bots/CalandroBot/src/main/resources",
      //     commandsJsonFilename = CalandroBot.sBotConfig.commandsJsonFilename,
      //     commands = CalandroBot.commandRepliesData
      // )

      aBarberoSetup <- Resource.eval(forTriggerGeneration(ABarberoBot.sBotConfig)(using log))
      aBarberoData  <- Resource.eval(
        aBarberoSetup.jsonDataRepository.loadData[ReplyBundleMessage](ABarberoBot.sBotConfig.repliesJsonFilename)
      )
      _ <- generateTriggerFile(
        botModuleRelativeFolderPath = "../bots/ABarberoBot/",
        triggerFilename = ABarberoBot.sBotConfig.triggerFilename,
        triggers = aBarberoData
      )
      m0sconiSetup <- Resource.eval(forTriggerGeneration(M0sconiBot.sBotConfig)(using log))
      m0sconiData  <- Resource.eval(
        m0sconiSetup.jsonDataRepository.loadData[ReplyBundleMessage](M0sconiBot.sBotConfig.repliesJsonFilename)
      )
      _ <- generateTriggerFile(
        botModuleRelativeFolderPath = "../bots/m0sconiBot/",
        triggerFilename = M0sconiBot.sBotConfig.triggerFilename,
        triggers = m0sconiData
      )
      richardSetup <- Resource.eval(forTriggerGeneration(RichardPHJBensonBot.sBotConfig)(using log))
      richardData  <- Resource.eval(
        richardSetup.jsonDataRepository
          .loadData[ReplyBundleMessage](RichardPHJBensonBot.sBotConfig.repliesJsonFilename)
      )
      _ <- generateTriggerFile(
        botModuleRelativeFolderPath = "../bots/richardPHJBensonBot/",
        triggerFilename = RichardPHJBensonBot.sBotConfig.triggerFilename,
        triggers = richardData
      )
      youTuboSetup <- Resource.eval(forTriggerGeneration(YouTuboAncheI0Bot.sBotConfig)(using log))
      youTuboData  <- Resource.eval(
        youTuboSetup.jsonDataRepository
          .loadData[ReplyBundleMessage](YouTuboAncheI0Bot.sBotConfig.repliesJsonFilename)
      )
      _ <- generateTriggerFile(
        botModuleRelativeFolderPath = "../bots/youTuboAncheI0Bot/",
        triggerFilename = YouTuboAncheI0Bot.sBotConfig.triggerFilename,
        triggers = youTuboData
      )
    } yield ExitCode.Success).use(_.pure)
  }

}
