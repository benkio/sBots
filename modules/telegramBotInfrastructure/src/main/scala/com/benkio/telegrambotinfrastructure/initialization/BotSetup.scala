package com.benkio.telegrambotinfrastructure.initialization

import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import cats.MonadThrow
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.http.DropboxClient
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceFile
import com.benkio.telegrambotinfrastructure.model.reply.Document
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.db.DBRepository
import com.benkio.telegrambotinfrastructure.repository.JsonDataRepository
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.repository.ResourcesRepository
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import doobie.Transactor
import log.effect.LogWriter
import org.http4s.*
import org.http4s.client.Client
import telegramium.bots.high.Api
import telegramium.bots.high.BotApi

import java.io.File

final case class BotSetup[F[_]](
    token: String,
    httpClient: Client[F],
    repository: Repository[F],
    jsonDataRepository: JsonDataRepository[F],
    dbLayer: DBLayer[F],
    backgroundJobManager: BackgroundJobManager[F],
    api: Api[F],
    webhookUri: Uri,
    webhookPath: Uri,
    sBotConfig: SBotConfig
)

object BotSetup {

  enum BotSetupError(msg: String) extends Throwable(msg) {
    case TokenNotFound(tokenFilename: String) extends BotSetupError(s"[BotSetup] Cannot find the token $tokenFilename")
    case TokenIsEmpty(tokenFilename: String)
        extends BotSetupError(s"[BotSetup] the retrieved token $tokenFilename is empty")
  }

  def deleteWebhooks[F[_]: Async](
      httpClient: Client[F],
      token: String
  ): Resource[F, Response[F]] = for {
    uri <- Resource.eval(
      MonadThrow[F].fromEither(Uri.fromString(s"https://api.telegram.org/bot$token/setWebhook?url="))
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
  ): Resource[F, String] = {
    for {
      _                   <- Resource.eval(LogWriter.info(s"[BotSetup:58:47] Retrieving Token $tokenFilename"))
      tokenMediaResources <- repository.getResourceFile(Document(tokenFilename))
      tokenFiles          <- tokenMediaResources
        .map(_.collect { case MediaResourceFile(rf) =>
          rf
        }.sequence)
        .getOrElse(Resource.eval[F, List[File]](Async[F].raiseError(BotSetupError.TokenNotFound(tokenFilename))))
      tokenFileContent <-
        tokenFiles.headOption.fold(Resource.eval(Async[F].raiseError(BotSetupError.TokenNotFound(tokenFilename))))(f =>
          Repository.fileToString(f)
        )
      _ <- Resource.eval(
        Async[F].raiseWhen(tokenFileContent.isEmpty)(
          BotSetupError.TokenIsEmpty(tokenFilename)
        )
      )
      _ <- Resource.eval(
        LogWriter.info(s"[BotSetup:58:47] Token $tokenFilename successfully retrieved")
      )
    } yield tokenFileContent
  }

  def loadDB[F[_]: Async](config: DBConfig)(using log: LogWriter[F]): Resource[F, DBLayer[F]] =
    Resource.eval(
      DBLayer[F](
        Transactor.fromDriverManager[F](
          config.driver,
          config.url,
          "",
          "",
          None
        )
      )
    )

  def apply[F[_]: Async](
      httpClient: Client[F],
      sBotConfig: SBotConfig,
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host
  )(using log: LogWriter[F]): Resource[F, BotSetup[F]] = {

    val resourceRepository = ResourcesRepository.fromResources[F]()
    for {
      tk            <- token[F](sBotConfig.token, resourceRepository)
      config        <- Resource.eval(Config.loadConfig[F](sBotConfig.sBotInfo.botId.value))
      _             <- Resource.eval(log.info(s"[${sBotConfig.sBotInfo.botId}] Configuration: $config"))
      dropboxClient <- Resource.eval(DropboxClient[F](httpClient))
      dbLayer       <- loadDB[F](config.db)
      repository = DBRepository.dbResources[F](dbLayer.dbMedia, dropboxClient)
      _                     <- Resource.eval(log.info(s"[${sBotConfig.sBotInfo.botId}] Delete webook..."))
      deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
      _                     <- Resource.eval(
        Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
          new RuntimeException(
            s"[${sBotConfig.sBotInfo.botId}] The delete webhook request failed: " + deleteWebhookResponse.as[String]
          )
        )
      )
      _       <- Resource.eval(log.info(s"[${sBotConfig.sBotInfo.botId}] Webhook deleted"))
      baseUrl <- Resource.eval(Async[F].fromEither(Uri.fromString(s"https://api.telegram.org/bot$tk")))
      api = BotApi(
        httpClient,
        baseUrl = baseUrl.renderString
      )
      backgroundJobManager <- Resource.eval(
        BackgroundJobManager[F](
          dbLayer = dbLayer,
          sBotInfo = sBotConfig.sBotInfo,
          ttl = sBotConfig.messageTimeToLive
        )(using Async[F], api, log)
      )
      path           <- Resource.eval(Async[F].fromEither(Uri.fromString(s"/$tk")))
      webhookBaseUri <- Resource.eval(Async[F].fromEither(Uri.fromString(webhookBaseUrl + path)))
    } yield BotSetup(
      token = tk,
      httpClient = httpClient,
      repository = repository,
      jsonDataRepository = JsonDataRepository[F]( // resourceRepository
      ),
      dbLayer = dbLayer,
      backgroundJobManager = backgroundJobManager,
      api = api,
      webhookUri = webhookBaseUri,
      webhookPath = path,
      sBotConfig = sBotConfig
    )
  }
}
