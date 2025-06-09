package com.benkio.botDB.show

import cats.effect.implicits.*
import cats.effect.kernel.Async
import cats.effect.Resource
import cats.implicits.*
import cats.Semigroup
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
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import scala.io.Source
import scala.jdk.CollectionConverters.*
import scala.sys.process.*
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
              YouTubeBotVideos(
                botName = youTubeBotIds.botName,
                outputFilePath = youTubeBotIds.outputFilePath,
                captionLanguage = youTubeBotIds.captionLanguage,
                videos = youTubeBotVideos
              )
            )
        )
    }

    def youTubeBotVideosToDbShowDatas(youTubeBotVideos: List[YouTubeBotVideos]): F[List[YouTubeBotDBShowDatas]] = {
      youTubeBotVideos.traverse(youTubeBotVideos =>
        youTubeBotVideos.videos
          .traverse(videoToDBShowData(_, youTubeBotVideos.botName))
          .map((dBShowDatas: List[Option[DBShowData]]) =>
            YouTubeBotDBShowDatas(
              botName = youTubeBotVideos.botName,
              outputFilePath = youTubeBotVideos.outputFilePath,
              captionLanguage = youTubeBotVideos.captionLanguage,
              dbShowDatas = dBShowDatas.flatMap(_.toList)
            )
          )
      )
    }

    private[show] def insertDBShowDatas(
        youTubeBotdbShowDatas: List[YouTubeBotDBShowDatas]
    ): F[Unit] = {
      val inputDbShowData = youTubeBotdbShowDatas
        .flatMap { case YouTubeBotDBShowDatas(_, _, _, dbShowDatas) => dbShowDatas }

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
        _                 <- storedDbShowDatas.traverse_(storedDbShowData =>
          LogWriter.info(
            s"[ShowUpdater] ${storedDbShowData.botName} has ${storedDbShowData.dbShowDatas.length} stored in ${storedDbShowData.outputFilePath}"
          )
        )
        youTubeBotIds = filterCandidateIds(candidateIds, storedDbShowDatas.flatMap(_.dbShowDatas.map(_.show_id)))
        _                <- LogWriter.info(s"[ShowUpdater] ${youTubeBotIds.flatMap(_.videoIds).length} Ids to be added")
        _                <- LogWriter.debug(s"[ShowUpdater] Data to be added: ${youTubeBotIds}")
        _                <- LogWriter.info("[ShowUpdater] Fetching data from Ids")
        youTubeBotVideos <- youTubeBotIdsToVideos(youTubeBotIds)
        _ <- LogWriter.info(s"[ShowUpdater] ${youTubeBotVideos.length} Converting YouTube data to DBShowData")
        _ <- LogWriter.debug(s"[ShowUpdater] Data to be converted: ${youTubeBotVideos}")
        youTubeBotdbShowDatas <- youTubeBotVideosToDbShowDatas(youTubeBotVideos)
        totalShowsNoCaption = mergeShowDatas(storedDbShowDatas, youTubeBotdbShowDatas)
        totalShows <-
          if config.showConfig.runShowCaptionFetching
          then
            LogWriter.info(
              s"[ShowUpdater] ✓ run show caption fetching is ${config.showConfig.runShowCaptionFetching}"
            ) >> addCaptions(totalShowsNoCaption)
          else
            LogWriter.info(
              s"[ShowUpdater] ❌ run show caption fetching is ${config.showConfig.runShowCaptionFetching}"
            ) >> Async[F].pure(totalShowsNoCaption)

        _ <- LogWriter.info(s"[ShowUpdater] Insert ${totalShows.length} DBShowDatas to DB")
        _ <- LogWriter.debug(s"[ShowUpdater] Data to be intserted: $youTubeBotdbShowDatas")
        _ <- insertDBShowDatas(totalShows)
        _ <- LogWriter.info("[ShowUpdater] Save DBShowDatas to project Jsons")
        _ <- totalShows.traverse_(youTubeBotdbShowData =>
          LogWriter.info(
            s"[ShowUpdater] Save ${youTubeBotdbShowData.dbShowDatas.length} in ${youTubeBotdbShowData.outputFilePath}"
          ) >>
            updateStoredJsons(youTubeBotdbShowData.outputFilePath, youTubeBotdbShowData.dbShowDatas)
        )
      } yield ()
      if config.showConfig.runShowFetching
      then Resource.eval(program)
      else Resource.eval(LogWriter.info("[ShowUpdater] Option runShowFetching = true. No run"))
    }

    private[show] def getStoredDbShowDatas: F[List[YouTubeBotDBShowDatas]] = {
      val showFilesResource: Resource[F, List[YouTubeBotFile]] =
        config.showConfig.showSources
          .traverse(showSource =>
            Resource
              .make(Async[F].delay(File(showSource.outputFilePath)))(f =>
                LogWriter.info(s"[ShowUpdater] Closing file $f")
              )
              .map(file =>
                YouTubeBotFile(botName = showSource.botName, captionLanguage = showSource.captionLanguage, file = file)
              )
          )
      val deleteFiles: F[List[YouTubeBotDBShowDatas]] =
        showFilesResource.use(showFiles =>
          for {
            _ <- LogWriter.info(s"[ShowUpdater] ✓ Dry Run. Delete show files: ${showFiles.map(_.file)}")
            _ <- showFiles.traverse(showFile => Async[F].delay(showFile.file.delete))
          } yield List.empty
        )
      val getStoredDbShowDatas: F[List[YouTubeBotDBShowDatas]] =
        showFilesResource.use(showFiles =>
          LogWriter.info(s"[ShowUpdater] ❌ Dry Run. read show files: ${showFiles.map(_.file)}") >>
            showFiles.traverse(showFile =>
              for {
                _                   <- LogWriter.info(s"[ShowUpdater] Parse show file content: ${showFile.file}")
                showFileContentJson <- Async[F].fromEither(parse(Source.fromFile(showFile.file).mkString))
                dbShowDatas         <- Async[F].fromEither(showFileContentJson.as[List[DBShowData]])
              } yield YouTubeBotDBShowDatas(
                botName = showFile.botName,
                outputFilePath = showFile.file.toString,
                captionLanguage = showFile.captionLanguage,
                dbShowDatas = dbShowDatas.distinctBy(_.show_id)
              )
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
        show_origin_automatic_caption = None // Added in a followup step. need yt-dlp
      )
      maybeDBShowData.fold(
        LogWriter.error(s"[PlaygroundMain] ERROR: $botName Video conversion problem for $video") *> None.pure[F]
      )(
        _.some.pure[F]
      )
    }

    private[show] def updateStoredJsons(outputFilePath: String, dbShowDatas: List[DBShowData]): F[Unit] = {
      Async[F]
        .fromTry(
          Try(
            Files.write(Paths.get(outputFilePath), dbShowDatas.asJson.spaces2.getBytes(StandardCharsets.UTF_8))
          )
        )
        .as(())
    }

    val shellDependencies: List[String]    = List("yt-dlp")
    private def checkDependencies: F[Unit] = {
      shellDependencies
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

    private[show] def addCaptions(
        youTubeBotDBShowDatass: List[YouTubeBotDBShowDatas]
    ): F[List[YouTubeBotDBShowDatas]] = {
      def fetchCaption(dbShowData: DBShowData, tempDir: Path, captionLanguage: String): F[DBShowData] = {
        val command =
          s"""yt-dlp --write-auto-subs --sub-lang $captionLanguage --skip-download --sub-format json3 -o "%(id)s" -P $tempDir https://www.youtube.com/watch?v=${dbShowData.show_id}"""
        val captionDownloadLogic = for {
          _           <- LogWriter.info(s"[ShowUpdater] ${dbShowData.show_id} - $captionLanguage: fetch caption")
          _           <- Async[F].delay(command.!)
          captionFile <- Async[F].delay(tempDir.resolve(s"${dbShowData.show_id}.$captionLanguage.json3"))
          _           <- LogWriter.info(
            s"[ShowUpdater] ${dbShowData.show_id} - $captionLanguage: Parse result file: $captionFile"
          )
          captionFileContent <- Async[F].fromTry(
            Try(
              Files
                .readAllLines(captionFile)
                .asScala
                .mkString("\n")
            )
          )
          captionJson <- Async[F].fromEither(parse(captionFileContent))
          caption = captionJson.findAllByKey("utf8").map(_.as[String]).collect { case Right(value) => value }.mkString
          _ <- LogWriter.info(
            s"[ShowUpdater] ${dbShowData.show_id} - $captionLanguage: caption length ${caption.length}"
          )
        } yield caption
        captionDownloadLogic
          .map(Some(_))
          .handleErrorWith(e =>
            LogWriter.error(
              s"[ShowUpdater] ❌ ${dbShowData.show_id} - $captionLanguage Downloading Caption: $e"
            ) >> Async[F]
              .pure(None)
          )
          .map(caption =>
            dbShowData.copy(
              show_origin_automatic_caption = caption
            )
          )
      }
      for {
        _       <- LogWriter.info(s"[ShowUpdater] Check Dependencies: ${shellDependencies.mkString}")
        _       <- checkDependencies
        _       <- LogWriter.info("[ShowUpdater] Create caption temp folder")
        tempDir <- Async[F].pure(Files.createTempDirectory(Paths.get("target"), "ytdlpCaptions").toAbsolutePath())
        _       <- LogWriter.info("[ShowUpdater] Start fetching captions")
        result  <- youTubeBotDBShowDatass.traverse(youTubeBotDBShowDatas =>
          val (youTubeBotDBShowDatasNoCaption, youTubeBotDBShowDatasCaption) =
            youTubeBotDBShowDatas.dbShowDatas
              .partition(_.show_origin_automatic_caption.isEmpty)

          LogWriter.info(
            s"[ShowUpdater] ${youTubeBotDBShowDatas.botName} Total No Caption ${youTubeBotDBShowDatasNoCaption.length}"
          ) >>
            youTubeBotDBShowDatasNoCaption
              .parTraverse(dbShowData => fetchCaption(dbShowData, tempDir, youTubeBotDBShowDatas.captionLanguage))
              .map(dbShowDatas => youTubeBotDBShowDatas.copy(dbShowDatas = dbShowDatas ++ youTubeBotDBShowDatasCaption))
        )
      } yield result
    }
  }
}
