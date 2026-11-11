package com.benkio.chattelegramadapter

import cats.effect.std.Random
import cats.effect.Async
import cats.Parallel
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.model.reply.ReplyValue
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chattelegramadapter.http.TelegramNotifier
import com.benkio.chattelegramadapter.initialization.BotSetup
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.ember.client.EmberClientBuilder
import telegramium.bots.high.Api

import java.nio.file.Path

class SBotPolling[F[_]: Parallel: Api](
    override val sBotSetup: BotSetup[F],
    override val commandEffectfulCallback: Map[String, Message => F[ReplyValue]] = Map.empty
)(using Async[F], LogWriter[F])
    extends ISBotPolling[F](sBotSetup)

class SBotWebhook[F[_]: Api](
    override val sBotSetup: BotSetup[F],
    webhookCertificate: Option[Path] = None,
    override val commandEffectfulCallback: Map[String, Message => F[ReplyValue]] = Map.empty
)(using Async[F], LogWriter[F])
    extends ISBotWebhook[F](sBotSetup, webhookCertificate)

object SBot {

  /** Kept for the bots and the tests: the config builder is [[SBotConfig.fromSBotInfo]], not Telegram specific. */
  def buildSBotConfig(sBotInfo: SBotInfo): SBotConfig = SBotConfig.fromSBotInfo(sBotInfo)

  def buildPollingBot[F[_]: Parallel: Async: Network: Random, A](
      action: SBotPolling[F] => F[A],
      sBotInfo: SBotInfo,
      commandEffectfulCallback: Map[String, Message => F[ReplyValue]] = Map.empty,
      opsChatId: Long = TelegramNotifier.defaultOpsChatId
  )(using log: LogWriter[F]): F[A] = (for {
    httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
    sBotSetup  <- BotSetup(
      httpClient = httpClient,
      sBotConfig = buildSBotConfig(sBotInfo),
      opsChatId = opsChatId
    )
  } yield sBotSetup).use { sBotSetup =>
    given Api[F] = sBotSetup.api
    action(
      new SBotPolling[F](
        sBotSetup = sBotSetup,
        commandEffectfulCallback = commandEffectfulCallback
      )
    )
  }
}
