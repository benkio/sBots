package com.benkio.xahleebot

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
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.ember.client._
import org.http4s.implicits._
import telegramium.bots.InputPartFile
import telegramium.bots.high._

class XahLeeBotPolling[F[_]: Parallel: Async: Api: Action: LogWriter](
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F]
    with XahLeeBot[F] {
  override def resourceAccess(using syncF: Sync[F]): ResourceAccess[F] = resAccess
}

class XahLeeBotWebhook[F[_]: Async: Api: Action: LogWriter](
    uri: Uri,
    resAccess: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends BotSkeletonWebhook[F](uri, path, webhookCertificate)
    with XahLeeBot[F] {
  override def resourceAccess(using syncF: Sync[F]): ResourceAccess[F] = resAccess
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
    log.debug("[XahLeeBot] Empty message reply data") *> List.empty.pure[F]

  override def commandRepliesDataF(using asyncF: Async[F], log: LogWriter[F]): F[List[ReplyBundleCommand[F]]] =
    CommandRepliesData
      .values[F](
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        botName = botName
      )
      .pure[F]

}

object XahLeeBot {

  val botName: String         = "XahLeeBot"
  val botPrefix: String       = "xah"
  val tokenFilename: String   = "xah_XahLeeBot.token"
  val configNamespace: String = "xahDB"

  def buildPollingBot[F[_]: Parallel: Async: Network, A](
      action: XahLeeBotPolling[F] => F[A]
  )(implicit log: LogWriter[F]): F[A] = (for {
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
        resAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager
      )(Parallel[F], Async[F], botSetup.api, botSetup.action, log)
    )
  }

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(implicit log: LogWriter[F]): Resource[F, XahLeeBotWebhook[F]] =
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
        resAccess = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(Async[F], botSetup.api, botSetup.action, log)
    }
}
