package com.benkio.botDB.show

import cats.effect.kernel.Async
import cats.effect.Resource
import cats.implicits.*
import com.benkio.botDB.config.Config
import com.benkio.telegrambotinfrastructure.model.reply.MediaFile
import com.benkio.telegrambotinfrastructure.resources.db.DBLayer
import com.benkio.telegrambotinfrastructure.resources.db.DBShowData
import com.google.api.services.youtube.model.Video
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import log.effect.LogWriter

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import scala.io.Source
import scala.util.Try

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
      youTubeService: YouTubeService[F]
  ): ShowUpdater[F] = ShowUpdaterImpl[F](
    config = config,
    dbLayer = dbLayer,
    youTubeService = youTubeService
  )

  private[show] def filterCandidateIds(
      candidateIds: List[YouTubeBotIds],
      storedIds: List[String]
  ): List[YouTubeBotIds] = {
    candidateIds.mapFilter(youTubeBotIds =>
      val result = youTubeBotIds.copy(videoIds = youTubeBotIds.videoIds.filterNot(id => storedIds.contains(id)))
      if result.videoIds.isEmpty then None else Some(result)
    )
  }

  private[show] class ShowUpdaterImpl[
      F[_]: Async: LogWriter
  ](
      config: Config,
      dbLayer: DBLayer[F],
      youTubeService: YouTubeService[F]
  ) extends ShowUpdater[F] {

    private[show] def youTubeBotIdsToVideos(
        youTubeBotIds: List[YouTubeBotIds]
    ): F[List[YouTubeBotVideos]] = {
      youTubeBotIds
        .traverse(youTubeBotIds =>
          youTubeService
            .getYouTubeVideos(youTubeBotIds.videoIds)
            .map(youTubeBotVideos =>
              YouTubeBotVideos(youTubeBotIds.botName, youTubeBotIds.outputFilePath, youTubeBotVideos)
            )
        )
    }

    def youTubeBotVideosToDbShowDatas(youTubeBotVideos: List[YouTubeBotVideos]): F[List[YouTubeBotDBShowDatas]] = {
      youTubeBotVideos.traverse(youTubeBotVideos =>
        youTubeBotVideos.videos
          .traverse(videoToDBShowData(_, youTubeBotVideos.botName))
          .map((dBShowDatas: List[Option[DBShowData]]) =>
            YouTubeBotDBShowDatas(
              youTubeBotVideos.botName,
              youTubeBotVideos.outputFilePath,
              dBShowDatas.flatMap(_.toList)
            )
          )
      )
    }

    private[show] def insertDBShowDatas(
        youTubeBotdbShowDatas: List[YouTubeBotDBShowDatas],
        storedDBShowDatas: List[DBShowData]
    ): F[Unit] = {
      val inputDbShowData = youTubeBotdbShowDatas
        .flatMap { case YouTubeBotDBShowDatas(_, _, dbShowDatas) => dbShowDatas } ++ storedDBShowDatas

      inputDbShowData.traverse_(show =>
        LogWriter.info(s"[ShowUpdater] ✓💾 ${show.show_title}") >> dbLayer.dbShow.insertShow(show)
      )
    }

    override def updateShow: Resource[F, Unit] = {
      val program = for {
        _                 <- LogWriter.info("[ShowUpdater] Fetching online show Ids")
        candidateIds      <- youTubeService.getAllBotNameIds
        _                 <- LogWriter.info(s"[ShowUpdater] Fetched ${candidateIds.flatMap(_.videoIds).length} Ids")
        _                 <- LogWriter.info("[ShowUpdater] Fetching stored DbShowData")
        storedDbShowDatas <- getStoredDbShowDatas
        _                 <- LogWriter.info(s"[ShowUpdater] ${storedDbShowDatas.length} stored DbShowData")
        youTubeBotIds = filterCandidateIds(candidateIds, storedDbShowDatas.map(_.show_id))
        _                <- LogWriter.info(s"[ShowUpdater] ${youTubeBotIds.flatMap(_.videoIds).length} Ids to be added")
        _                <- LogWriter.debug(s"[ShowUpdater] Data to be added: ${youTubeBotIds}")
        _                <- LogWriter.info("[ShowUpdater] Fetching data from Ids")
        youTubeBotVideos <- youTubeBotIdsToVideos(youTubeBotIds)
        _ <- LogWriter.info(s"[ShowUpdater] ${youTubeBotVideos.length} Converting YouTube data to DBShowData")
        _ <- LogWriter.debug(s"[ShowUpdater] Data to be converted: ${youTubeBotVideos}")
        youTubeBotdbShowDatas <- youTubeBotVideosToDbShowDatas(youTubeBotVideos)
        totalShows = storedDbShowDatas.length + youTubeBotdbShowDatas.flatMap(_.dbShowDatas).length
        _ <- LogWriter.info(s"[ShowUpdater] Insert $totalShows DBShowDatas to DB")
        _ <- LogWriter.debug(s"[ShowUpdater] Data to be intserted: $youTubeBotdbShowDatas")
        _ <- insertDBShowDatas(youTubeBotdbShowDatas, storedDbShowDatas)
        _ <- LogWriter.info("[ShowUpdater] Save DBShowDatas to project Jsons")
        _ <- youTubeBotdbShowDatas.traverse_(youTubeBotdbShowData =>
          updateStoredJsons(youTubeBotdbShowData.outputFilePath, youTubeBotdbShowData.dbShowDatas)
        )
      } yield ()
      if config.showConfig.runShowFetching
      then Resource.eval(program)
      else Resource.eval(LogWriter.info("[ShowUpdater] Option runShowFetching = true. No run"))
    }

    private[show] def getStoredDbShowDatas: F[List[DBShowData]] = {
      val showFilesResource: Resource[F, List[File]] =
        config.showConfig.showSources
          .traverse(showSource =>
            Resource.make(Async[F].delay(File(showSource.outputFilePath)))(f =>
              LogWriter.info(s"[ShowUpdater] Closing file $f")
            )
          )
      val deleteFiles: F[List[DBShowData]] =
        showFilesResource.use(showFiles =>
          for {
            _ <- LogWriter.info(s"[ShowUpdater] ✓ Dry Run. Delete show files: $showFiles")
            _ <- showFiles.traverse(showFile => Async[F].delay(showFile.delete))
          } yield List.empty
        )
      val getStoredDbShowDatas: F[List[DBShowData]] =
        showFilesResource.use(showFiles =>
          LogWriter.info("[ShowUpdater] ❌ Dry Run. read show files: $showFiles") >>
            showFiles.flatTraverse(showFile =>
              for {
                _                   <- LogWriter.info(s"[ShowUpdater] Parse show file content: $showFile")
                showFileContentJson <- Async[F].fromEither(parse(Source.fromFile(showFile).mkString))
                dbShowDatas         <- Async[F].fromEither(showFileContentJson.as[List[DBShowData]])
              } yield dbShowDatas
            )
        )
      if config.showConfig.dryRun
      then deleteFiles
      else getStoredDbShowDatas
    }

    private[show] def videoToDBShowData(video: Video, botName: String): F[Option[DBShowData]] = {
      def durationISO8601ToSeconds(isoDuration: String): Int = {
        val duration = Duration.parse(isoDuration)
        duration.getSeconds.toInt
      }
      val maybeDBShowData = for {
        id         <- Option(video.getId())
        title      <- Option(video.getSnippet().getTitle())
        uploadDate <- Option(video.getSnippet().getPublishedAt().toStringRfc3339())
        duration   <- Option(video.getContentDetails().getDuration())
      } yield DBShowData(
        show_id = id,
        bot_name = botName,
        show_title = title,
        show_upload_date = uploadDate,
        show_duration = durationISO8601ToSeconds(duration),
        show_description = Option(video.getSnippet().getDescription()),
        show_is_live = Option(video.getLiveStreamingDetails()).isDefined,
        show_origin_automatic_caption_id = None, // TODO: #730 add caption id
        show_origin_automatic_caption = None     // TODO: #730 add caption data
      )
      maybeDBShowData.fold(
        LogWriter.error(s"[PlaygroundMain] ERROR: $botName Video conversion problem for $video") *> None.pure[F]
      )(
        _.some.pure[F]
      )
    }

    private[show] def updateStoredJsons(outputFilePath: String, dbShowDatas: List[DBShowData]): F[Unit] = {
      def overwriteOutputFileContent(content: String) =
        Async[F]
          .fromTry(
            Try(
              Files.write(Paths.get(outputFilePath), content.getBytes(StandardCharsets.UTF_8))
            )
          )
          .as(())
      val appendOutputFileContent = for {
        fileContent <- Async[F].fromTry(Try(String(Files.readAllBytes(Paths.get(outputFilePath)))))
        storedShows <- Async[F].fromEither(decode[List[DBShowData]](fileContent))
        resultContent = dbShowDatas ++ storedShows
        _ <- overwriteOutputFileContent(resultContent.asJson.spaces2)
      } yield ()
      val updateComputation =
        if config.showConfig.dryRun
        then overwriteOutputFileContent(dbShowDatas.asJson.spaces2)
        else appendOutputFileContent
      updateComputation
    }
  }
}
