package com.benkio.telegramBotInfrastructure.model

import info.mukel.telegrambot4s._
import methods._
import models._
import java.nio.file.Files
import java.nio.file.Path
import info.mukel.telegrambot4s.api.declarative.Messages
import info.mukel.telegrambot4s.api.ChatActions
import info.mukel.telegrambot4s.api.RequestHandler
import com.benkio.telegramBotInfrastructure.default.Actions.Action
import com.benkio.telegramBotInfrastructure.model._
import scala.concurrent.Future
import org.scalatest._
import java.util.concurrent._

class ReplySpec extends WordSpec with Matchers {

  "Mp3File case class" should {
    "accept only .mp3 extendion filename" when {
      "created" in {
        noException should be thrownBy Mp3File("test.mp3")
        the[IllegalArgumentException] thrownBy Mp3File("test.gif")
      }
    }
  }
  "GifFile case class" should {
    "accept only .mp3 extendion filename" when {
      "created" in {
        noException should be thrownBy GifFile("test.gif")
        the[IllegalArgumentException] thrownBy GifFile("test.mp3")
      }
    }
  }
  "PhotoFile case class" should {
    "accept only .mp3 extendion filename" when {
      "created" in {
        noException should be thrownBy PhotoFile("test.jpg")
        noException should be thrownBy PhotoFile("test.png")
        the[IllegalArgumentException] thrownBy PhotoFile("test.mp3")
      }
    }
  }
  "MediaFile apply" should {
    "create the right MediaFile" when {
      "a valid filename with allowed extension is provided" in {
        MediaFile("audio.mp3") shouldEqual Mp3File("audio.mp3")
        MediaFile("picture.jpg") shouldEqual PhotoFile("picture.jpg")
        MediaFile("picture.png") shouldEqual PhotoFile("picture.png")
        MediaFile("a.gif") shouldEqual GifFile("a.gif")
      }
    }
    "Not create the Mediafile" when {
      "a filename with an unknown extension is provided" in {
        (the[IllegalArgumentException] thrownBy MediaFile("test.fuck") should have)
          .message("filepath extension not recognized: test.fuck \n allowed extensions: mp3, gif, jpg, png")

      }
    }
  }
}
class ReplySpecAsync extends AsyncWordSpec with Matchers {

  implicit val audioAction: Action[Mp3File] =
    (mp3: Mp3File) => (m: Message) => Future.successful(m.copy(text = Some("Mp3")))
  implicit val gifAction: Action[GifFile] =
    (gif: GifFile) => (m: Message) => Future.successful(m.copy(text = Some("Gif")))
  implicit val photoAction: Action[PhotoFile] =
    (photo: PhotoFile) => (m: Message) => Future.successful(m.copy(text = Some("Photo")))
  implicit val textAction: Action[TextReply] =
    (textReply: TextReply) => (m: Message) => Future.successful(m.copy(text = Some("Text")))

  "ToMessageReply" should {
    "apply the right action" when {
      "the specific MediaFile is provided" in {
        val input: List[Reply] = List(
          Mp3File("audio.mp3"),
          PhotoFile("picture.jpg"),
          PhotoFile("picture.png"),
          GifFile("a.gif"),
          TextReply((_: Message) => List.empty)
        )

        val result: Future[List[Message]] =
          Future.traverse(input)(reply =>
            Reply.toMessageReply(
              reply,
              Message(
                messageId = 0,
                date = 0,
                chat = Chat(id = 0, `type` = ChatType.Private)
              )
            )
          )

        result.map { messageList =>
          {
            messageList.foreach {
              case (m: Message) if (m.text.isDefined && List("Mp3", "Gif", "Photo", "Text").contains(m.text.get)) =>
                succeed
            }
            succeed
          }
        }
      }
    }
  }
}
