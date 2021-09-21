package com.benkio.telegrambotinfrastructure.model

import cats.effect._
import org.scalatest.Inside._
import org.scalatest._
import org.scalatest.wordspec.AnyWordSpec

import matchers.should._

class ReplySelectionSpec extends AnyWordSpec with Matchers {

  val input: List[MediaFile] =
    List(Mp3File("a.mp3"), GifFile("b.gif"), PhotoFile("c.jpg"), PhotoFile("d.png"), VideoFile("e.mp4"))

  "RandomSelection logic" should {
    "be a function returning a list of one element" when {
      "a list of multiple element is provided" in {
        val result = RandomSelection.logic[IO](input).unsafeRunSync()
        inside(result) { case (mediaFile: MediaFile) :: Nil =>
          List("a.mp3", "b.gif", "c.jpg", "d.png", "e.mp4") should contain(mediaFile.filename)
        }
      }
    }
  }
  "SelectAll logic" should {
    "always return the same input" in {
      SelectAll.logic[IO](input).unsafeRunSync() shouldEqual input
    }
  }
}
