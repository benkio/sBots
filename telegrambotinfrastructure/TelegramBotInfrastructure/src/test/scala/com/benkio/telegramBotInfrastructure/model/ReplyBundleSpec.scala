package com.benkio.telegramBotInfrastructure.model

import org.scalatest._
import com.benkio.telegramBotInfrastructure.default.Actions.Action
import info.mukel.telegrambot4s.models.Message
import scala.concurrent.Future

class ReplyBundleSpec extends WordSpec with Matchers {

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

        ???
      }
    }
  }
}
