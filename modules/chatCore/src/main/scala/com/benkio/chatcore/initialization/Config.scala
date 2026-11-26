package com.benkio.chatcore.initialization

import cats.*
import cats.implicits.*
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.config.StartupCheckConfig
import com.benkio.chatcore.PagedListTextStore
import com.benkio.chatcore.SubscriptionDraftStore
import log.effect.LogWriter
import pureconfig.*

import scala.concurrent.duration.*

final case class Config(
    db: DBConfig,
    httpCache: HttpClientsCacheConfig,
    requestProcessingTimeout: Option[FiniteDuration] = Some(SBotConfig.defaultRequestProcessingTimeout),
    startupCheck: StartupCheckConfig = StartupCheckConfig(),
    pagedListTextModeExpiry: FiniteDuration = PagedListTextStore.defaultExpiry,
    pagedListTextModeMaxChats: Int = PagedListTextStore.defaultMaxChats,
    subscriptionDraftExpiry: FiniteDuration = SubscriptionDraftStore.defaultExpiry,
    subscriptionDraftMaxDrafts: Int = SubscriptionDraftStore.defaultMaxDrafts
) derives ConfigReader
final case class DBConfig(
    driver: String,
    dbName: String,
    url: String
) derives ConfigReader
final case class HttpClientsCacheConfig(
    dropbox: HttpCacheConfig = HttpCacheConfig()
) derives ConfigReader
final case class HttpCacheConfig(
    ttlSeconds: Long = 21600L,
    maxSize: Int = 20
) derives ConfigReader {
  val ttl: FiniteDuration = ttlSeconds.seconds
}

object Config {

  // Try to load the config from normal application.conf and from main application.conf if fails
  def loadConfig[F[_]: MonadThrow](namespace: String)(using log: LogWriter[F]): F[Config] =
    loadConfigInternal("main." + namespace).handleErrorWith(err =>
      log.error(
        s"An error occurred loading the $namespace configuration from main. Ignore if run thorugh single bot: ${err.getMessage()}"
      ) >>
        loadConfigInternal[F](namespace)
    )

  private def loadConfigInternal[F[_]](namespace: String)(using monadThrow: MonadThrow[F]): F[Config] =
    monadThrow.fromEither(
      ConfigSource.default
        .at(namespace)
        .load[Config]
        .leftMap(err => new RuntimeException(err.prettyPrint()))
    )
}
