package com.benkio.xahleebot

import cats.*
import cats.effect.*
import cats.implicits.*
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.BotSkeleton
import com.benkio.telegrambotinfrastructure.BotSkeletonPolling
import com.benkio.telegrambotinfrastructure.BotSkeletonWebhook
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.ember.client.*
import org.http4s.implicits.*
import org.http4s.Uri
import telegramium.bots.high.*
import telegramium.bots.InputPartFile

class XahLeeBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    resourceAccessInput: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F]
) extends BotSkeletonPolling[F](resourceAccessInput)
    with XahLeeBot[F] {
  override def resourceAccess: ResourceAccess[F] =
    resourceAccessInput
}

class XahLeeBotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    resourceAccessInput: ResourceAccess[F],
    val dbLayer: DBLayer[F],
    val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends BotSkeletonWebhook[F](uri, path, webhookCertificate, resourceAccessInput)
    with XahLeeBot[F] {
  override def resourceAccess: ResourceAccess[F] =
    resourceAccessInput
}

trait XahLeeBot[F[_]: Async: LogWriter] extends BotSkeleton[F] {

  override val botName: String         = XahLeeBot.botName
  override val botPrefix: String       = XahLeeBot.botPrefix
  override val triggerFilename: String = XahLeeBot.triggerFilename
  override val triggerListUri: Uri     = XahLeeBot.triggerListUri
  val backgroundJobManager: BackgroundJobManager[F]
  val dbLayer: DBLayer[F]

  override def messageRepliesDataF: F[List[ReplyBundleMessage[F]]] =
    LogWriter.debug("[XahLeeBot] Empty message reply data") *> XahLeeBot.messageRepliesData[F].pure[F]

  override def commandRepliesDataF: F[List[ReplyBundleCommand[F]]] =
    XahLeeBot.commandRepliesData[F](backgroundJobManager, dbLayer).pure[F]

}

object XahLeeBot {

  val botName: String         = "XahLeeBot"
  val botPrefix: String       = "xah"
  val tokenFilename: String   = "xah_XahLeeBot.token"
  val configNamespace: String = "xah"
  val triggerFilename: String = "xah_triggers.txt"
  val triggerListUri: Uri     = uri"https://github.com/benkio/sBots/blob/master/modules/bots/XahLeeBot/xah_triggers.txt"

  def messageRepliesData[F[_]]: List[ReplyBundleMessage[F]] = List.empty

  def commandRepliesData[F[_]: Async: LogWriter](
      backgroundJobManager: BackgroundJobManager[F],
      dbLayer: DBLayer[F]
  ): List[ReplyBundleCommand[F]] =
    CommandRepliesData
      .values[F](
        dbLayer = dbLayer,
        backgroundJobManager = backgroundJobManager,
        botName = botName,
        botPrefix = botPrefix
      )

  def buildPollingBot[F[_]: Parallel: Async: Network](using log: LogWriter[F]): Resource[F, XahLeeBotPolling[F]] =
    for {
      httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
      botSetup <- BotSetup(
        httpClient = httpClient,
        tokenFilename = tokenFilename,
        namespace = configNamespace,
        botName = botName
      )
    } yield new XahLeeBotPolling[F](
      resourceAccessInput = botSetup.resourceAccess,
      dbLayer = botSetup.dbLayer,
      backgroundJobManager = botSetup.backgroundJobManager
    )(using Parallel[F], Async[F], botSetup.api, log)

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
        resourceAccessInput = botSetup.resourceAccess,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(using Async[F], botSetup.api, log)
    }
}
