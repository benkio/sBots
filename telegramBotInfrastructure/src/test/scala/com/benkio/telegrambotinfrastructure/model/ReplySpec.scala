package com.benkio.telegrambotinfrastructure.model

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
    assertEquals(mp3"audio.mp3", mp3"audio.mp3")
    assertEquals(pho"picture.jpg", pho"picture.jpg")
    assertEquals(pho"picture.png", pho"picture.png")
    assertEquals(gif"gif.gif", gif"gif.gif")
    assertEquals(vid"video.mp4", vid"video.mp4")
  }

  test("MediaFile show instance should return the expected string") {
    assertEquals(MediaFile.showInstance.show(mp3"audio.mp3"), "audio.mp3")
    assertEquals(MediaFile.showInstance.show(pho"picture.jpg"), "picture.jpg")
    assertEquals(MediaFile.showInstance.show(pho"picture.png"), "picture.png")
    assertEquals(MediaFile.showInstance.show(gif"gif.gif"), "gif.gif")
    assertEquals(MediaFile.showInstance.show(vid"video.mp4"), "video.mp4")
  }
}
