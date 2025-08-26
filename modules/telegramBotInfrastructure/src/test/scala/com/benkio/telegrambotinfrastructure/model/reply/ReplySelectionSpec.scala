package com.benkio.telegrambotinfrastructure.model.reply

import cats.effect.*
import com.benkio.telegrambotinfrastructure.messagefiltering.RandomSelection
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

class ReplySelectionSpec extends CatsEffectSuite {

  val input: List[MediaFile] =
    List(Mp3File("a.mp3"), GifFile("b.gif"), PhotoFile("c.jpg"), PhotoFile("d.png"), VideoFile("e.mp4"))

  val message = Message(
    messageId = 0,
    date = 0,
    chat = Chat(id = 0, `type` = "test")
  )

  test(
    "RandomSelection select should be a function returning a list of one element when a list of multiple element is provided"
  ) {
    val result: IO[List[ReplyValue]] = RandomSelection.select[IO](
      reply = MediaReply.fromList[IO](input),
      message = message
    )
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
}
