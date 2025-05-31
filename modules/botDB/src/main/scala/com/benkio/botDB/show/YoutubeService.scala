package com.benkio.botDB.show

import cats.effect.Resource
import com.benkio.botDB.config.Config
import cats.effect.kernel.Async
import cats.syntax.all.*
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequest
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.model.*
import com.google.api.services.youtube.YouTube
import log.effect.LogWriter

import scala.jdk.CollectionConverters.*

trait YouTubeService[F[_]] {
  def getAllIds: Resource[F, List[String]]
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
      _ <- LogWriter.info("[YouTubeService] Create youtube")
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

  private class YouTubeServiceImpl[F[_]: Async: LogWriter](youTubeService: YouTube, config: Config, youTubeApiKey: String)
      extends YouTubeService[F] {
    override def getAllIds: Resource[F, List[String]] = ???
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
          for
            nextRequest <- YouTubeRequests
              .createYouTubePlaylistRequest(youTubeService, playlistId, nextPageToken.some, youTubeApiKey)
            nextResponse <- Async[F].delay(nextRequest.execute())
            nextIds      <- collectAllIds(nextResponse)
          yield nextIds
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
    playlistIds.foldMapM(pId => getYouTubePlaylistIds(youTubeService, pId, youTubeApiKey))

  private def getYouTubeChannelUploadsPlaylistId[F[_]: Async: LogWriter](
      youTubeService: YouTube,
      channelHandle: String,
      youTubeApiKey: String
  ): F[String] =
    for {
      request  <- YouTubeRequests.createYouTubeChannelUploadPlaylistRequest(youTubeService, channelHandle, youTubeApiKey)
      response <- Async[F].delay(request.execute())
      firstItem <- Async[F].fromOption(
        response.getItems().asScala.toList.headOption,
        Throwable(s"[YouTubeService] $channelHandle can't find the upload youtube playlist")
      )
      uploadPlaylistId = firstItem.getContentDetails().getRelatedPlaylists().getUploads()
    } yield uploadPlaylistId

  private def getYoutubeVideos[F[_]: Async: LogWriter](
      youTubeService: YouTube,
      videoIds: List[String],
      youTubeApiKey: String
  ): F[List[Video]] = {
    val videoIdsChucks = videoIds.grouped(maxResults).toList
    for {
      _ <- LogWriter.info(s"[YouTubeService] getYouTubeVideos ${videoIdsChucks.length} requests for ${videoIds.length}")
      requests <- videoIdsChucks.traverse(YouTubeRequests.createYouTubeVideoRequest(youTubeService, _, youTubeApiKey))
      videos <- requests.foldLeft(List.empty[Video].pure[F]) { case (ioAcc, request) =>
        for
          response <- Async[F].delay(request.execute())
          videos = response.getItems().asScala.toList
          acc <- ioAcc
        yield acc ++ videos
      }
    } yield videos
  }
}
