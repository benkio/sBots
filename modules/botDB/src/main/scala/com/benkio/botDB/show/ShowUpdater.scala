package com.benkio.botDB.show

import cats.effect.kernel.Async
import cats.effect.Resource
import cats.implicits.*
import com.benkio.botDB.config.Config
import com.benkio.telegrambotinfrastructure.model.media.MediaResource
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import log.effect.LogWriter

import java.io.File
import scala.io.Source

trait ShowUpdater[F[_]]:
  def updateShow: Resource[F, Unit]

object ShowUpdater {

  final case class FailedToOpenFile(mediaFile: MediaFile)
      extends Throwable(s"[ShowUpdater] Unable to open file $mediaFile")

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
        candidateIds   <- youTubeService.getAllBotNameIds
        _              <- LogWriter.info(s"[ShowUpdater] Fetched ${candidateIds.values.flatten.size} Ids")
        _              <- LogWriter.info("[ShowUpdater] Fetching stored Ids")
        storedIds      <- getStoredIds
        newIds = candidateIds.map { case (botName, ids) => (botName, ids.filterNot(id => storedIds.contains(id))) }
        _      <- LogWriter.info(s"[ShowUpdater] ${newIds.values.flatten.size} Ids to be added")
        _      <- LogWriter.info("[ShowUpdater] Fetching data from Ids")
        videos <- candidateIds.unorderedTraverse(ids => youTubeService.getYouTubeVideos(ids))
        _      <- LogWriter.info("[ShowUpdater] Converting YouTube data to DBMediaData")
        _      <- LogWriter.info("[ShowUpdater] Insert DBMediaData to DB")
      } yield ()
      if config.showConfig.runShowFetching
      then Resource.eval(program)
      else Resource.eval(LogWriter.info("[ShowUpdater] Option runShowFetching = true. No run"))
    }

    private def getStoredIds: F[List[String]] = {
      val showFilesResource: Resource[F, List[File]] =
        config.showConfig.showSources
          .map(showSource => MediaFile.fromString(showSource.outputFilePath))
          .traverse(mediaFile =>
            resourceAccess
              .getResourceFile(mediaFile)
              .flatMap(resourceFiles =>
                resourceFiles.head match {
                  case MediaResource.MediaResourceFile(resourceFile) => resourceFile
                  case _ => Resource.eval(Async[F].raiseError[File](FailedToOpenFile(mediaFile)))
                }
              )
          )
      val deleteFiles: F[List[String]] =
        showFilesResource.use(showFiles =>
          for {
            _ <- LogWriter.info(s"[ShowUpdater] ✓ Dry Run. Delete show files: $showFiles")
            _ <- showFiles.traverse(showFile => Async[F].delay(showFile.delete))
          } yield List.empty
        )
      val getStoredFilesShowIds: F[List[String]] =
        showFilesResource.use(showFiles =>
          LogWriter.info("[ShowUpdater] ❌ Dry Run. read show files: $showFiles") >>
            showFiles.flatTraverse(showFile =>
              for {
                _                   <- LogWriter.info(s"[ShowUpdater] Parse show file content: $showFile")
                showFileContentJson <- Async[F].fromEither(parse(Source.fromFile(showFile).mkString))
                showFileIdsJson = showFileContentJson.findAllByKey("show_url")
                showFileIds <- Async[F].fromEither(showFileIdsJson.traverse(_.as[String]))
              } yield showFileIds
            )
        )
      if config.showConfig.dryRun
      then deleteFiles
      else getStoredFilesShowIds
    }
  }
}
