package com.benkio.botDB.show

import munit.*

class YouTubeSourceSpec extends FunSuite {

  val botName = "testBot"

  test("YouTubeSource should return YouTubeSource.Playlist the input doesn't start with `@`") {
    val playlistId = "PL7lQFvEjqu8OBiulbaSNnlCtlfI8Zd7zS"
    assertEquals(
      YouTubeSource(playlistId),
      YouTubeSource.Playlist(playlistId)
    )
  }
  test("YouTubeSource should return YouTubeSource.Channel the input starts with `@`") {
    val channelHandle = "@youtuboancheio1365"
    assertEquals(
      YouTubeSource(channelHandle),
      YouTubeSource.Channel(channelHandle)
    )
  }
}
