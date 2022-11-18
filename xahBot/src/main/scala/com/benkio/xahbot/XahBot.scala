package com.benkio.xahbot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.default.Actions._
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.web.UrlFetcher
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.BotOps
import com.benkio.telegrambotinfrastructure._
import doobie.Transactor
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.ember.client._
import org.http4s.implicits._
import org.http4s.Status
import org.http4s.Uri
import telegramium.bots.high._

class XahBotPolling[F[_]: Parallel: Async: Api: Action: LogWriter](
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F]
    with XahBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = resAccess
}

class XahBotWebhook[F[_]: Async: Api: Action: LogWriter](
    url: Uri,
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/"
) extends BotSkeletonWebhook[F](url, path)
    with XahBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = resAccess
}

trait XahBot[F[_]] extends BotSkeleton[F] {

  override val botName: String   = XahBot.botName
  override val botPrefix: String = XahBot.botPrefix
  val linkSources: String        = XahBot.linkSources
  val backgroundJobManager: BackgroundJobManager[F]

  override def commandRepliesDataF(implicit asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    CommandRepliesData
      .values[F](
        resourceAccess = resourceAccess,
        backgroundJobManager = backgroundJobManager,
        linkSources = linkSources,
        botName = botName
      )
      .pure[F]

  override def messageRepliesDataF(implicit
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    log.debug("[XahBot] Empty message reply data") *> List.empty.pure[F]

}

object XahBot extends BotOps {

  val botName: String     = "XahBot"
  val botPrefix: String   = "xah"
  val linkSources: String = "xah_LinkSources"
  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess.fromResources.getResourceByteArray("xah_XahBot.token").map(_.map(_.toChar).mkString)

  final case class BotSetup[F[_]](
      token: String,
      httpClient: Client[F],
      resourceAccess: ResourceAccess[F],
      dbLayer: DBLayer[F]
  )

  def buildCommonBot[F[_]: Async](
      httpClient: Client[F]
  )(implicit log: LogWriter[F]): Resource[F, BotSetup[F]] = for {
    tk     <- token[F]
    config <- Resource.eval(Config.loadConfig[F])
    _      <- Resource.eval(log.info(s"XahBot Configuration: $config"))
    transactor = Transactor.fromDriverManager[F](
      config.driver,
      config.url,
      "",
      ""
    )
    urlFetcher <- Resource.eval(UrlFetcher[F](httpClient))
    dbLayer    <- Resource.eval(DBLayer[F](transactor))
    resourceAccess = ResourceAccess.dbResources[F](dbLayer.dbMedia, urlFetcher)
    _                     <- Resource.eval(log.info("[XahBot] Delete webook..."))
    deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
    _ <- Resource.eval(
      Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
        new RuntimeException("[XahBot] The delete webhook request failed: " + deleteWebhookResponse.as[String])
      )
    )
    _ <- Resource.eval(log.info("[XahBot] Webhook deleted"))
  } yield BotSetup(tk, httpClient, resourceAccess, dbLayer)

  def buildPollingBot[F[_]: Parallel: Async: LogWriter, A](
      action: XahBotPolling[F] => F[A]
  ): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].build
    botSetup   <- buildCommonBot[F](httpClient)
  } yield botSetup).use(botSetup => {
    implicit val api: Api[F] =
      BotApi(botSetup.httpClient, baseUrl = s"https://api.telegram.org/bot${botSetup.token}")
    implicit val resAccess = botSetup.resourceAccess
    for {
      backgroundJobManager <- BackgroundJobManager[F](
        dbSubscription = botSetup.dbLayer.dbSubscription,
        resourceAccess = botSetup.resourceAccess,
        youtubeLinkSources = XahBot.linkSources
      )
      result <- action(
        new XahBotPolling[F](
          resAccess = botSetup.resourceAccess,
          backgroundJobManager = backgroundJobManager,
          dbLayer = botSetup.dbLayer
        )
      )
    } yield result
  })

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host
  )(implicit log: LogWriter[F]): Resource[F, XahBotWebhook[F]] = for {
    botSetup <- buildCommonBot[F](httpClient)
    baseUrl  <- Resource.eval(Async[F].fromEither(Uri.fromString(s"https://api.telegram.org/bot${botSetup.token}")))
    path     <- Resource.eval(Async[F].fromEither(Uri.fromString(s"/${botSetup.token}")))
    webhookBaseUri <- Resource.eval(Async[F].fromEither(Uri.fromString(webhookBaseUrl + path)))
    api       = BotApi(httpClient, baseUrl = baseUrl.renderString)
    resAccess = botSetup.resourceAccess
    backgroundJobManager <- {
      implicit val implApi: Api[F] = api
      implicit val implResAccess   = resAccess
      Resource.eval(
        BackgroundJobManager[F](
          dbSubscription = botSetup.dbLayer.dbSubscription,
          resourceAccess = botSetup.resourceAccess,
          youtubeLinkSources = XahBot.linkSources
        )
      )
    }
  } yield {
    implicit val implApi: Api[F] = api
    implicit val implResAccess   = resAccess
    new XahBotWebhook[F](
      url = webhookBaseUri,
      resAccess = botSetup.resourceAccess,
      backgroundJobManager = backgroundJobManager,
      dbLayer = botSetup.dbLayer,
      path = path
    )
  }
}
