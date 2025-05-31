package com.benkio.botDB.show

import cats.effect.IO
import com.google.api.services.youtube.YouTube
import log.effect.LogWriter

import scala.jdk.CollectionConverters.*

object YoutubeRequests {

  val maxResults = 50L

  // YouTube Requests ///////////////////////////////////////////////////////

  def createYouTubeVideoRequest(youtubeService: YouTube, videoIds: List[String], apiKeys: String)(using
      log: LogWriter[IO]
  ): IO[YouTube#Videos#List] =
    for {
      _ <- log.info(s"[YoutubeRequests] ${videoIds.length} Create a YouTube Video request")
      request <- IO(
        youtubeService
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

  def createYouTubeVideoCaptionRequest(youtubeService: YouTube, videoId: String, apiKeys: String)(using
      log: LogWriter[IO]
  ): IO[YouTube#Captions#List] =
    for {
      _ <- log.info(s"[YoutubeRequests] $videoId Create a YouTube Video Caption request")
      request <- IO(
        youtubeService
          .captions()
          .list(List("id", "snippet").asJava, videoId)
          .setKey(apiKeys)
          .setFields("items(id,snippet/trackKind,snippet/language)")
      )
    } yield request

  def createYouTubePlaylistRequest(
      youtubeService: YouTube,
      playlistId: String,
      pageToken: Option[String],
      apiKeys: String
  )(using log: LogWriter[IO]): IO[YouTube#PlaylistItems#List] =
    for {
      _ <- log.info(s"[YoutubeRequests] $playlistId Create a YouTube Video Playlist request")
      request <- IO(
        youtubeService
          .playlistItems()
          .list(List("contentDetails").asJava)
          .setKey(apiKeys)
          .setFields("items(contentDetails/videoId),nextPageToken,pageInfo")
          .setPlaylistId(playlistId)
          .setMaxResults(maxResults)
      )
    } yield pageToken.fold(request)(pt => request.setPageToken(pt))

  def createYouTubeChannelUploadPlaylistRequest(
      youtubeService: YouTube,
      channelHandle: String,
      apiKeys: String
  )(using log: LogWriter[IO]): IO[YouTube#Channels#List] =
    for {
      _ <- log.info(s"[YoutubeRequests] $channelHandle Create a YouTube Channel request")
      request <- IO(
        youtubeService
          .channels()
          .list(List("contentDetails").asJava)
          .setForHandle(channelHandle)
          .setKey(apiKeys)
          .setFields("items(contentDetails/relatedPlaylists/uploads)")
      )
    } yield request

}
