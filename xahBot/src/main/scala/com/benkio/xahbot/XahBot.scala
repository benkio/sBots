package com.benkio.xahbot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.web.UrlFetcher
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

class XahBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    val dbLayer: DBLayer[F]
) extends BotSkeletonPolling[F]
    with XahBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = dbLayer.resourceAccess
}

class XahBotWebhook[F[_]: Async: Api: LogWriter](
    url: Uri,
    val dbLayer: DBLayer[F],
    path: Uri = uri"/"
) extends BotSkeletonWebhook[F](url, path)
    with XahBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = dbLayer.resourceAccess
}

trait XahBot[F[_]] extends BotSkeleton[F] {

  val dbLayer: DBLayer[F]

  override val botName: String   = XahBot.botName
  override val botPrefix: String = XahBot.botPrefix

  override def commandRepliesDataF(implicit asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    CommandRepliesData.values[F](dbLayer = dbLayer, botName = botName).pure[F]

  override def messageRepliesDataF(implicit
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    log.debug("[XahBot] Empty message reply data") *> List.empty.pure[F]

}

object XahBot extends BotOps {

  val botName: String   = "XahBot"
  val botPrefix: String = "xah"
  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess.fromResources.getResourceByteArray("xah_XahBot.token").map(_.map(_.toChar).mkString)

  final case class BotSetup[F[_]](token: String, dbLayer: DBLayer[F])

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
    urlFetcher            <- Resource.eval(UrlFetcher[F](httpClient))
    dbLayer               <- Resource.eval(DBLayer[F](transactor, urlFetcher))
    _                     <- Resource.eval(log.info("[XahBot] Delete webook..."))
    deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
    _ <- Resource.eval(
      Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
        new RuntimeException("[XahBot] The delete webhook request failed: " + deleteWebhookResponse.as[String])
      )
    )
    _ <- Resource.eval(log.info("[XahBot] Webhook deleted"))
  } yield BotSetup(tk, dbLayer)

  def buildPollingBot[F[_]: Parallel: Async, A](
      action: XahBotPolling[F] => F[A]
  )(implicit log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].build
    botSetup   <- buildCommonBot[F](httpClient)
  } yield (httpClient, botSetup)).use(httpClient_botSetup => {
    implicit val api: Api[F] =
      BotApi(httpClient_botSetup._1, baseUrl = s"https://api.telegram.org/bot${httpClient_botSetup._2.token}")
    action(new XahBotPolling[F](httpClient_botSetup._2.dbLayer))
  })

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host
  )(implicit log: LogWriter[F]): Resource[F, XahBotWebhook[F]] = for {
    botSetup <- buildCommonBot[F](httpClient)
    baseUrl  <- Resource.eval(Async[F].fromEither(Uri.fromString(s"https://api.telegram.org/bot${botSetup.token}")))
    path     <- Resource.eval(Async[F].fromEither(Uri.fromString(s"/${botSetup.token}")))
    webhookBaseUri <- Resource.eval(Async[F].fromEither(Uri.fromString(webhookBaseUrl + path)))
  } yield {
    implicit val api: Api[F] = BotApi(httpClient, baseUrl = baseUrl.renderString)
    new XahBotWebhook[F](
      url = webhookBaseUri,
      dbLayer = botSetup.dbLayer,
      path = path
    )
  }
}
