package com.benkio.botDB.db

import java.io.File
import com.benkio.botDB.config.ShowConfig
import com.benkio.botDB.show.ShowSource
import com.benkio.botDB.show.ShowFetcher
import log.effect.LogWriter
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.db.DBMediaData
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource.given
import cats.effect.Resource
import cats.effect.kernel.Async
import cats.implicits.*
import cats.effect.implicits.*
import com.benkio.botDB.config.Config
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
  def apply[F[_]: Async: LogWriter](
      cfg: Config,
      dbLayer: DBLayer[F],
      resourceAccess: ResourceAccess[F],
      migrator: DBMigrator[F],
      showFetcher: ShowFetcher[F]
  ): BotDBController[F] =
    new BotDBControllerImpl(
      cfg = cfg,
      dbLayer = dbLayer,
      resourceAccess = resourceAccess,
      migrator = migrator,
      showFetcher = showFetcher
    )

  private class BotDBControllerImpl[F[_]: Async: LogWriter](
      cfg: Config,
      dbLayer: DBLayer[F],
      resourceAccess: ResourceAccess[F],
      migrator: DBMigrator[F],
      showFetcher: ShowFetcher[F]
  ) extends BotDBController[F] {
    override def build: Resource[F, Unit] = for {
      _ <- Resource.eval(migrator.migrate(cfg))
      _ <- populateMediaTable
    } yield ()

    private def showFetching(showConfig: ShowConfig): F[Unit] =
      if showConfig.runShowFetching
      then
        showConfig.showSources
          .parTraverse(ms =>
            for
              showSource <- ShowSource(ms.url, ms.botName, ms.outputFilePath)
              _ <- if showConfig.dryRun then Async[F].delay(File(ms.outputFilePath).delete).void else Async[F].unit
              dbShowDatas <- showFetcher.generateShowJson(showSource)
            yield dbShowDatas
          )
          .flatMap(
            _.flatten.traverse_(dbShowData =>
              LogWriter.info(
                s"Inserted show ${dbShowData.show_title} of url ${dbShowData.show_url}, successfully"
              ) >> dbLayer.dbShow.insertShow(dbShowData)
            )
          )
      else Async[F].unit

    override def populateMediaTable: Resource[F, Unit] = for {
      allFiles <- cfg.jsonLocation.flatTraverse(resourceAccess.getResourcesByKind)
      _ <- Resource.eval(LogWriter.info(s"[BotDBController]: all files from ${cfg.jsonLocation}: ${allFiles.length}"))
      jsons = allFiles.mapFilter(mediaSource => mediaSource.getMediaResourceFile.filter(_.getName.endsWith("json")))
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
                  mime_type = i.mime,
                  media_sources = i.sources.asJson.noSpaces,
                  media_count = 0,
                  created_at = Instant.now().getEpochSecond.toString
                )
              )
            _ <- LogWriter.info(s"Inserted file ${i.filename} of kinds ${i.kinds} from ${i.sources}, successfully")
          } yield ()
        )
      )
      _ <- Resource.eval(showFetching(cfg.showConfig))
    } yield ()
  }
}
