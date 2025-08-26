package com.benkio.botDB.media

import cats.effect.kernel.Async
import cats.effect.Resource
import cats.implicits.*
import cats.Show
import com.benkio.botDB.config.Config
import com.benkio.telegrambotinfrastructure.model.media.getMediaResourceFile
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource
import com.benkio.telegrambotinfrastructure.model.media.MediaFileSource.given
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.MimeType
import com.benkio.telegrambotinfrastructure.model.MimeTypeOps.given
import com.benkio.telegrambotinfrastructure.repository.db.DBLayer
import com.benkio.telegrambotinfrastructure.repository.db.DBMediaData
import com.benkio.telegrambotinfrastructure.repository.Repository
import io.circe.parser.decode
import io.circe.syntax.*
import log.effect.LogWriter

import java.io.File
import java.time.Instant
import scala.io.Source

sealed trait MediaUpdater[F[_]] {
  def updateMedia: Resource[F, Unit]
}

object MediaUpdater {
  def apply[F[_]: Async: LogWriter](
      config: Config,
      dbLayer: DBLayer[F],
      repository: Repository[F]
  ): MediaUpdater[F] =
    new MediaUpdaterImpl(
      config = config,
      dbLayer = dbLayer,
      repository = repository
    )

  private[media] class MediaUpdaterImpl[F[_]: Async: LogWriter](
      config: Config,
      dbLayer: DBLayer[F],
      repository: Repository[F]
  ) extends MediaUpdater[F] {

    private[media] def fetchRootBotFiles: Resource[F, List[MediaResource[F]]] =
      config.jsonLocation.flatTraverse(location => repository.getResourcesByKind(location).map(_.reduce.toList))

    private[media] def filterMediaJsonFiles(allFiles: List[MediaResource[F]]): Resource[F, List[File]] = {
      allFiles
        .mapFilter(_.getMediaResourceFile)
        .traverseFilter(resourceFile =>
          resourceFile.map(f => if f.getName.endsWith("_list.json") then Some(f) else None)
        )
    }

    private[media] def parseMediaJsonFiles(jsons: List[File]): Resource[F, List[MediaFileSource]] = {
      Resource.eval(Async[F].fromEither(jsons.flatTraverse(json => {
        val fileContent = Source.fromFile(json).getLines().mkString("\n")
        decode[List[MediaFileSource]](fileContent).leftMap(e => Throwable(e.show))
      })))
    }

    private[media] def insertMedia(i: MediaFileSource) = {
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
          s"[MediaUpdater] ✓💾 ${i.filename}"
        )
      } yield ()
    }

    override def updateMedia: Resource[F, Unit] = for {
      allFiles <- fetchRootBotFiles
      _        <- Resource.eval(
        LogWriter.info(s"[MediaUpdater]: all files from ${config.jsonLocation}: ${allFiles.length}")
      )
      jsons <- filterMediaJsonFiles(allFiles)
      _     <- Resource.eval(LogWriter.info(s"[MediaUpdater]: Json file to be computed: $jsons"))
      input <- parseMediaJsonFiles(jsons)
      _     <- Resource.eval(LogWriter.info(s"[MediaUpdater]: Media to be added: ${input.length}"))
      _     <- Resource.eval(input.traverse_(insertMedia(_)))
    } yield ()
  }
}
