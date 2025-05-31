package com.benkio.botDB.show

import cats.effect.kernel.Async
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.botDB.config.Config
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import log.effect.LogWriter

trait ShowUpdater[F[_]]:
  def updateShow: Resource[F, Unit]

object ShowUpdater {
  def apply[
      F[_]: Async: LogWriter
  ](
      config: Config,
      dbLayer: DBLayer[F],
      resourceAccess: ResourceAccess[F],
      youTubeApiKey: String
  ): ShowUpdater[F] = ShowUpdaterImpl[F](
    config = config,
    dbLayer = dbLayer,
    resourceAccess = resourceAccess,
    youTubeApiKey = youTubeApiKey
  )

  private class ShowUpdaterImpl[
      F[_]: Async: LogWriter
  ](
      config: Config,
      dbLayer: DBLayer[F],
      resourceAccess: ResourceAccess[F],
      youTubeApiKey: String
  ) extends ShowUpdater[F] {
    override def updateShow: Resource[F, Unit] = {
      val _ = (dbLayer, resourceAccess, youTubeApiKey)
      val program = for {
        _              <- LogWriter.info("[ShowUpdater] Creating YouTube Service")
        youTubeService <- YouTubeService(config = config, youTubeApiKey)
        _              <- LogWriter.info("[ShowUpdater] Fetching online show Ids")
        allIds <- youTubeService.getAllIds
        _              <- LogWriter.info("[ShowUpdater] Fetched ${allIds.length} Ids")
        _              <- LogWriter.info("[ShowUpdater] Fetching stored Ids")
        // TODO: Check if the output needs to be cancelled from the config
        _ <- LogWriter.info("[ShowUpdater] $numberOfIds Ids to be added")
        _ <- LogWriter.info("[ShowUpdater] Fetching data from Ids")
        _ <- LogWriter.info("[ShowUpdater] Converting YouTube data to DBMediaData")
        _ <- LogWriter.info("[ShowUpdater] Insert DBMediaData to DB")
      } yield ()
      if config.showConfig.runShowFetching
      then Resource.eval(program)
      else Resource.eval(LogWriter.info("[ShowUpdater] Option runShowFetching = true. No run"))
    }

  }
}
