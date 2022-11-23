package com.benkio.xahbot

import cats._
import cats.effect._
import cats.implicits._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.model._
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure._
import log.effect.LogWriter
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.ember.client._
import org.http4s.implicits._
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
    uri: Uri,
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/"
) extends BotSkeletonWebhook[F](uri, path)
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

object XahBot {

  val botName: String         = "XahBot"
  val botPrefix: String       = "xah"
  val linkSources: String     = "xah_LinkSources"
  val tokenFilename: String   = "xah_XahBot.token"
  val configNamespace: String = "xahDB"

  def buildPollingBot[F[_]: Parallel: Async, A](
      action: XahBotPolling[F] => F[A]
  )(implicit log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].build
    botSetup <- BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botName = botName,
      linkSources = linkSources
    )
  } yield botSetup).use { botSetup =>
    action(
      new XahBotPolling[F](
        resAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager
      )(Parallel[F], Async[F], botSetup.api, botSetup.action, log)
    )
  }

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host
  )(implicit log: LogWriter[F]): Resource[F, XahBotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botName = botName,
      linkSources = linkSources,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new XahBotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        resAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager
      )(Async[F], botSetup.api, botSetup.action, log)
    }
}
