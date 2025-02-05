package com.benkio.telegrambotinfrastructure.model.reply

import cats.effect.*
import com.benkio.telegrambotinfrastructure.model.RandomSelection
import com.benkio.telegrambotinfrastructure.model.ReplySelection
import com.benkio.telegrambotinfrastructure.model.SelectAll
import io.circe.parser.decode
import io.circe.syntax.*
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
    "RandomSelection logic should be a function returning a list of one element when a list of multiple element is provided"
  ) {
    val result: IO[List[ReplyValue]] = RandomSelection.logic[IO](
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
  test("SelectAll logic should always return the same input") {
    assertIO(
      SelectAll.logic[IO](
        reply = MediaReply.fromList[IO](input),
        message = message
      ),
      input
    )
  }

  test("ReplySelection JSON Decoder/Encoder should works as expected") {
    val jsonInputs = List(
      """"SelectAll"""",
      """"RandomSelection"""".stripMargin
    )

    for inputString <- jsonInputs
    yield {
      val eitherMessageTrigger = decode[ReplySelection](inputString)
      eitherMessageTrigger.fold(
        e => fail("failed in parsing the input string as reply selection", e),
        ms => assertEquals(ms.asJson.toString, inputString)
      )
    }
  }
}
