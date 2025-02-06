package com.benkio.botDB.show

import munit.*
import org.http4s.Uri

class ShowSourceSpec extends FunSuite {

  type EitherThrow[A] = Either[Throwable, A]
  val outputFilePath = "file.json"

  val botName = "testBot"

  test("fromString should return the input url if it's valid playlist") {
    val url = "https://youtube.com/playlist?list=PL7lQFvEjqu8OBiulbaSNnlCtlfI8Zd7zS&si=5yXXFQk025DZctuE"
    assertEquals(
      ShowSource[EitherThrow](List(url), botName, outputFilePath),
      Right(ShowSource(List(YoutubePlaylist(Uri.unsafeFromString(url))), botName, outputFilePath))
    )
  }
  test("fromString should return the input url if it's valid channel") {
    val url = "https://www.youtube.com/@youtuboancheio1365"
    assertEquals(
      ShowSource[EitherThrow](List(url), botName, outputFilePath),
      Right(ShowSource(List(YoutubeChannel(Uri.unsafeFromString(url))), botName, outputFilePath))
    )
  }
  test("fromString should fail if the url is invalid") {
    val url = List("invalid url")
    assert(ShowSource[EitherThrow](url, botName, outputFilePath).isLeft)
  }
}
