package com.benkio.botDB.show

import munit.*

class YoutubeSourceSpec extends FunSuite {

  val botName = "testBot"

  test("YoutubeSource should return YoutubeSource.Playlist the input doesn't start with `@`") {
    val playlistId = "PL7lQFvEjqu8OBiulbaSNnlCtlfI8Zd7zS"
    assertEquals(
      YoutubeSource(playlistId),
      YoutubeSource.Playlist(playlistId)
    )
  }
  test("YoutubeSource should return YoutubeSource.Channel the input starts with `@`") {
    val channelHandle = "@youtuboancheio1365"
    assertEquals(
      YoutubeSource(channelHandle),
      YoutubeSource.Channel(channelHandle)
    )
  }
}
