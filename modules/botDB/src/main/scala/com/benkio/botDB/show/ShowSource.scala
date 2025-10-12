package com.benkio.botDB.show

final case class ShowSource(youTubeSources: List[YouTubeSource], botId: String, outputFilePath: String)

enum YouTubeSource:
  case Playlist(id: String)    extends YouTubeSource
  case Channel(handle: String) extends YouTubeSource

object YouTubeSource {
  def apply(source: String): YouTubeSource =
    if source.startsWith("@")
    then YouTubeSource.Channel(source)
    else YouTubeSource.Playlist(source)
}
