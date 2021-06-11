package com.benkio.telegramBotInfrastructure.model

import org.scalatest._
import matchers.should._
import org.scalatest.wordspec.AnyWordSpec

class ReplySpec extends AnyWordSpec with Matchers {

  "Mp3File case class" should {
    "accept only .mp3 extendion filename" when {
      "created" in {
        noException should be thrownBy Mp3File("test.mp3")
        the[IllegalArgumentException] thrownBy Mp3File("test.gif")
      }
    }
  }
  "GifFile case class" should {
    "accept only .mp3 extendion filename" when {
      "created" in {
        noException should be thrownBy GifFile("test.gif")
        the[IllegalArgumentException] thrownBy GifFile("test.mp3")
      }
    }
  }
  "PhotoFile case class" should {
    "accept only .mp3 extendion filename" when {
      "created" in {
        noException should be thrownBy PhotoFile("test.jpg")
        noException should be thrownBy PhotoFile("test.png")
        the[IllegalArgumentException] thrownBy PhotoFile("test.mp3")
      }
    }
  }
  "MediaFile apply" should {
    "create the right MediaFile" when {
      "a valid filename with allowed extension is provided" in {
        MediaFile("audio.mp3") shouldEqual Mp3File("audio.mp3")
        MediaFile("picture.jpg") shouldEqual PhotoFile("picture.jpg")
        MediaFile("picture.png") shouldEqual PhotoFile("picture.png")
        MediaFile("a.gif") shouldEqual GifFile("a.gif")
      }
    }
    "Not create the Mediafile" when {
      "a filename with an unknown extension is provided" in {
        (the[IllegalArgumentException] thrownBy MediaFile("test.fuck") should have)
          .message("filepath extension not recognized: test.fuck \n allowed extensions: mp3, gif, jpg, png")

      }
    }
  }
}
