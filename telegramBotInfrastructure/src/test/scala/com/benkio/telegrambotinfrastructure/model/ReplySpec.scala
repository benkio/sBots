package com.benkio.telegrambotinfrastructure.model

import cats.implicits.*
import munit.FunSuite

class ReplySpec extends FunSuite {

  test("Mp3File case class should accept only .mp3 extension when filename created") {
    val _ = Mp3File("test.mp3") // This shouldn't throw an exception
    intercept[IllegalArgumentException] { Mp3File("test.gif") }
  }
  test("GifFile case class should accept only .gif extension when filename created") {
    val _ = GifFile("test.gif")
    intercept[IllegalArgumentException] { GifFile("test.mp3") }
  }
  test("PhotoFile case class should accept only .jpg and .png extension when filenames create") {
    val _ = PhotoFile("test.jpg")
    val _ = PhotoFile("test.png")
    intercept[IllegalArgumentException] { PhotoFile("test.mp3") }
  }

  test("VideoFile case class should accept only .mp4 extension when filename created") {
    val _ = VideoFile("test.mp4")
    intercept[IllegalArgumentException] { VideoFile("test.mp3") }
  }

  test(
    "MediaFile apply should create the right MediaFile when a valid filename with an allowed extension is provided"
  ) {
    assertEquals(MediaFile("audio.mp3"), Mp3File("audio.mp3"))
    assertEquals(MediaFile("picture.jpg"), PhotoFile("picture.jpg"))
    assertEquals(MediaFile("picture.png"), PhotoFile("picture.png"))
    assertEquals(MediaFile("gif.gif"), GifFile("gif.gif"))
    assertEquals(MediaFile("video.mp4"), VideoFile("video.mp4"))
  }

  test("MediaFile apply should not create the Mediafile when a filename with an unknown extension is provided") {
    interceptMessage[IllegalArgumentException](
      "filepath extension not recognized: test.fuck \n allowed extensions: mp3, gif, jpg, png, mp4"
    ) {
      MediaFile("test.fuck")
    }
  }

  test("MediaFile show instance should return the expected string") {
    assertEquals(MediaFile("audio.mp3").show, "audio.mp3")
    assertEquals(MediaFile("picture.jpg").show, "picture.jpg")
    assertEquals(MediaFile("picture.png").show, "picture.png")
    assertEquals(MediaFile("gif.gif").show, "gif.gif")
    assertEquals(MediaFile("video.mp4").show, "video.mp4")
  }
}
