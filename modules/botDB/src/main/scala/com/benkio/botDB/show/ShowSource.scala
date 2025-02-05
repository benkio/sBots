package com.benkio.botDB.show

import org.http4s.Uri
import cats.ApplicativeThrow
import cats.syntax.all.*

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

extension (youtubeSource: YoutubeSource)
  def toYTDLPCommand: String = s"""yt-dlp -J --verbose ${youtubeSource.url}"""
  def toJqCommand(botName: String): String = youtubeSource match {
    case YoutubeChannel(_) =>
      s"""jq 'del(..|nulls) | .entries[] | select(.title|contains("Videos")) | [.entries[] | {show_url: .webpage_url, show_title: .title, show_upload_date: .upload_date, show_duration: .duration, show_description: .description, show_is_live: .is_live, show_origin_automatic_caption: .automatic_captions | with_entries(if (.key|test(".*orig")) then ( {key: .key, value: .value } ) else empty end)[][] | select(.ext|contains("json")) | .url }] | map(. + {"bot_name": "$botName"})'"""
    case YoutubePlaylist(_) =>
      s"""jq 'del(..|nulls) | [.entries[] | {show_url: .webpage_url, show_title: .title, show_upload_date: .upload_date, show_duration: .duration, show_description: .description, show_is_live: .is_live, show_origin_automatic_caption: .automatic_captions | with_entries(if (.key|test(".*orig")) then ( {key: .key, value: .value } ) else empty end)[][] | select(.ext|contains("json")) | .url }] | map(. + {"bot_name": "$botName"})'"""
  }
