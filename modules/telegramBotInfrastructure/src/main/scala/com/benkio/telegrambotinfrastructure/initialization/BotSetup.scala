package com.benkio.telegrambotinfrastructure.initialization

import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import cats.MonadThrow
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceFile
import com.benkio.telegrambotinfrastructure.model.reply.Document
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.telegram.TelegramReply
import com.benkio.telegrambotinfrastructure.web.DropboxClient
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import doobie.Transactor
import log.effect.LogWriter
import org.http4s.*
import org.http4s.client.Client
import telegramium.bots.high.Api
import telegramium.bots.high.BotApi

final case class BotSetup[F[_]](
    token: String,
    httpClient: Client[F],
    resourceAccess: ResourceAccess[F],
    dbLayer: DBLayer[F],
    backgroundJobManager: BackgroundJobManager[F],
    api: Api[F],
    webhookUri: Uri,
    webhookPath: Uri
)

object BotSetup {

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
      resourceAccess: ResourceAccess[F]
  ): Resource[F, String] =
    for
      _                   <- Resource.eval(LogWriter.info(s"[BotSetup:58:47] Retrieving Token $tokenFilename"))
      tokenMediaResources <- resourceAccess.getResourceFile(Document(tokenFilename))
      tokenFiles          <- tokenMediaResources.collect { case MediaResourceFile(rf) =>
        rf
      }.sequence
      tokenFileContent <-
        tokenFiles.headOption.fold(Resource.raiseError(Throwable(s"[BotSetup] Cannot find the token $tokenFilename")))(
          f => ResourceAccess.fileToString(f)
        )
      _ <- Resource.eval(
        Async[F].raiseWhen(tokenFileContent.isEmpty)(
          Throwable(s"[BotSetup] the retrieved token $tokenFilename is empty")
        )
      )
      _ <- Resource.eval(
        LogWriter.info(s"[BotSetup:58:47] Token $tokenFilename successfully retrieved")
      )
    yield tokenFileContent

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
      tokenFilename: String,
      namespace: String,
      botName: String,
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host
  )(using log: LogWriter[F], telegramReply: TelegramReply[Text]): Resource[F, BotSetup[F]] = for {
    tk            <- token[F](tokenFilename, ResourceAccess.fromResources[F]())
    config        <- Resource.eval(Config.loadConfig[F](namespace))
    _             <- Resource.eval(log.info(s"[$botName] Configuration: $config"))
    dropboxClient <- Resource.eval(DropboxClient[F](httpClient))
    dbLayer       <- loadDB[F](config.db)
    resourceAccess = ResourceAccess.dbResources[F](dbLayer.dbMedia, dropboxClient)
    _                     <- Resource.eval(log.info(s"[$botName] Delete webook..."))
    deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
    _                     <- Resource.eval(
      Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
        new RuntimeException(
          s"[$botName] The delete webhook request failed: " + deleteWebhookResponse.as[String]
        )
      )
    )
    _       <- Resource.eval(log.info(s"[$botName] Webhook deleted"))
    baseUrl <- Resource.eval(Async[F].fromEither(Uri.fromString(s"https://api.telegram.org/bot$tk")))
    api = BotApi(
      httpClient,
      baseUrl = baseUrl.renderString
    )
    backgroundJobManager <- Resource.eval(
      BackgroundJobManager[F](
        dbSubscription = dbLayer.dbSubscription,
        dbShow = dbLayer.dbShow,
        botName = botName,
        resourceAccess = resourceAccess
      )(using Async[F], api, telegramReply, log)
    )
    path           <- Resource.eval(Async[F].fromEither(Uri.fromString(s"/$tk")))
    webhookBaseUri <- Resource.eval(Async[F].fromEither(Uri.fromString(webhookBaseUrl + path)))
  } yield BotSetup(
    token = tk,
    httpClient = httpClient,
    resourceAccess = resourceAccess,
    dbLayer = dbLayer,
    backgroundJobManager = backgroundJobManager,
    api = api,
    webhookUri = webhookBaseUri,
    webhookPath = path
  )
}
