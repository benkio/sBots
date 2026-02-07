package com.benkio.botDB.show

import cats.effect.kernel.Async
import cats.syntax.all.*
import cats.Semigroup
import com.benkio.botDB.config.Config
import com.benkio.botDB.config.ShowSourceConfig
import com.benkio.telegrambotinfrastructure.model.SBotInfo.SBotId
import com.benkio.telegrambotinfrastructure.repository.db.DBShowData
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.model.*
import com.google.api.services.youtube.YouTube
import io.circe.*
import io.circe.parser.*
import log.effect.LogWriter

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import scala.jdk.CollectionConverters.*
import scala.sys.process.*
import scala.util.Try

final case class YouTubeBotFile(botId: SBotId, captionLanguage: String, file: File)
final case class YouTubeBotIds(botId: SBotId, outputFilePath: String, captionLanguage: String, videoIds: List[String])
final case class YouTubeBotVideos(botId: SBotId, outputFilePath: String, captionLanguage: String, videos: List[Video])
final case class YouTubeBotDBShowDatas(
    botId: SBotId,
    outputFilePath: String,
    captionLanguage: String,
    dbShowDatas: List[DBShowData]
)

object YouTubeBotDBShowDatas {
  given Semigroup[YouTubeBotDBShowDatas] {
    def combine(d1: YouTubeBotDBShowDatas, d2: YouTubeBotDBShowDatas) =
      YouTubeBotDBShowDatas(
        botId = d1.botId,
        outputFilePath = d1.outputFilePath,
        captionLanguage = d1.captionLanguage,
        dbShowDatas = d1.dbShowDatas ++ d2.dbShowDatas
      )
  }
}

trait YouTubeService[F[_]] {
  def getAllBotNameIds: F[List[YouTubeBotIds]]
  def getYouTubeVideos(
      videoIds: List[String]
  ): F[List[Video]]
  def fetchCaption(videoId: String, tempDir: Path, captionLanguage: String): F[Option[String]]
}

object YouTubeService {

  private val maxResults: Int = 50

  def apply[F[_]: LogWriter: Async](
      config: Config,
      youTubeApiKey: String
  ): F[YouTubeService[F]] =
    for {
      _                      <- LogWriter.info("[YouTubeService] Create NetHttpTransport and jsonFactory for YouTube")
      googleNetHttpTransport <- Async[F].delay(GoogleNetHttpTransport.newTrustedTransport())
      jsonFactory = GsonFactory.getDefaultInstance()
      _              <- LogWriter.info("[YouTubeService] Create youtube")
      youTubeService <- Async[F].delay(
        YouTube
          .Builder(
            googleNetHttpTransport,
            jsonFactory,
            new HttpRequestInitializer() {
              override def initialize(request: HttpRequest): Unit = ()
            }
          )
          .setApplicationName(config.showConfig.applicationName)
          .build()
      )
    } yield YouTubeServiceImpl(
      youTubeService = youTubeService,
      config = config,
      youTubeApiKey = youTubeApiKey
    )

