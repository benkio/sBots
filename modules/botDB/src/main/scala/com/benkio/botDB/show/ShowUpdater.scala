package com.benkio.botDB.show

import cats.effect.kernel.Async
import cats.effect.Resource
import cats.implicits.*
import cats.Semigroup
import com.benkio.botDB.config.Config
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.repository.db.DBLayer
import com.benkio.chatcore.repository.db.DBShowData
import com.google.api.services.youtube.model.Video
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import log.effect.LogWriter

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import scala.io.Source
import scala.sys.process.*
import scala.util.Try

trait ShowUpdater[F[_]] {
  def updateShow: Resource[F, Unit]
}

object ShowUpdater {

  final case class FailedToOpenFile(mediaFile: MediaFile)
      extends Throwable(s"[ShowUpdater] Unable to open file $mediaFile")

  def apply[
      F[_]: Async: LogWriter
  ](
      config: Config,
      dbLayer: DBLayer[F],
      youTubeService: YouTubeService[F],
      captionParser: CaptionParser[F]
  ): ShowUpdater[F] = ShowUpdaterImpl[F](
    config = config,
    dbLayer = dbLayer,
    youTubeService = youTubeService,
    captionParser = captionParser
  )

  private[show] def mergeShowDatas(
      storedDbShowDatas: List[YouTubeBotDBShowDatas],
      youTubeDbShowDatas: List[YouTubeBotDBShowDatas]
  ): List[YouTubeBotDBShowDatas] = {
    storedDbShowDatas
      .groupMapReduce(_.outputFilePath)(identity)(Semigroup[YouTubeBotDBShowDatas].combine)
      .combine(
        youTubeDbShowDatas.groupMapReduce(_.outputFilePath)(identity)(Semigroup[YouTubeBotDBShowDatas].combine)
      )
      .values
      .toList
  }

  private[show] def filterCandidateIds(
      candidateIds: List[YouTubeBotIds],
      storedIds: List[String]
  ): List[YouTubeBotIds] = {
    candidateIds.mapFilter(youTubeBotIds => {
      val result = youTubeBotIds.copy(videoIds = youTubeBotIds.videoIds.filterNot(id => storedIds.contains(id)))
      if result.videoIds.isEmpty then None else Some(result)
    })
  }

