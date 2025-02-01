package com.benkio.botDB.db

import log.effect.LogWriter
import com.benkio.telegrambotinfrastructure.resources.db.DBMedia
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource.given
import cats.effect.Resource
import cats.effect.Sync
import cats.implicits.*
import com.benkio.botDB.Config
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import io.circe.parser.decode
import io.circe.syntax.*
import com.benkio.telegrambotinfrastructure.model.media.getMediaResourceFile

import java.time.Instant
import scala.io.Source

sealed trait BotDBController[F[_]] {
  def build: Resource[F, Unit]

  def populateMediaTable: Resource[F, Unit]
}

object BotDBController {
  def apply[F[_]: Sync: LogWriter](
      cfg: Config,
      databaseRepository: DBMedia[F],
      resourceAccess: ResourceAccess[F],
      migrator: DBMigrator[F]
  ): BotDBController[F] =
    new BotDBControllerImpl(
      cfg = cfg,
      databaseRepository = databaseRepository,
      resourceAccess = resourceAccess,
      migrator = migrator
    )

  private class BotDBControllerImpl[F[_]: Sync: LogWriter](
      cfg: Config,
      databaseRepository: DBMedia[F],
      resourceAccess: ResourceAccess[F],
      migrator: DBMigrator[F]
  ) extends BotDBController[F] {
    override def build: Resource[F, Unit] = for {
      _ <- Resource.eval(migrator.migrate(cfg))
      _ <- populateMediaTable
    } yield ()

    override def populateMediaTable: Resource[F, Unit] = for {
      allFiles <- cfg.jsonLocation.flatTraverse(resourceAccess.getResourcesByKind)
      _ <- Resource.eval(LogWriter.info(s"[BotDBController]: all files from ${cfg.jsonLocation}: ${allFiles.length}"))
      jsons = allFiles.mapFilter(mediaSource => mediaSource.getMediaResourceFile.filter(_.getName.endsWith("json")))
      _ <- Resource.eval(LogWriter.info(s"[BotDBController]: Json file to be computed: $jsons"))
      input <- Resource.eval(Sync[F].fromEither(jsons.flatTraverse(json => {
        val fileContent = Source.fromFile(json).getLines().mkString("\n")
        decode[List[MediaFileSource]](fileContent).leftMap(e => Throwable(e.show))
      })))
      _ <- Resource.eval(
        input.traverse_(i =>
          for {
            _ <- databaseRepository
              .insertMedia(
                DBMediaData(
                  media_name = i.filename,
                  kinds = i.kinds.map(_.asJson.toString),
                  mime_type = DBMediaData.mimeTypeOrDefault(i.filename, i.mime),
                  media_sources = i.sources.asJson.toString,
                  media_count = 0,
                  created_at = Instant.now().getEpochSecond.toString
                )
              )
            _ <- LogWriter.info(s"Inserted file ${i.filename} of kinds ${i.kinds} from ${i.sources}, successfully")
          } yield ()
        )
      )
    } yield ()
  }
}
