package com.benkio.telegramBotInfrastructure.model

import cats.effect._
import org.scalatest._
import com.benkio.telegramBotInfrastructure.default.Actions.Action
import telegramium.bots.{Chat, Message}
import matchers.should._
import org.scalatest.wordspec.AnyWordSpec

class ReplyBundleSpec extends AnyWordSpec with Matchers {

  implicit val replyAction: Action[Reply, IO] =
    (r : Reply) => (m: Message) => IO.pure(r match {
      case _ : Mp3File => m.copy(text = Some("Mp3"))
      case _ : GifFile => m.copy(text = Some("Gif"))
      case _ : PhotoFile => m.copy(text = Some("Photo"))
      case _ : TextReply => m.copy(text = Some("Text"))
    })

  "computeReplyBundle" should {
    "return the expected message" when {
      "the ReplyBundle and Message is provided" in {
        val inputMediafile: List[MediaFile] = List(
          Mp3File("audio.mp3"),
          PhotoFile("picture.jpg"),
          PhotoFile("picture.png"),
          GifFile("a.gif")
        )

        val replyBundleInput: ReplyBundleMessage = ReplyBundleMessage(
          trigger = TextTrigger(List(StringTextTriggerValue("test"))),
          mediafiles = inputMediafile
        )

        val message = Message(
          messageId = 0,
          date = 0,
          chat = Chat(id = 0, `type` = "test")
        )

        val result : List[Message] = ReplyBundle.computeReplyBundle(replyBundleInput, message).unsafeRunSync()

        result.length shouldBe 5
        result should contain(message.copy(text = Some("Mp3")))
        result should contain(message.copy(text = Some("Gif")))
        result should contain(message.copy(text = Some("Photo")))
        result should contain(message.copy(text = Some("Text")))
      }
    }
  }
}
