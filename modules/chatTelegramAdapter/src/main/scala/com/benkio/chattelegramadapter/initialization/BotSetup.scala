package com.benkio.chattelegramadapter.initialization

import cats.effect.std.Random
import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import cats.MonadThrow
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.initialization.BotCoreSetup
import com.benkio.chatcore.initialization.DBConfig
import com.benkio.chatcore.llm.LlmDependencies
import com.benkio.chatcore.model.media.MediaResource.MediaResourceFile
import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.JsonDataRepository
import com.benkio.chatcore.repository.Repository
import com.benkio.chatcore.repository.ResourcesRepository
import com.benkio.chatcore.BackgroundJobManager
import com.benkio.chatcore.LlmEngine
import com.benkio.chatcore.Notifier
import com.benkio.chattelegramadapter.http.TelegramNotifier
import com.benkio.chattelegramadapter.model.TelegramToken
import com.benkio.chattelegramadapter.TelegramBackgroundJobManager
import log.effect.LogWriter
import org.http4s.*
import org.http4s.client.Client
import telegramium.bots.high.Api
import telegramium.bots.high.BotApi

import java.nio.file.Path
import scala.concurrent.duration.*

final case class BotSetup[F[_]](
    token: TelegramToken,
    httpClient: Client[F],
    repository: Repository[F],
    jsonDataRepository: JsonDataRepository[F],
    dbLayer: DBLayer[F],
    backgroundJobManager: BackgroundJobManager[F],
    api: Api[F],
    notifier: Notifier[F],
    webhookUri: Uri,
    webhookPath: Uri,
    sBotConfig: SBotConfig,
    opsChatId: Long,
    llmEngine: LlmEngine[F]
)

object BotSetup {

  /** The same error of a bot is notified once in this window, followed by how many more there were. */
  private val NotifierWindow: FiniteDuration = 5.minutes

  enum BotSetupError(msg: String) extends Throwable(msg) {
    case TokenNotFound(tokenFilename: String) extends BotSetupError(s"[BotSetup] Cannot find the token $tokenFilename")
    case TokenIsEmpty(tokenFilename: String)
        extends BotSetupError(s"[BotSetup] the retrieved token $tokenFilename is empty")
  }

  def deleteWebhooks[F[_]: Async](
      httpClient: Client[F],
      token: TelegramToken
  ): Resource[F, Response[F]] = for {
    uri <- Resource.eval(
      MonadThrow[F].fromEither(Uri.fromString(s"https://api.telegram.org/bot${token.value}/setWebhook?url="))
    )
    deleteWebhookRequest: Request[F] =
      Request[F](
        method = Method.POST,
        uri = uri
      )
    response <- httpClient.run(deleteWebhookRequest)
  } yield response

  def token[F[_]: Async: LogWriter](
      tokenFilename: String,
      repository: Repository[F]
  ): Resource[F, TelegramToken] = {
    for {
      _                   <- Resource.eval(LogWriter.info(s"[BotSetup:58:47] Retrieving Token $tokenFilename"))
      tokenMediaResources <- repository.getResourceFile(Document(tokenFilename))
      tokenFiles          <- tokenMediaResources
        .map(_.collect { case MediaResourceFile(rf) =>
          rf
        }.sequence)
        .getOrElse(Resource.eval[F, List[Path]](Async[F].raiseError(BotSetupError.TokenNotFound(tokenFilename))))
      tokenFileContent <-
        Resource
          .eval(Async[F].fromOption(tokenFiles.headOption, BotSetupError.TokenNotFound(tokenFilename)))
          .flatMap(Repository.fileToString)
      _ <- Resource.eval(
        Async[F].raiseWhen(tokenFileContent.isEmpty)(
          BotSetupError.TokenIsEmpty(tokenFilename)
        )
      )
      _ <- Resource.eval(
        LogWriter.info(s"[BotSetup:58:47] Token $tokenFilename successfully retrieved")
      )
    } yield TelegramToken(tokenFileContent)
  }

  def loadDB[F[_]: Async](config: DBConfig, botId: com.benkio.chatcore.model.SBotInfo.SBotId)(using
      log: LogWriter[F]
  ): Resource[F, DBLayer[F]] =
    BotCoreSetup.loadDB[F](config, botId)

  def apply[F[_]: Async: Random](
      httpClient: Client[F],
      sBotConfig: SBotConfig,
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      opsChatId: Long,
      llmDependencies: Option[LlmDependencies[F]] = None
  )(using log: LogWriter[F]): Resource[F, BotSetup[F]] = {

    val resourceRepository = ResourcesRepository.fromResources[F]()
    for {
      tk   <- token[F](sBotConfig.token, resourceRepository)
      core <- BotCoreSetup[F](httpClient, sBotConfig, llmDependencies)
      jsonDataRepository  = core.jsonDataRepository
      dbLayer             = core.dbLayer
      repository          = core.repository
      effectiveSBotConfig = core.sBotConfig
      _                     <- Resource.eval(log.info(s"[${effectiveSBotConfig.sBotInfo.botId}] Delete webook..."))
      deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
      _                     <- Resource.eval(
        Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
          new RuntimeException(
            s"[${effectiveSBotConfig.sBotInfo.botId}] The delete webhook request failed: " + deleteWebhookResponse
              .as[String]
          )
        )
      )
      _       <- Resource.eval(log.info(s"[${effectiveSBotConfig.sBotInfo.botId}] Webhook deleted"))
      baseUrl <- Resource.eval(Async[F].fromEither(Uri.fromString(s"https://api.telegram.org/bot${tk.value}")))
      api = BotApi(
        httpClient,
        baseUrl = baseUrl.renderString
      )
      notifier <- Resource.eval(
        Notifier.resilient[F](new TelegramNotifier[F](opsChatId)(using Async[F], api, log), NotifierWindow)
      )
      backgroundJobManager <- Resource.eval(
        TelegramBackgroundJobManager[F](
          repository = repository,
          dbLayer = dbLayer,
          sBotInfo = effectiveSBotConfig.sBotInfo,
          ttl = effectiveSBotConfig.messageTimeToLive
        )(using Async[F], api, Random[F], notifier, log)
      )
      path           <- Resource.eval(Async[F].fromEither(Uri.fromString(s"/${tk.value}")))
      webhookBaseUri <- Resource.eval(Async[F].fromEither(Uri.fromString(webhookBaseUrl + path)))
    } yield BotSetup(
      token = tk,
      httpClient = httpClient,
      repository = repository,
      jsonDataRepository = jsonDataRepository,
      dbLayer = dbLayer,
      backgroundJobManager = backgroundJobManager,
      api = api,
      notifier = notifier,
      webhookUri = webhookBaseUri,
      webhookPath = path,
      sBotConfig = effectiveSBotConfig,
      opsChatId = opsChatId,
      llmEngine = core.llmEngine
    )
  }
}
