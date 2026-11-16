package com.benkio.botDB.show

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.google.api.services.youtube.YouTube
import log.effect.LogWriter

import scala.jdk.CollectionConverters.*

object YouTubeRequests {

  val maxResults = 50L

  // YouTube Requests ///////////////////////////////////////////////////////

  def createYouTubeVideoRequest[F[_]: Async](youTubeService: YouTube, videoIds: List[String], apiKeys: String)(using
      log: LogWriter[F]
  ): F[YouTube#Videos#List] =
    for {
      _       <- log.info(s"[YouTubeRequests] ${videoIds.length} Create a YouTube Video request")
      request <- Async[F].delay(
        youTubeService
          .videos()
          .list(List("id", "snippet", "contentDetails", "liveStreamingDetails").asJava)
          .setKey(apiKeys)
          .setFields(
            "items(id,snippet/title,snippet/publishedAt,snippet/description,contentDetails/duration,liveStreamingDetails)"
          )
          .setMaxResults(maxResults)
          .setId(videoIds.asJava)
      )
    } yield request

  def createYouTubeVideoCaptionRequest[F[_]: Async](youTubeService: YouTube, videoId: String, apiKeys: String)(using
      log: LogWriter[F]
  ): F[YouTube#Captions#List] =
    for {
      _       <- log.info(s"[YouTubeRequests] $videoId Create a YouTube Video Caption request")
      request <- Async[F].delay(
        youTubeService
          .captions()
          .list(List("id", "snippet").asJava, videoId)
          .setKey(apiKeys)
          .setFields("items(id,snippet/trackKind,snippet/language)")
      )
    } yield request

  def createYouTubeDownloadVideoCaptionRequest[F[_]: Async](
      youTubeService: YouTube,
      captionId: String,
      apiKeys: String
  )(using
      log: LogWriter[F]
  ): F[YouTube#Captions#Download] =
    for {
      _       <- log.info(s"[YouTubeRequests] $captionId Create a Download YouTube Video Caption request")
      request <- Async[F].delay(
        youTubeService
          .captions()
          .download(captionId)
          .setKey(apiKeys)
          .setFields("items(id,snippet/trackKind,snippet/language)")
      )
    } yield request

  def createYouTubePlaylistRequest[F[_]: Async](
      youTubeService: YouTube,
      playlistId: String,
      pageToken: Option[String],
      apiKeys: String
  )(using log: LogWriter[F]): F[YouTube#PlaylistItems#List] =
    for {
      _       <- log.info(s"[YouTubeRequests] $playlistId Create a YouTube Video Playlist request")
      request <- Async[F].delay(
        youTubeService
          .playlistItems()
          .list(List("contentDetails").asJava)
          .setKey(apiKeys)
          .setFields("items(contentDetails/videoId),nextPageToken,pageInfo")
          .setPlaylistId(playlistId)
          .setMaxResults(maxResults)
      )
    } yield pageToken.fold(request)(pt => request.setPageToken(pt))

  def createYouTubeChannelUploadPlaylistRequest[F[_]: Async](
      youTubeService: YouTube,
      channelHandle: String,
      apiKeys: String
  )(using log: LogWriter[F]): F[YouTube#Channels#List] =
    for {
      _       <- log.info(s"[YouTubeRequests] $channelHandle Create a YouTube Channel request")
      request <- Async[F].delay(
        youTubeService
          .channels()
          .list(List("contentDetails").asJava)
          .setForHandle(channelHandle)
          .setKey(apiKeys)
          .setFields("items(contentDetails/relatedPlaylists/uploads)")
      )
    } yield request

}
