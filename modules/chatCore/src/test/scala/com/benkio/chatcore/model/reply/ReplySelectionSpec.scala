package com.benkio.chatcore.model.reply

import cats.effect.*
import com.benkio.chatcore.messagefiltering.RandomSelection
import munit.CatsEffectSuite

class ReplySelectionSpec extends CatsEffectSuite {

  val input: List[MediaFile] =
    List(Mp3File("a.mp3"), GifFile("bGif.mp4"), PhotoFile("c.jpg"), PhotoFile("d.png"), VideoFile("e.mp4"))

  test(
    "RandomSelection select should be a function returning a list of one element when a list of multiple element is provided"
  ) {
    val result: IO[ReplyValue] = RandomSelection.select[IO](
      reply = MediaReply.fromList(input)
    )
    assertIO(
      result.map(mediaFile =>
        mediaFile match {
          case mediaFile: MediaFile =>
            List("a.mp3", "bGif.mp4", "c.jpg", "d.png", "e.mp4").contains(mediaFile.filename)
          case _ => fail("I expect a mediafile in here")
        }
      ),
      true
    )
  }
}
