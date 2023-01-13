package com.benkio.telegrambotinfrastructure.model

import cats.Applicative
import cats.effect._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

class ReplyBundleSpec extends CatsEffectSuite {

  implicit val replyAction: Action[IO] =
    (r: Reply) =>
      (m: Message) =>
        IO.pure(r match {
          case _: Mp3File      => List(m.copy(text = Some("Mp3")))
          case _: GifFile      => List(m.copy(text = Some("Gif")))
          case _: PhotoFile    => List(m.copy(text = Some("Photo")))
          case _: VideoFile    => List(m.copy(text = Some("Video")))
          case _: TextReply[_] => List(m.copy(text = Some("Text")))
        })

  val inputMediafile: List[MediaFile] = List(
    Mp3File("audio.mp3"),
    PhotoFile("picture.jpg"),
    PhotoFile("picture.png"),
    GifFile("a.gif"),
    VideoFile("video.mp4")
  )

  test("computeReplyBundle should return the expected message when the ReplyBundle and Message is provided") {

    val replyBundleInput: ReplyBundleMessage[IO] = ReplyBundleMessage[IO](
      trigger = TextTrigger(
        StringTextTriggerValue("test")
      ),
      text = Some(TextReply(_ => IO.pure(List("some text that will be overwritten by the implicit")))),
      mediafiles = inputMediafile
    )

    val message = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 0, `type` = "test")
    )

    val result: IO[List[Message]] =
      ReplyBundle.computeReplyBundle(replyBundleInput, message, Applicative[IO].pure(true))

    for {
      _ <- assertIO(result.map(_.length), 6)
      _ <- assertIO(result.map(_.contains(message.copy(text = Some("Mp3")))), true)
      _ <- assertIO(result.map(_.contains(message.copy(text = Some("Photo")))), true)
      _ <- assertIO(result.map(_.contains(message.copy(text = Some("Gif")))), true)
      _ <- assertIO(result.map(_.contains(message.copy(text = Some("Text")))), true)
      _ <- assertIO(result.map(_.contains(message.copy(text = Some("Video")))), true)
    } yield ()
  }

  test("prettyPrint of ReplyBundleMessage should return the expected string") {
    val replyBundleInput: ReplyBundleMessage[IO] = ReplyBundleMessage[IO](
      trigger = TextTrigger(
        StringTextTriggerValue("stringTextTriggerValue"),
        RegexTextTriggerValue("regexTextTriggerValue".r, 21)
      ),
      text = Some(TextReply(_ => IO.pure(List("some text that will be overwritten by the implicit")))),
      mediafiles = inputMediafile
    )
    val result: Array[String] = ReplyBundleMessage.prettyPrint(replyBundleInput).split('\n')
    assertEquals(result.length, 7)
    assertEquals(result(0), "--------------------------------------------------")
    assertEquals(result(1), "audio.mp3                 | stringTextTriggerValue")
    assertEquals(result(2), "picture.jpg               | regexTextTriggerValue")
    assertEquals(result(3), "picture.png               | ")
    assertEquals(result(4), "a.gif                     | ")
    assertEquals(result(5), "video.mp4                 | ")
    assertEquals(result(6), "--------------------------------------------------")
  }
}