  private[show] class ShowUpdaterImpl[
      F[_]: Async: LogWriter
  ](
      config: Config,
      dbLayer: DBLayer[F],
      youTubeService: YouTubeService[F],
      captionParser: CaptionParser[F]
  ) extends ShowUpdater[F] {

    private[show] def youTubeBotIdsToVideos(
        youTubeBotIds: List[YouTubeBotIds]
    ): F[List[YouTubeBotVideos]] = {
      youTubeBotIds
        .traverse(youTubeBotIds =>
          youTubeService
            .getYouTubeVideos(youTubeBotIds.videoIds)
            .map(youTubeBotVideos =>
              YouTubeBotVideos(
                botId = youTubeBotIds.botId,
                outputFilePath = youTubeBotIds.outputFilePath,
                captionLanguage = youTubeBotIds.captionLanguage,
                captionFolderPath = youTubeBotIds.captionFolderPath,
                videos = youTubeBotVideos
              )
            )
        )
    }

    def youTubeBotVideosToDbShowDatas(youTubeBotVideos: List[YouTubeBotVideos]): F[List[YouTubeBotDBShowDatas]] = {
      youTubeBotVideos.traverse(youTubeBotVideos =>
        youTubeBotVideos.videos
          .traverse(videoToDBShowData(_, youTubeBotVideos.botId))
          .map((dBShowDatas: List[Option[DBShowData]]) =>
            YouTubeBotDBShowDatas(
              botId = youTubeBotVideos.botId,
              outputFilePath = youTubeBotVideos.outputFilePath,
              captionFolderPath = youTubeBotVideos.captionFolderPath,
              captionLanguage = youTubeBotVideos.captionLanguage,
              dbShowDatas = dBShowDatas.flatMap(_.toList)
            )
          )
      )
    }

    private[show] def insertDBShowDatas(
        youTubeBotdbShowDatas: List[YouTubeBotDBShowDatas]
    ): F[Unit] = {
      youTubeBotdbShowDatas
        .traverse_ { case YouTubeBotDBShowDatas(_, _, captionFolderPath, _, dbShowDatas) =>
          dbShowDatas.traverse_(show => {

            val captionFilePath: Path = Path.of(captionFolderPath, s"${show.show_id}.srt")
            for {
              _            <- LogWriter.info(s"[ShowUpdater] Fetch Caption for ${show.show_title}")
              maybeCaption <- captionParser.parsePlainCaptionSrt(captionFilePath)
              showWithCaption = show.copy(show_origin_automatic_caption = maybeCaption)
              _ <- LogWriter.info(s"[ShowUpdater] ✓💾 ${showWithCaption.show_title}")
              _ <- dbLayer.dbShow.insertShow(showWithCaption)
            } yield ()
          })

        }
    }

    override def updateShow: Resource[F, Unit] = {
      val updateShowFromJsons: F[Unit] = for {
        _ <- LogWriter.info(
          s"[ShowUpdater] Option runShowFetching = ${config.showConfig.runShowFetching}. Update Shows from Jsons"
        )
        storedDbShowDatas <- getStoredDbShowDatas
        _                 <- LogWriter.info(s"[ShowUpdater] Insert ${storedDbShowDatas.length} DBShowDatas to DB")
        _                 <- insertDBShowDatas(storedDbShowDatas)
      } yield ()

      val updateShowFromYoutube: F[Unit] = for {
        _            <- LogWriter.info("[ShowUpdater] Fetching online show Ids")
        candidateIds <- youTubeService.getAllBotNameIds.handleErrorWith(e =>
          LogWriter.error(
            s"[ShowUpdater] Error when fetching online show Ids. Continue with just stared data: ${e.getMessage}"
          ) >> List.empty.pure
        )
        _ <- LogWriter.info(
          s"[ShowUpdater] Fetched ${candidateIds.flatMap(_.videoIds).length} Ids. Fetching stored DbShowData"
        )
        storedDbShowDatas <- getStoredDbShowDatas
        youTubeBotIds = filterCandidateIds(candidateIds, storedDbShowDatas.flatMap(_.dbShowDatas.map(_.show_id)))
        _                <- LogWriter.info(s"[ShowUpdater] ${youTubeBotIds.flatMap(_.videoIds).length} Ids to be added")
        _                <- LogWriter.debug(s"[ShowUpdater] Data to be added: ${youTubeBotIds}")
        _                <- LogWriter.info("[ShowUpdater] Fetching data from Ids")
        youTubeBotVideos <- youTubeBotIdsToVideos(youTubeBotIds)
        _ <- LogWriter.info(s"[ShowUpdater] ${youTubeBotVideos.length} Converting YouTube data to DBShowData")
        _ <- LogWriter.debug(s"[ShowUpdater] Data to be converted: ${youTubeBotVideos}")
        youTubeBotdbShowDatas <- youTubeBotVideosToDbShowDatas(youTubeBotVideos)
        totalShowsNoCaption = mergeShowDatas(storedDbShowDatas, youTubeBotdbShowDatas)
        _ <-
          if config.showConfig.runShowCaptionFetching
          then LogWriter.info(
            s"[ShowUpdater] ✓ run show caption fetching is ${config.showConfig.runShowCaptionFetching}"
          ) >> saveAndFetchCaptions(totalShowsNoCaption)
          else
            LogWriter.info(
              s"[ShowUpdater] ❌ run show caption fetching is ${config.showConfig.runShowCaptionFetching}"
            )

        _ <- LogWriter.info(s"[ShowUpdater] Insert ${totalShowsNoCaption.length} DBShowDatas to DB")
        _ <- LogWriter.debug(s"[ShowUpdater] Data to be intserted: $youTubeBotdbShowDatas")
        _ <- insertDBShowDatas(totalShowsNoCaption)
        _ <- LogWriter.info("[ShowUpdater] Save DBShowDatas to project Jsons")
        _ <- totalShowsNoCaption.traverse_(youTubeBotdbShowData =>
          LogWriter.info(
            s"[ShowUpdater] Save ${youTubeBotdbShowData.dbShowDatas.length} in ${youTubeBotdbShowData.outputFilePath}"
          ) >>
            updateStoredJsons(youTubeBotdbShowData.outputFilePath, youTubeBotdbShowData.dbShowDatas)
        )
      } yield ()
      if config.showConfig.runShowFetching
      then Resource.eval(updateShowFromYoutube)
      else Resource.eval(updateShowFromJsons)
    }

    private[show] def getStoredDbShowDatas: F[List[YouTubeBotDBShowDatas]] = {
      val showFilesResource: Resource[F, List[YouTubeBotFile]] =
        config.showConfig.showSources
          .traverse(showSource =>
            Resource
              .make(Async[F].delay(Path.of(showSource.outputFilePath)))(f =>
                LogWriter.info(s"[ShowUpdater] Closing file $f")
              )
              .map(file =>
                YouTubeBotFile(
                  botId = SBotId(showSource.botId),
                  captionLanguage = showSource.captionLanguage,
                  captionFolderPath = showSource.captionFolderPath,
                  file = file
                )
              )
          )
      val deleteFiles: F[List[YouTubeBotDBShowDatas]] =
        showFilesResource.use(showFiles =>
          for {
            _ <- LogWriter.info(s"[ShowUpdater] ✓ Dry Run. Delete show files: ${showFiles.map(_.file)}")
            _ <- showFiles.traverse(showFile => Async[F].delay(Files.delete(showFile.file)))
          } yield List.empty
        )
      val getStoredDbShowDatas: F[List[YouTubeBotDBShowDatas]] =
        showFilesResource.use(showFiles =>
          LogWriter.info(s"[ShowUpdater] ❌ Dry Run. read show files: ${showFiles.map(_.file)}") >>
            showFiles.traverse(showFile =>
              for {
                _                   <- LogWriter.info(s"[ShowUpdater] Parse show file content: ${showFile.file}")
                showFileContentJson <- Async[F]
                  .fromEither(parse(Source.fromFile(showFile.file.toFile()).mkString))
                  .onError(e => LogWriter.error(s"[ShowUpdater] Error Reading File $showFile: $e"))
                dbShowDatas <- Async[F]
                  .fromEither(showFileContentJson.as[List[DBShowData]])
                  .onError(e => LogWriter.error(s"[ShowUpdater] Error Parsing file $showFile: $e"))
              } yield YouTubeBotDBShowDatas(
                botId = showFile.botId,
                outputFilePath = showFile.file.toString,
                captionFolderPath = showFile.captionFolderPath,
                captionLanguage = showFile.captionLanguage,
                dbShowDatas = dbShowDatas.distinctBy(_.show_id)
              )
            )
        )
      if config.showConfig.dryRun
      then deleteFiles
      else getStoredDbShowDatas
    }

    private[show] def videoToDBShowData(video: Video, botId: SBotId): F[Option[DBShowData]] = {
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
        bot_id = botId.value,
        show_title = title,
        show_upload_date = uploadDate,
        show_duration = durationISO8601ToSeconds(duration),
        show_description = Option(video.getSnippet().getDescription().replace("\n", " ")),
        show_is_live = Option(video.getLiveStreamingDetails()).isDefined,
        show_origin_automatic_caption = None // Added in a followup step. need yt-dlp
      )
      maybeDBShowData.fold(
        LogWriter.error(s"[PlaygroundMain] ERROR: $botId Video conversion problem for $video") *> None.pure[F]
      )(
        _.some.pure[F]
      )
    }

