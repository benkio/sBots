package com.benkio.main

import telegramium.bots.InputPartFile
import java.io.File
import com.benkio.telegrambotinfrastructure.initialization.BotSetup
import cats.MonadThrow
import cats.effect.Async
import cats.effect.Resource
import cats.implicits._
import com.benkio.telegrambotinfrastructure.BackgroundJobManager
import com.benkio.telegrambotinfrastructure.default.Actions
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.web.UrlFetcher
import doobie.Transactor
import log.effect.LogWriter
import org.http4s.client.Client
import org.http4s.Status
import org.http4s.Uri
import org.http4s._
import telegramium.bots.high.Api
import telegramium.bots.high.BotApi
import com.benkio.main.Config
import org.http4s.ember.client._

final case class MainSetup[F[_]](
    httpClient: Client[F],
    dbLayer: DBLayer[F],
  webhookBaseUrl: String,
  host: String,
  port: Int,
  webhookCertificate: Option[InputPartFile]
)

object MainSetup {

  def apply[F[_]: Async]()(implicit log: LogWriter[F]): Resource[F, MainSetup[F]] = for {
    config <- Resource.eval(Config.loadConfig[F])
    _      <- Resource.eval(log.info(s"[Main] Configuration: $config"))
    httpClient  <- EmberClientBuilder.default[F].withMaxResponseHeaderSize(8192).build
    dbLayer    <- BotSetup.loadDB[F](config.mainDB)
    certificate <- Resource.eval(Async[F].pure(config.webhookCertificate.map(fp => InputPartFile(new File(fp)))))
  } yield MainSetup(
    httpClient = httpClient,
    dbLayer = dbLayer,
    webhookBaseUrl = config.webhookBaseUrl,
    host = config.hostUrl,
    port = config.port,
    webhookCertificate = certificate
  )
}
