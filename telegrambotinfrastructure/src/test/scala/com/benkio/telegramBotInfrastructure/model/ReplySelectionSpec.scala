package com.benkio.telegramBotInfrastructure.model

import cats.effect._
import org.scalatest._
import org.scalatest.Inside._
import matchers.should._
import org.scalatest.wordspec.AnyWordSpec

class ReplySelectionSpec extends AnyWordSpec with Matchers {

  val input: List[MediaFile] = List(Mp3File("a.mp3"), GifFile("b.gif"), PhotoFile("c.jpg"))

  "RandomSelection logic" should {
    "be a function returning a list of one element" when {
      "a list of multiple element is provided" in {
        val result = RandomSelection.logic[IO](input).unsafeRunSync()
        inside(result) {
          case (mediaFile: MediaFile) :: Nil => List("a.mp3", "b.gif", "c.jpg") should contain(mediaFile.filename)
        }
      }
    }
  }
  "SelectAll logic" should {
    "always return the same input" in {
      SelectAll.logic[IO](input).unsafeRunSync shouldEqual input
    }
  }
}
