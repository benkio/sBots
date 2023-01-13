package com.benkio.telegrambotinfrastructure.model

import cats.effect._
import munit.CatsEffectSuite

class ReplySelectionSpec extends CatsEffectSuite {

  val input: List[MediaFile] =
    List(Mp3File("a.mp3"), GifFile("b.gif"), PhotoFile("c.jpg"), PhotoFile("d.png"), VideoFile("e.mp4"))

  test(
    "RandomSelection logic should be a function returning a list of one element when a list of multiple element is provided"
  ) {
    val result: IO[List[Reply]] = RandomSelection.logic[IO](input)
    for {
      _ <- assertIO(result.map(_.length), 1)
      _ <- assertIO(
        result.map(mediaFiles =>
          mediaFiles.forall {
            case (mediaFile: MediaFile) =>
              List("a.mp3", "b.gif", "c.jpg", "d.png", "e.mp4").contains(mediaFile.filename)
            case _ => fail("I expect a mediafile in here")
          }
        ),
        true
      )
    } yield ()
  }
  test("SelectAll logic should always return the same input") {
    assertIO(SelectAll.logic[IO](input), input)
  }
}
