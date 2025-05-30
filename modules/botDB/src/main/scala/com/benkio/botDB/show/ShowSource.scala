package com.benkio.botDB.show

final case class ShowSource(youtubeSources: List[YoutubeSource], botName: String, outputFilePath: String)

enum YoutubeSource:
  case Playlist(id: String)    extends YoutubeSource
  case Channel(handle: String) extends YoutubeSource

object YoutubeSource {
  def apply(source: String): YoutubeSource =
    if source.startsWith("@")
    then YoutubeSource.Channel(source)
    else YoutubeSource.Playlist(source)
}
