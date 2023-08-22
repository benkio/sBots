package com.benkio.main

import cats.effect.Async
import cats.effect.Resource
import com.benkio.main.Config
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import fs2.io.net.Network
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.ember.client._
import telegramium.bots.InputPartFile

import java.io.File

final case class MainSetup[F[_]](
    httpClient: Client[F],
    dbLayer: DBLayer[F],
    webhookBaseUrl: String,
    host: String,
    port: Int,
    webhookCertificate: Option[InputPartFile],
    keystorePath: Option[String],
    keystorePassword: Option[String]
)

object MainSetup {

  def apply[F[_]: Async: Network]()(implicit log: LogWriter[F]): Resource[F, MainSetup[F]] = for {
    config      <- Resource.eval(Config.loadConfig[F])
    _           <- Resource.eval(log.info(s"[Main] Configuration: $config"))
    httpClient  <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
    dbLayer     <- BotSetup.loadDB[F](config.mainDB)
    certificate <- Resource.eval(Async[F].pure(config.webhookCertificate.map(fp => InputPartFile(new File(fp)))))
  } yield MainSetup(
    httpClient = httpClient,
    dbLayer = dbLayer,
    webhookBaseUrl = config.webhookBaseUrl,
    host = config.hostUrl,
    port = config.port,
    webhookCertificate = certificate,
    keystorePath = config.keystorePath,
    keystorePassword = config.keystorePassword
  )
}
