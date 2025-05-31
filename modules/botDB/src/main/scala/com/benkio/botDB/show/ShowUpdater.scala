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
      cfg: Config,
      dbLayer: DBLayer[F],
      resourceAccess: ResourceAccess[F],
      youtubeApiKey: String
  ): ShowUpdater[F] = ShowUpdaterImpl[F](
    cfg = cfg,
    dbLayer = dbLayer,
    resourceAccess = resourceAccess,
    youtubeApiKey = youtubeApiKey
  )

  private class ShowUpdaterImpl[
      F[_]: Async: LogWriter
  ](
      cfg: Config,
      dbLayer: DBLayer[F],
      resourceAccess: ResourceAccess[F],
      youtubeApiKey: String
  ) extends ShowUpdater[F] {
    override def updateShow: Resource[F, Unit] = {
      val _ = (dbLayer, resourceAccess, youtubeApiKey)
      val program = for {
        _              <- LogWriter.info("[ShowUpdater] Creating Youtube Service")
        youTubeService <- YoutubeService(cfg.showConfig.applicationName)
        _              <- LogWriter.info("[ShowUpdater] Fetching online show Ids")
        _              <- LogWriter.info("[ShowUpdater] Fetching stored Ids")
        // TODO: Check if the output needs to be cancelled from the config
        _ <- LogWriter.info("[ShowUpdater] $numberOfIds Ids to be added")
        _ <- LogWriter.info("[ShowUpdater] Fetching data from Ids")
        _ <- LogWriter.info("[ShowUpdater] Converting Youtube data to DBMediaData")
        _ <- LogWriter.info("[ShowUpdater] Insert DBMediaData to DB")
      } yield ()
      if cfg.showConfig.runShowFetching
      then Resource.eval(program)
      else Resource.eval(LogWriter.info("[ShowUpdater] Option runShowFetching = true. No run"))
    }

  }
}
