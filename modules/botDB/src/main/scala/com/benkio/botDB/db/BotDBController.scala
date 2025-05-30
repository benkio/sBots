package com.benkio.botDB.db

import cats.effect.kernel.Async
import cats.effect.Resource
import cats.implicits.*
import cats.Show
import com.benkio.botDB.config.Config
import com.benkio.botDB.config.ShowConfig
//import com.benkio.botDB.show.ShowFetcher
import com.benkio.telegrambotinfrastructure.model.media.getMediaResourceFile
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource.given
import com.benkio.telegrambotinfrastructure.model.MimeType
import com.benkio.telegrambotinfrastructure.model.MimeTypeOps.given
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import io.circe.parser.decode
import io.circe.syntax.*
import log.effect.LogWriter

import java.time.Instant
import scala.io.Source

sealed trait BotDBController[F[_]] {
  def build: Resource[F, Unit]

  def populateMediaTable: Resource[F, Unit]
}

object BotDBController {
  def apply[F[_]: Async: LogWriter](
      cfg: Config,
      dbLayer: DBLayer[F],
      resourceAccess: ResourceAccess[F],
      migrator: DBMigrator[F]
      // showFetcher: ShowFetcher[F]
  ): BotDBController[F] =
    new BotDBControllerImpl(
      cfg = cfg,
      dbLayer = dbLayer,
      resourceAccess = resourceAccess,
      migrator = migrator
      // showFetcher = showFetcher
    )

  private class BotDBControllerImpl[F[_]: Async: LogWriter](
      cfg: Config,
      dbLayer: DBLayer[F],
      resourceAccess: ResourceAccess[F],
      migrator: DBMigrator[F]
      // showFetcher: ShowFetcher[F]
  ) extends BotDBController[F] {
    override def build: Resource[F, Unit] = for {
      _ <- Resource.eval(migrator.migrate(cfg))
      _ <- populateMediaTable
      _ <- Resource.eval(showFetching(cfg.showConfig))
    } yield ()

    private def showFetching(showConfig: ShowConfig): F[Unit] = ???

    override def populateMediaTable: Resource[F, Unit] = for {
      allFiles <- cfg.jsonLocation.flatTraverse(location =>
        resourceAccess.getResourcesByKind(location).map(_.reduce.toList)
      )
      _ <- Resource.eval(
        LogWriter.info(s"[BotDBController]: all files from ${cfg.jsonLocation}: ${allFiles.length}")
      )
      jsons <- allFiles
        .mapFilter(_.getMediaResourceFile)
        .traverseFilter(resourceFile =>
          resourceFile.map(f => if f.getName.endsWith("_list.json") then Some(f) else None)
        )
      _ <- Resource.eval(LogWriter.info(s"[BotDBController]: Json file to be computed: $jsons"))
      input <- Resource.eval(Async[F].fromEither(jsons.flatTraverse(json => {
        val fileContent = Source.fromFile(json).getLines().mkString("\n")
        decode[List[MediaFileSource]](fileContent).leftMap(e => Throwable(e.show))
      })))
      _ <- Resource.eval(
        input.traverse_(i =>
          for {
            _ <- dbLayer.dbMedia
              .insertMedia(
                DBMediaData(
                  media_name = i.filename,
                  kinds = i.kinds.asJson.noSpaces,
                  mime_type = Show[MimeType].show(i.mime),
                  media_sources = i.sources.asJson.noSpaces,
                  media_count = 0,
                  created_at = Instant.now().getEpochSecond.toString
                )
              )
            _ <- LogWriter.info(
              s"[BotDBController] Inserted file ${i.filename} of kinds ${i.kinds} from ${i.sources}, successfully"
            )
          } yield ()
        )
      )
    } yield ()
  }
}
