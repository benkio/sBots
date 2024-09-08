package com.benkio.xahleebot

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.model.*
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer

import com.benkio.telegrambotinfrastructure.*
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.ember.client.*
import org.http4s.implicits.*
import telegramium.bots.InputPartFile
import telegramium.bots.high.*

class XahLeeBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    resourceAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F](resourceAccess)
    with XahLeeBot[F] {
  override def resourceAccess(using syncF: Sync[F]): ResourceAccess[F] = resourceAccess
}

class XahLeeBotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    resourceAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends BotSkeletonWebhook[F](uri, path, webhookCertificate, resourceAccess)
    with XahLeeBot[F] {
  override def resourceAccess(using syncF: Sync[F]): ResourceAccess[F] = resourceAccess
}

trait XahLeeBot[F[_]] extends BotSkeleton[F] {

  override val botName: String   = XahLeeBot.botName
  override val botPrefix: String = XahLeeBot.botPrefix
  val backgroundJobManager: BackgroundJobManager[F]
  val dbLayer: DBLayer[F]

  override def messageRepliesDataF(using
      applicativeF: Applicative[F],
      log: LogWriter[F]
  ): F[List[ReplyBundleMessage[F]]] =
    log.debug("[XahLeeBot] Empty message reply data") *> XahLeeBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF(using asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    XahLeeBot.commandRepliesData[F](backgroundJobManager, dbLayer).pure[F]

}

object XahLeeBot {

  val botName: String         = "XahLeeBot"
  val botPrefix: String       = "xah"
  val tokenFilename: String   = "xah_XahLeeBot.token"
  val configNamespace: String = "xahDB"

  def messageRepliesData[F[_]: Applicative]: List[ReplyBundleMessage[F]] = List.empty

  def commandRepliesData[F[_]: Async: LogWriter](
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F]
  ): List[ReplyBundleCommand[F]] =
    CommandRepliesData
      .values[F](
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        botName = botName
      )

  def buildPollingBot[F[_]: Parallel: Async: Network, A](
      action: XahLeeBotPolling[F] => F[A]
  )(using log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
    botSetup <- BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botName = botName
    )
  } yield botSetup).use { botSetup =>
    action(
      new XahLeeBotPolling[F](
        resourceAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager
      )(using Parallel[F], Async[F], botSetup.api, log)
    )
  }

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, XahLeeBotWebhook[F]] =
    BotSetup(
      httpClient = httpClient,
      tokenFilename = tokenFilename,
      namespace = configNamespace,
      botName = botName,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new XahLeeBotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        resourceAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(using Async[F], botSetup.api, log)
    }
}
