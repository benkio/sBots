package com.benkio.telegrambotinfrastructure.initialization

import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import java.nio.file.Files
import com.benkio.telegrambotinfrastructure.model.reply.Document
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.telegram.TelegramReply

import cats.MonadThrow
import cats.effect.Async
import cats.effect.Resource
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.Config
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.web.UrlFetcher
import doobie.Transactor
import log.effect.LogWriter
import org.http4s.client.Client

import org.http4s.*
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
      token: String,
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
    resourceAccess
      .getResourceFile(Document(tokenFilename))
      .evalMap {
        case MediaResource.MediaResourceFile(f) => Files.readAllBytes(f.toPath).map(_.toChar).mkString.pure
        case MediaResource.MediaResourceIFile(x) =>
          Async[F].raiseError(
            Throwable(
              s"[BotSetup] Cannot find bot token. Expected: MediaResourceFile, got: ${MediaResource.MediaResourceIFile(x)}"
            )
          )
      }

  def loadDB[F[_]: Async](config: Config)(using log: LogWriter[F]): Resource[F, DBLayer[F]] =
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
    tk         <- token[F](tokenFilename, ResourceAccess.fromResources[F]())
    config     <- Resource.eval(Config.loadConfig[F](namespace))
    _          <- Resource.eval(log.info(s"[$botName] Configuration: $config"))
    urlFetcher <- Resource.eval(UrlFetcher[F](httpClient))
    dbLayer    <- loadDB[F](config)
    resourceAccess = ResourceAccess.dbResources[F](dbLayer.dbMedia, urlFetcher)
    _                     <- Resource.eval(log.info(s"[$botName] Delete webook..."))
    deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
    _ <- Resource.eval(
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
