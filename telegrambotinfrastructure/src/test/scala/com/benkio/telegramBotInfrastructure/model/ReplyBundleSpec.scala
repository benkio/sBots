package com.benkio.telegramBotInfrastructure.model

import info.mukel.telegrambot4s._
import methods._
import models._
import org.scalatest._
import com.benkio.telegramBotInfrastructure.default.Actions.Action
import info.mukel.telegrambot4s.models.Message
import scala.concurrent.Future

class ReplyBundleSpec extends AsyncWordSpec with Matchers {

  implicit val audioAction: Action[Mp3File] =
    (mp3: Mp3File) => (m: Message) => Future.successful(m.copy(text = Some("Mp3")))
  implicit val gifAction: Action[GifFile] =
    (gif: GifFile) => (m: Message) => Future.successful(m.copy(text = Some("Gif")))
  implicit val photoAction: Action[PhotoFile] =
    (photo: PhotoFile) => (m: Message) => Future.successful(m.copy(text = Some("Photo")))
  implicit val textAction: Action[TextReply] =
    (textReply: TextReply) => (m: Message) => Future.successful(m.copy(text = Some("Text")))

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
          chat = Chat(id = 0, `type` = ChatType.Private)
        )

        val result = ReplyBundle.computeReplyBundle(replyBundleInput, message)

        result.map { listMessages =>
          listMessages.length shouldBe 5
          listMessages should contain(message.copy(text = Some("Mp3")))
          listMessages should contain(message.copy(text = Some("Gif")))
          listMessages should contain(message.copy(text = Some("Photo")))
          listMessages should contain(message.copy(text = Some("Text")))
        }
      }
    }
  }
}
