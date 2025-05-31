package com.benkio.botDB.show

import cats.effect.kernel.Async
import cats.effect.Resource
import cats.syntax.all.*
import com.benkio.botDB.config.Config
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
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
      // check in the config if we need to fetch data.
      for {
        _ <- LogWriter.info(s"[ShowUpdater] Creating Youtube Service")
        _ <- LogWriter.info(s"[ShowUpdater] Fetching online show Ids")
        _ <- LogWriter.info(s"[ShowUpdater] Fetching stored Ids")
        // TODO: Check if the output needs to be cancelled from the config
        _ <- LogWriter.info("[ShowUpdater] $numberOfIds Ids to be added")
        _ <- LogWriter.info(s"[ShowUpdater] Fetching data from Ids")
        _ <- LogWriter.info(s"[ShowUpdater] Converting Youtube data to DBMediaData")
        _ <- LogWriter.info(s"[ShowUpdater] Insert DBMediaData to DB")
      } yield ???
      ???
    }

  }
}
