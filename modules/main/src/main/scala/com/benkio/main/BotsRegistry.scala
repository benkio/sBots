package com.benkio.main

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.config.SBotConfig
import com.benkio.telegrambotinfrastructure.model.reply.Text
import com.benkio.telegrambotinfrastructure.model.SBotInfo
import com.benkio.telegrambotinfrastructure.SBot
import com.benkio.telegrambotinfrastructure.SBotMainPolling
import com.benkio.telegrambotinfrastructure.SBotWebhook
import com.benkio.ABarberoBot.ABarberoBot
import com.benkio.CalandroBot.CalandroBot
import com.benkio.M0sconiBot.M0sconiBot
import com.benkio.RichardPHJBensonBot.RichardPHJBensonBot
import com.benkio.XahLeeBot.XahLeeBot
import com.benkio.YouTuboAncheI0Bot.YouTuboAncheI0Bot
import log.effect.LogLevels
import log.effect.LogWriter
import telegramium.bots.high.WebhookBot
import telegramium.bots.Message

final case class BotRegistryEntry[F[_]](
    sBotInfo: SBotInfo,
    commandEffectfulCallback: Map[String, Message => F[List[Text]]] = Map.empty
)

extension (botRegistryEntry: BotRegistryEntry[IO]) {
  def sBotWebhookResource(mainSetup: MainSetup[IO])(using log: LogWriter[IO]): Resource[IO, SBotWebhook[IO]] = {
    SBot.buildWebhookBot[IO](
      httpClient = mainSetup.httpClient,
      sBotInfo = botRegistryEntry.sBotInfo,
      webhookBaseUrl = mainSetup.webhookBaseUrl,
      webhookCertificate = mainSetup.webhookCertificate,
      commandEffectfulCallback = botRegistryEntry.commandEffectfulCallback
    )
  }
  def sBotConfig: SBotConfig = SBot.buildSBotConfig(botRegistryEntry.sBotInfo)
}

opaque type BotRegistry[F[_]] = List[BotRegistryEntry[F]]

extension (botRegistry: BotRegistry[IO]) {
  def runPolling(): IO[ExitCode] =
    botRegistry
      .map(botRegistryEntry => SBotMainPolling.run(logLevel = LogLevels.Info, sBotInfo = botRegistryEntry.sBotInfo))
      .reduce(op = (botA, botB) => botA &> botB)
  def webhookBots(mainSetup: MainSetup[IO])(using log: LogWriter[IO]): Resource[IO, List[WebhookBot[IO]]] =
    botRegistry
      .traverse(_.sBotWebhookResource(mainSetup))
}

object BotRegistry {
  def apply[F[_]](entries: List[BotRegistryEntry[F]]): BotRegistry[F] = entries
  def toList[F[_]](r: BotRegistry[F]): List[BotRegistryEntry[F]]      = r

  val value: BotRegistry[IO] = BotRegistry(
    List(
      BotRegistryEntry[IO](sBotInfo = ABarberoBot.sBotInfo),
      BotRegistryEntry[IO](sBotInfo = CalandroBot.sBotInfo),
      BotRegistryEntry[IO](sBotInfo = M0sconiBot.sBotInfo),
      BotRegistryEntry[IO](sBotInfo = XahLeeBot.sBotInfo),
      BotRegistryEntry[IO](sBotInfo = YouTuboAncheI0Bot.sBotInfo),
      BotRegistryEntry[IO](
        sBotInfo = RichardPHJBensonBot.sBotInfo,
        commandEffectfulCallback = RichardPHJBensonBot.commandEffectfulCallback[IO]
      )
    )
  )
}
