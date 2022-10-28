package com.benkio.xahbot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.patterns.CommandPatterns._
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBResourceAccess
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
    rAccess: ResourceAccess[F]
) extends BotSkeletonPolling[F]
    with XahBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = rAccess
}

class XahBotWebhook[F[_]: Async: Api: LogWriter](url: Uri, rAccess: ResourceAccess[F], path: Uri = uri"/")
    extends BotSkeletonWebhook[F](url, path)
    with XahBot[F] {
  override def resourceAccess(implicit syncF: Sync[F]): ResourceAccess[F] = rAccess
}

trait XahBot[F[_]] extends BotSkeleton[F] {

  override val botName: String = "XahBot"

  override def commandRepliesDataF(implicit asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] = List(
    randomLinkByKeywordReplyBundleF,
    randomLinkReplyBundleF
  ).sequence[F, ReplyBundleCommand[F]].map(_ ++ CommandRepliesData.values[F])

  override def messageRepliesDataF(implicit
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    log.debug("[XahBot] Empty message reply data") *> List.empty.pure[F]

  private def randomLinkReplyBundleF(implicit asyncF: Async[F], log: LogWriter[F]): F[ReplyBundleCommand[F]] =
    RandomLinkCommand.selectRandomLinkReplyBundleCommand(
      resourceAccess = resourceAccess,
      youtubeLinkSources = "xah_LinkSources"
    )

  private def randomLinkByKeywordReplyBundleF(implicit asyncF: Async[F], log: LogWriter[F]): F[ReplyBundleCommand[F]] =
    RandomLinkCommand.selectRandomLinkByKeywordsReplyBundleCommand(
      resourceAccess = resourceAccess,
      botName = botName,
      youtubeLinkSources = "xah_LinkSources"
    )
}

object XahBot extends BotOps {

  def token[F[_]: Async]: Resource[F, String] =
    ResourceAccess.fromResources.getResourceByteArray("xah_XahBot.token").map(_.map(_.toChar).mkString)

  def buildCommonBot[F[_]: Async](
      httpClient: Client[F]
  )(implicit log: LogWriter[F]): Resource[F, (String, ResourceAccess[F])] = for {
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
    dbResourceAccess      <- Resource.eval(DBResourceAccess(transactor, urlFetcher))
    _                     <- Resource.eval(log.info("[XahBot] Delete webook..."))
    deleteWebhookResponse <- deleteWebhooks[F](httpClient, tk)
    _ <- Resource.eval(
      Async[F].raiseWhen(deleteWebhookResponse.status != Status.Ok)(
        new RuntimeException("[XahBot] The delete webhook request failed: " + deleteWebhookResponse.as[String])
      )
    )
    _ <- Resource.eval(log.info("[XahBot] Webhook deleted"))
  } yield (tk, dbResourceAccess)

  def buildPollingBot[F[_]: Parallel: Async, A](
      action: XahBotPolling[F] => F[A]
  )(implicit log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].build
    tk_ra      <- buildCommonBot[F](httpClient)
  } yield (httpClient, tk_ra._1, tk_ra._2)).use(httpClient_tk_ra => {
    implicit val api: Api[F] =
      BotApi(httpClient_tk_ra._1, baseUrl = s"https://api.telegram.org/bot${httpClient_tk_ra._2}")
    action(new XahBotPolling[F](httpClient_tk_ra._3))
  })

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host
  )(implicit log: LogWriter[F]): Resource[F, XahBotWebhook[F]] = for {
    tk_ra          <- buildCommonBot[F](httpClient)
    baseUrl        <- Resource.eval(Async[F].fromEither(Uri.fromString(s"https://api.telegram.org/bot${tk_ra._1}")))
    path           <- Resource.eval(Async[F].fromEither(Uri.fromString(s"/${tk_ra._1}")))
    webhookBaseUri <- Resource.eval(Async[F].fromEither(Uri.fromString(webhookBaseUrl + path)))
  } yield {
    implicit val api: Api[F] = BotApi(httpClient, baseUrl = baseUrl.renderString)
    new XahBotWebhook[F](
      url = webhookBaseUri,
      rAccess = tk_ra._2,
      path = path
    )
  }
}
