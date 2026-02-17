package com.benkio.XahLeeBot

import cats.*
import cats.effect.*
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleCommand
import com.benkio.telegrambotinfrastructure.model.reply.ReplyBundleMessage
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotName
import com.benkio.telegrambotinfrastructure.SBotPolling
import com.benkio.telegrambotinfrastructure.SBotWebhook
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.ember.client.*
import org.http4s.implicits.*
import telegramium.bots.high.*
import telegramium.bots.InputPartFile

class XahLeeBotPolling[F[_]: Parallel: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    override val messageRepliesData: List[ReplyBundleMessage]
) extends SBotPolling[F](sBotSetup, messageRepliesData, XahLeeBot.commandRepliesData)

class XahLeeBotWebhook[F[_]: Async: Api: LogWriter](
    override val sBotSetup: BotSetup[F],
    override val messageRepliesData: List[ReplyBundleMessage],
    webhookCertificate: Option[InputPartFile] = None
) extends SBotWebhook[F](sBotSetup, messageRepliesData, XahLeeBot.commandRepliesData, webhookCertificate)

object XahLeeBot {

  val tokenFilename: String   = "xah_XahLeeBot.token"
  val configNamespace: String = "xah"
  val sBotConfig: SBotConfig  = SBotConfig(
    sBotInfo = SBotInfo(SBotId("xah"), SBotName("XahLeeBot")),
    triggerFilename = "xah_triggers.txt",
    triggerListUri = uri"https://github.com/benkio/sBots/blob/main/modules/bots/XahLeeBot/xah_triggers.txt",
    token = tokenFilename,
    listJsonFilename = "xah_list.json",
    repliesJsonFilename = "xah_replies.json",
    commandsJsonFilename = "xah_commands.json"
  )

  val commandRepliesData: List[ReplyBundleCommand] =
    CommandRepliesData.values(sBotInfo = sBotConfig.sBotInfo)

  def buildPollingBot[F[_]: Parallel: Async: Network](using log: LogWriter[F]): Resource[F, XahLeeBotPolling[F]] =
    for {
      httpClient         <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
      botSetup           <- BotSetup(httpClient = httpClient, sBotConfig = sBotConfig)
      messageRepliesData <- Resource.eval(
        botSetup.jsonDataRepository.loadData[ReplyBundleMessage](XahLeeBot.sBotConfig.repliesJsonFilename)
      )
    } yield new XahLeeBotPolling[F](botSetup, messageRepliesData)(using Parallel[F], Async[F], botSetup.api, log)

  def buildWebhookBot[F[_]: Async](
      httpClient: Client[F],
      webhookBaseUrl: String = org.http4s.server.defaults.IPv4Host,
      webhookCertificate: Option[InputPartFile] = None
  )(using log: LogWriter[F]): Resource[F, XahLeeBotWebhook[F]] = for {
    botSetup           <- BotSetup(httpClient = httpClient, sBotConfig = sBotConfig, webhookBaseUrl = webhookBaseUrl)
    messageRepliesData <- Resource.eval(
      botSetup.jsonDataRepository.loadData[ReplyBundleMessage](XahLeeBot.sBotConfig.repliesJsonFilename)
    )
  } yield new XahLeeBotWebhook[F](botSetup, messageRepliesData, webhookCertificate)(using Async[F], botSetup.api, log)
}
