package com.benkio.botDB.show

import cats.effect.Resource
import com.benkio.botDB.config.Config
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
//import cats.effect.kernel.Async
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
//import log.effect.LogWriter

trait ShowUpdater[F[_]]:
  def updateShow: Resource[F, List[DBShowData]]

object ShowUpdater {
  def apply[
      F[_] // : Async: LogWriter
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
      F[_] // : Async: LogWriter
  ](
      cfg: Config,
      dbLayer: DBLayer[F],
      resourceAccess: ResourceAccess[F],
      youtubeApiKey: String
  ) extends ShowUpdater[F] {
    override def updateShow: Resource[F, List[DBShowData]] =
      val _ = (cfg, dbLayer, resourceAccess, youtubeApiKey)
      ???

  }
}
