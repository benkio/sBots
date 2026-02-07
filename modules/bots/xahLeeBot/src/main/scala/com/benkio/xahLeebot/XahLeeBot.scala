package com.benkio.xahleebot

import cats.*
import cats.effect.*
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.SBot
import com.benkio.telegrambotinfrastructure.SBotPolling
import com.benkio.telegrambotinfrastructure.SBotWebhook
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.ember.client.*
import org.http4s.implicits.*
import org.http4s.Uri
import telegramium.bots.high.*
import telegramium.bots.InputPartFile

class XahLeeBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    override val repository: Repository[F],
    override val dbLayer: DBLayer[F],
    override val backgroundJobManager: BackgroundJobManager[F]
) extends SBotPolling[F]()
    with XahLeeBot[F] {}

class XahLeeBotWebhook[F[_]: Async: Api: LogWriter](
    uri: Uri,
    override val repository: Repository[F],
    override val dbLayer: DBLayer[F],
    override val backgroundJobManager: BackgroundJobManager[F],
    path: Uri = uri"/",
    webhookCertificate: Option[InputPartFile] = None
) extends SBotWebhook[F](uri, path, webhookCertificate)
    with XahLeeBot[F] {}

trait XahLeeBot[F[_]] extends SBot[F] {

  override val sBotConfig: SBotConfig = XahLeeBot.sBotConfig
  val backgroundJobManager: BackgroundJobManager[F]
  val dbLayer: DBLayer[F]

  override val messageRepliesData: List[ReplyBundleMessage] =
    XahLeeBot.messageRepliesData

  override val commandRepliesData: List[ReplyBundleCommand] =
    XahLeeBot.commandRepliesData

}

object XahLeeBot {

  val sBotConfig: SBotConfig = SBotConfig(
    sBotInfo = SBotInfo(SBotId("xah"), SBotInfo.SBotName("XahLeeBot")),
    triggerFilename = "xah_triggers.txt",
    triggerListUri = uri"https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/xah_triggers.txt",
    repliesJsonFilename = "xah_replies.json"
  )
  val tokenFilename: String   = "xah_XahLeeBot.token"
  val configNamespace: String = "xah"

  val messageRepliesData: List[ReplyBundleMessage] = List.empty

  val commandRepliesData: List[ReplyBundleCommand] =
    CommandRepliesData
      .values(
        sBotInfo = sBotConfig.sBotInfo
      )

  def buildPollingBot[F[_]: Parallel: Async: Network](using log: LogWriter[F]): Resource[F, XahLeeBotPolling[F]] =
    for {
      httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
      botSetup   <- BotSetup(
        httpClient = httpClient,
        tokenFilename = tokenFilename,
        namespace = configNamespace,
        sBotConfig = sBotConfig
      )
    } yield new XahLeeBotPolling[F](
      repository = botSetup.repository,
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
      sBotConfig = sBotConfig,
      webhookBaseUrl = webhookBaseUrl
    ).map { botSetup =>
      new XahLeeBotWebhook[F](
        uri = botSetup.webhookUri,
        path = botSetup.webhookPath,
        repository = botSetup.repository,
        dbLayer = botSetup.dbLayer,
        backgroundJobManager = botSetup.backgroundJobManager,
        webhookCertificate = webhookCertificate
      )(using Async[F], botSetup.api, log)
    }
}
