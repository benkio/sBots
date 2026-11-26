package com.benkio.main

import cats.effect.Async
import cats.effect.Resource
import cats.syntax.foldable.*
import com.benkio.chatcore.llm.LlmWiring
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.Notifier
import com.benkio.chatdiscordadapter.gateway.DiscordGatewayConfig
import com.benkio.chatircadapter.connection.IrcConfig
import com.benkio.chatllm.llm.LlmClientApi
import com.benkio.chatllm.llm.LlmClients
import com.benkio.chatllm.llm.LlmConfig
import com.benkio.chatllm.llm.OpenRouterClient
import com.benkio.chatllm.ChatLlm
import com.benkio.chatllm.LlmContextCleanup
import cron4s.CronExpr
import doobie.Transactor
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.ember.client.*
import org.http4s.Uri

import java.nio.file.Path
import java.nio.file.Paths

/** What the Telegram bots need from the main setup. */
final case class TelegramSetup(
    webhookBaseUrl: String,
    webhookCertificate: Option[Path],
    opsChatId: Long
)

final case class MainSetup[F[_]](
    httpClient: Client[F],
    dbLayer: DBLayer[F],
    http: HttpServerConfig,
    telegram: TelegramSetup,
    discord: DiscordGatewayConfig,
    irc: IrcConfig,
    healthcheckEndpoint: Uri,
    healthcheckCron: CronExpr,
    mainDBName: String,
    llmConfig: LlmConfig,
    llmClient: LlmClientApi[F],
    llmWiring: LlmWiring[F]
)

object MainSetup {

  def apply[F[_]: Async: Network]()(using log: LogWriter[F]): Resource[F, MainSetup[F]] = for {
    config     <- Resource.eval(Config.loadConfig[F])
    _          <- Resource.eval(log.info(s"[Main] Configuration: $config"))
    httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
    _          <- Resource.eval(log.info("[Main] httpClient"))
    llmClient  <- Resource.eval(
      // Not guarded: LimitedLlmClient must observe a failed quota notification so the next denial retries it.
      LlmClients.build[F](httpClient, config.llm, Notifier.logging[F])
    )
    _ <- Resource.eval(log.info("[Main] llmClient"))
    _ <- Resource.eval(OpenRouterClient.paidModelWarning(config.llm.openrouter).traverse_(w => log.warn(w)))
    // The same database is shared by the bots; the cleanup job works on the whole llm_context table.
    llmTransactor = Transactor.fromDriverManager[F](
      config.mainDB.driver,
      config.mainDB.url,
      "",
      "",
      None
    )
    dbLayer <- Resource.eval(
      DBLayer.make[F](
        llmTransactor,
        botId = com.benkio.chatcore.model.SBotInfo.SBotId("main")
      )
    )
    _ <- Resource.eval(log.info("[Main] dbLayer"))
    // #1489: the cleanup works on the whole llm_context table; main owns its fiber and cancels it on shutdown.
    _ <- Resource.make(LlmContextCleanup.start[F](llmTransactor, config.llm.context, config.llm.trace))(_.cancel)
    certificate <- Resource.eval(
      Async[F].pure(config.telegram.webhookCertificate.map(fp => Paths.get(fp)))
    )
    _ <- Resource.eval(log.info("[Main] webhook certificate"))
  } yield MainSetup(
    httpClient = httpClient,
    dbLayer = dbLayer,
    http = config.http,
    telegram = TelegramSetup(
      webhookBaseUrl = config.telegram.webhookBaseUrl,
      webhookCertificate = certificate,
      opsChatId = config.telegram.opsChatId
    ),
    discord = config.discord,
    irc = config.irc,
    healthcheckEndpoint = config.healthcheckPing.endpoint,
    healthcheckCron = config.healthcheckPing.cron,
    mainDBName = config.mainDB.dbName,
    llmConfig = config.llm,
    llmClient = llmClient,
    // The implementations live in chatLlm; each bot's transactor and id are supplied at its setup. Without the API
    // keys, or the persona of a bot, that bot gets the noop responder.
    llmWiring = ChatLlm.wiring[F](config.llm, llmClient)
  )
}
