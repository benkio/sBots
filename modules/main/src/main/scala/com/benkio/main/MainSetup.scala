package com.benkio.main

import cats.effect.Async
import cats.effect.Resource
import com.benkio.chatcore.repository.db.DBLayer
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
    healthcheckEndpoint: Uri,
    healthcheckCron: CronExpr
)

object MainSetup {

  def apply[F[_]: Async: Network]()(using log: LogWriter[F]): Resource[F, MainSetup[F]] = for {
    config     <- Resource.eval(Config.loadConfig[F])
    _          <- Resource.eval(log.info(s"[Main] Configuration: $config"))
    httpClient <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
    _          <- Resource.eval(log.info("[Main] httpClient"))
    dbLayer    <- Resource.eval(
      DBLayer[F](
        Transactor.fromDriverManager[F](
          config.mainDB.driver,
          config.mainDB.url,
          "",
          "",
          None
        )
      )
    )
    _           <- Resource.eval(log.info("[Main] dbLayer"))
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
    healthcheckEndpoint = config.healthcheckPing.endpoint,
    healthcheckCron = config.healthcheckPing.cron
  )
}
