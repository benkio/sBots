package com.benkio.botDB.show

import cats.syntax.all.*
import cats.ApplicativeThrow
import org.http4s.Uri

final case class ShowSource(youtubeSources: List[YoutubeSource], botName: String, outputFilePath: String)

sealed trait YoutubeSource:
  def url: Uri

final case class YoutubePlaylist(url: Uri) extends YoutubeSource
final case class YoutubeChannel(url: Uri)  extends YoutubeSource

object ShowSource:

  def apply[F[_]: ApplicativeThrow](urls: List[String], botName: String, outputFilePath: String): F[ShowSource] =
    urls
      .traverse(url =>
        ApplicativeThrow[F]
          .fromEither(Uri.fromString(url))
          .map(uri =>
            if url.toLowerCase.contains("playlist")
            then YoutubePlaylist(uri)
            else YoutubeChannel(uri)
          )
      )
      .map(youtubeSources => ShowSource(youtubeSources, botName, outputFilePath))

extension (youtubeSource: YoutubeSource) def toYTDLPCommand: String = s"""yt-dlp -J --verbose ${youtubeSource.url}"""
