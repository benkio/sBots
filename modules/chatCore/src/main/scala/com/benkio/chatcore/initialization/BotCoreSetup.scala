package com.benkio.chatcore.initialization

import cats.effect.Async
import cats.effect.Resource
import com.benkio.chatcore.config.SBotConfig
import com.benkio.chatcore.http.DropboxClient
import com.benkio.chatcore.http.HttpClients
import com.benkio.chatcore.http.MegaClient
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.db.DBRepository
import com.benkio.chatcore.repository.JsonDataRepository
import com.benkio.chatcore.repository.Repository
import doobie.util.transactor.Transactor
import log.effect.LogWriter
import org.http4s.client.Client

/** What a bot needs whatever the platform it runs on: its configuration, the DB with the data of its replies and the
  * repository of its media.
  */
final case class BotCoreSetup[F[_]](
    jsonDataRepository: JsonDataRepository[F],
    dbLayer: DBLayer[F],
    repository: Repository[F]
)

object BotCoreSetup {

  def loadDB[F[_]: Async](config: DBConfig)(using log: LogWriter[F]): Resource[F, DBLayer[F]] =
    Resource.eval(
      DBLayer[F](
        Transactor.fromDriverManager[F](
          config.driver,
          config.url,
          "",
          "",
          None
        )
      )
    )

  def apply[F[_]: Async](httpClient: Client[F], sBotConfig: SBotConfig)(using
      log: LogWriter[F]
  ): Resource[F, BotCoreSetup[F]] =
    for {
      config        <- Resource.eval(Config.loadConfig[F](sBotConfig.sBotInfo.botId.value))
      _             <- Resource.eval(log.info(s"[${sBotConfig.sBotInfo.botId}] Configuration: $config"))
      dropboxClient <- Resource.eval(DropboxClient[F](httpClient, config.httpCache.dropbox))
      megaClient    <- Resource.eval(MegaClient[F](httpClient, config.httpCache.mega))
      clients = HttpClients(dropboxClient = dropboxClient, megaClient = megaClient)
      jsonDataRepository = JsonDataRepository[F]()
      dbLayer <- loadDB[F](config.db).evalMap(DBLayer.loadReplies[F](_, jsonDataRepository, sBotConfig))
    } yield BotCoreSetup(
      jsonDataRepository = jsonDataRepository,
      dbLayer = dbLayer,
      repository = DBRepository.dbResources[F](dbLayer.dbMedia, clients)
    )
}
