package com.benkio.botDB.show

import org.http4s.Uri
import cats.ApplicativeThrow
import cats.syntax.all.*

sealed trait ShowSource:
  def url: Uri
  def botName: String
  def outputFilePath: String

case class YoutubePlaylist(url: Uri, botName: String, outputFilePath: String) extends ShowSource
case class YoutubeChannel(url: Uri, botName: String, outputFilePath: String)  extends ShowSource

object ShowSource:

  def apply[F[_]: ApplicativeThrow](url: String, botName: String, outputFilePath: String): F[ShowSource] =
    for uri <- ApplicativeThrow[F].fromEither(Uri.fromString(url))
    yield
      if url.toLowerCase.contains("playlist")
      then YoutubePlaylist(uri, botName, outputFilePath)
      else YoutubeChannel(uri, botName, outputFilePath)

extension (source: ShowSource)
  def toYTDLPCommand: String = s"""yt-dlp -J --verbose ${source.url}"""
  def toJqCommand: String = source match {
    case YoutubeChannel(_, botName,_) =>
      s"""jq 'del(..|nulls) | .entries[] | select(.title|contains("Videos")) | [.entries[] | {show_url: .webpage_url, show_title: .title, show_upload_date: .upload_date, show_duration: .duration, show_description: .description, show_is_live: .is_live, show_origin_automatic_caption: .automatic_captions | with_entries(if (.key|test(".*orig")) then ( {key: .key, value: .value } ) else empty end)[][] | select(.ext|contains("json")) | .url }] | map(. + {"bot_name": "$botName"})'"""
    case YoutubePlaylist(_, botName,_) =>
      s"""jq 'del(..|nulls) | [.entries[] | {show_url: .webpage_url, show_title: .title, show_upload_date: .upload_date, show_duration: .duration, show_description: .description, show_is_live: .is_live, show_origin_automatic_caption: .automatic_captions | with_entries(if (.key|test(".*orig")) then ( {key: .key, value: .value } ) else empty end)[][] | select(.ext|contains("json")) | .url }] | map(. + {"bot_name": "$botName"})'"""
  }