    private[show] def updateStoredJsons(outputFilePath: String, dbShowDatas: List[DBShowData]): F[Unit] = {
      Async[F]
        .fromTry(
          Try(
            Files.write(
              Paths.get(outputFilePath),
              dbShowDatas.sortBy(_.show_id).asJson.spaces2.getBytes(StandardCharsets.UTF_8)
            )
          )
        )
        .void
    }

    val shellDependencies: List[String]                                = List("yt-dlp")
    private def checkDependencies(dependencies: List[String]): F[Unit] = {
      dependencies
        .traverse_(program =>
          Async[F]
            .delay(s"which $program".!)
            .flatMap(result =>
              Async[F].raiseUnless(result == 0)(
                Throwable(s"[ShowUpdater] error checking dependencies: $program is missing")
              )
            )
        )
    }

    private[show] def saveAndFetchCaptions(
        youTubeBotDBShowDatass: List[YouTubeBotDBShowDatas],
        dependencies: List[String] = shellDependencies
    ): F[Unit] = for {
      _ <- LogWriter.info(s"[ShowUpdater] Check Dependencies: ${shellDependencies.mkString}")
      _ <- checkDependencies(dependencies)
      _ <- youTubeBotDBShowDatass.traverse(saveAndFetchCaption)
    } yield ()

    private[show] def saveAndFetchCaption(
        youTubeBotDBShowDatas: YouTubeBotDBShowDatas
    ): F[Unit] = for {
      _ <- LogWriter.info("[ShowUpdater] Getting Caption Folder")
      captionFolderPath = Path.of(youTubeBotDBShowDatas.captionFolderPath)
      _ <- LogWriter.info("[ShowUpdater] 🚀 Start fetching and saving captions")
      _ <- youTubeBotDBShowDatas.dbShowDatas.traverse(
        saveAndFetchCaptionDbShowData(
          _,
          captionFolderPath = captionFolderPath,
          captionLanguage = youTubeBotDBShowDatas.captionLanguage
        )
      )
      _ <- LogWriter.info("[ShowUpdater] 🏁 Finish saving captions")
    } yield ()

    private[show] def saveAndFetchCaptionDbShowData(
        dbShowData: DBShowData,
        captionFolderPath: Path,
        captionLanguage: String
    ): F[Unit] = for {
      _ <- LogWriter.info("[ShowUpdater] 💾 Saving caption for ${dbShowData.show_id}")
      _ <- youTubeService.saveCaption(
        videoId = dbShowData.show_id,
        captionFolderPath = captionFolderPath,
        captionLanguage = captionLanguage
      )
    } yield ()
  }
}