  private class YouTubeServiceImpl[F[_]: Async: LogWriter](
      youTubeService: YouTube,
      config: Config,
      youTubeApiKey: String
  ) extends YouTubeService[F] {
    override def getAllBotNameIds: F[List[YouTubeBotIds]] = {
      val source = config.showConfig.showSources
      for {
        _              <- LogWriter.info("[YouTubeService] Get Youtube playlist Ids from sources")
        botPlaylistIds <- source.traverse {
          case ShowSourceConfig(youTubeSources, botId, captionLanguage, outputFilePath) =>
            youTubeSources
              .map(YouTubeSource(_))
              .traverse {
                case YouTubeSource.Playlist(id)           => id.pure[F]
                case YouTubeSource.Channel(channelHandle) =>
                  getYouTubeChannelUploadsPlaylistId(
                    youTubeService = youTubeService,
                    channelHandle = channelHandle,
                    youTubeApiKey = youTubeApiKey
                  )
              }
              .map(YouTubeBotIds(SBotId(botId), outputFilePath, captionLanguage, _))
        }
        _           <- LogWriter.info("[YouTubeService] Get Youtube videos Ids from sources")
        botVideoIds <- botPlaylistIds.traverse {
          case YouTubeBotIds(botId, outputFilePath, captionLanguage, playlistIds) =>
            getYouTubePlaylistsIds(
              youTubeService = youTubeService,
              playlistIds = playlistIds,
              youTubeApiKey = youTubeApiKey
            )
              .map(videoIds => YouTubeBotIds(botId, outputFilePath, captionLanguage, videoIds))
        }
      } yield botVideoIds
    }

    override def getYouTubeVideos(
        videoIds: List[String]
    ): F[List[Video]] = {
      val videoIdsChucks = videoIds.grouped(maxResults).toList
      for {
        _ <- LogWriter.info(
          s"[YouTubeService] getYouTubeVideos ${videoIdsChucks.length} requests for ${videoIds.length}"
        )
        requests <- videoIdsChucks.traverse(YouTubeRequests.createYouTubeVideoRequest(youTubeService, _, youTubeApiKey))
        _        <- LogWriter.debug(s"[YoutubeService] ${requests.length} YouTube requests")
        videos   <- requests.foldLeft(List.empty[Video].pure[F]) { case (ioAcc, request) =>
          for {
            _        <- LogWriter.debug(s"[YoutubeService] Execute request $request")
            response <- Async[F].delay(request.execute())
            _        <- LogWriter.debug(s"[YoutubeService] request response $response")
            videos = response.getItems().asScala.toList
            _   <- LogWriter.debug(s"[YoutubeService] request response items $videos")
            acc <- ioAcc
            result = acc ++ videos
            _ <- LogWriter.debug(s"[YoutubeService] request results ${result.length}")
          } yield result
        }
      } yield videos
    }

    override def fetchCaption(videoId: String, tempDir: Path, captionLanguage: String): F[Option[String]] = {
      val command =
        s"""yt-dlp --write-auto-subs --sub-lang $captionLanguage --skip-download --sub-format json3 -o "%(id)s" -P $tempDir https://www.youtube.com/watch?v=${videoId}"""
      val captionDownloadLogic: F[Option[String]] = for {
        _           <- LogWriter.info(s"[ShowUpdater] ${videoId} - $captionLanguage: fetch caption")
        _           <- Async[F].delay(command.!)
        captionFile <- Async[F].delay(tempDir.resolve(s"${videoId}.$captionLanguage.json3"))
        _           <- LogWriter.info(
          s"[ShowUpdater] ${videoId} - $captionLanguage: Parse result file: $captionFile"
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
          s"[ShowUpdater] ${videoId} - $captionLanguage: caption length ${caption.length}"
        )
      } yield Some(caption.replace("\n", " "))
      captionDownloadLogic
        .handleErrorWith(e =>
          LogWriter.error(
            s"[ShowUpdater] ❌ ${videoId} - $captionLanguage Downloading Caption: $e"
          ) >> Async[F]
            .pure(None)
        )
    }
  }

  private def getYouTubePlaylistIds[F[_]: LogWriter: Async](
      youTubeService: YouTube,
      playlistId: String,
      youTubeApiKey: String
  ): F[List[String]] = {
    def extractPlaylistIds(response: PlaylistItemListResponse): List[String] =
      response.getItems().asScala.toList.map(_.getContentDetails().getVideoId())
    def collectAllIds(response: PlaylistItemListResponse): F[List[String]] =
      Option(response.getNextPageToken())
        .fold(List.empty.pure[F])(nextPageToken =>
          for {
            nextRequest <- YouTubeRequests
              .createYouTubePlaylistRequest(youTubeService, playlistId, nextPageToken.some, youTubeApiKey)
            nextResponse <- Async[F].delay(nextRequest.execute())
            nextIds      <- collectAllIds(nextResponse)
          } yield nextIds
        )
        .map(nextIds => extractPlaylistIds(response) ++ nextIds)
    for {
      _               <- LogWriter.info(s"[YouTubeService] $playlistId Ids fetching")
      initialRequest  <- YouTubeRequests.createYouTubePlaylistRequest(youTubeService, playlistId, None, youTubeApiKey)
      _               <- LogWriter.info(s"[YouTubeService] $playlistId Initial Request execution")
      initialResponse <- Async[F].delay(initialRequest.execute())
      _               <- LogWriter.info(s"[YouTubeService] $playlistId Computing video ids")
      ids             <- collectAllIds(initialResponse)
    } yield ids
  }

  private def getYouTubePlaylistsIds[F[_]: Async: LogWriter](
      youTubeService: YouTube,
      playlistIds: List[String],
      youTubeApiKey: String
  ): F[List[String]] =
    playlistIds.foldMapM(pId =>
      getYouTubePlaylistIds(youTubeService = youTubeService, playlistId = pId, youTubeApiKey = youTubeApiKey)
    )

  private def getYouTubeChannelUploadsPlaylistId[F[_]: Async: LogWriter](
      youTubeService: YouTube,
      channelHandle: String,
      youTubeApiKey: String
  ): F[String] =
    for {
      request <- YouTubeRequests.createYouTubeChannelUploadPlaylistRequest(youTubeService, channelHandle, youTubeApiKey)
      response  <- Async[F].delay(request.execute())
      firstItem <- Async[F].fromOption(
        response.getItems().asScala.toList.headOption,
        Throwable(s"[YouTubeService] $channelHandle can't find the upload youtube playlist")
      )
      uploadPlaylistId = firstItem.getContentDetails().getRelatedPlaylists().getUploads()
    } yield uploadPlaylistId
}
