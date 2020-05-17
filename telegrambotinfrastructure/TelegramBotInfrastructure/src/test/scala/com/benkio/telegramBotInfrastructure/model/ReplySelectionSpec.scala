package com.benkio.telegramBotInfrastructure.model

import org.scalatest._
import org.scalatest.Inside._

class ReplySelectionSpec extends WordSpec with Matchers {

  val input: List[MediaFile] = List(Mp3File("a.mp3"), GifFile("b.gif"), PhotoFile("c.jpg"))

  "RandomSelection logic" should {
    "be a function returning a list of one element" when {
      "a list of multiple element is provided" in {
        val result = RandomSelection.logic(input)
        inside(result) {
          case (mediaFile: MediaFile) :: Nil => List("a.mp3", "b.gif", "c.jpg") should contain(mediaFile.filename)
        }
      }
    }
  }
  "SelectAll logic" should {
    "always return the same input" in {
      SelectAll.logic(input) shouldEqual input
    }
  }
}
