package com.benkio.telegrambotinfrastructure.model

import cats.effect._
import com.benkio.telegrambotinfrastructure.default.Actions.Action
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

class ReplyBundleSpec extends CatsEffectSuite {

  implicit val replyAction: Action[Reply, IO] =
    (r: Reply) =>
      (m: Message) =>
        IO.pure(r match {
          case _: Mp3File      => List(m.copy(text = Some("Mp3")))
          case _: GifFile      => List(m.copy(text = Some("Gif")))
          case _: PhotoFile    => List(m.copy(text = Some("Photo")))
          case _: VideoFile    => List(m.copy(text = Some("Video")))
          case _: TextReply[_] => List(m.copy(text = Some("Text")))
        })

  test("computeReplyBundle should return the expected message when the ReplyBundle and Message is provided") {
    val inputMediafile: List[MediaFile] = List(
      Mp3File("audio.mp3"),
      PhotoFile("picture.jpg"),
      PhotoFile("picture.png"),
      GifFile("a.gif"),
      VideoFile("video.mp4")
    )

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

    val result: IO[List[Message]] = ReplyBundle.computeReplyBundle(replyBundleInput, message)

    assertIO(result.map(_.length), 6)
    assertIO(result.map(_.contains(message.copy(text = Some("Mp3")))), true)
    assertIO(result.map(_.contains(message.copy(text = Some("Photo")))), true)
    assertIO(result.map(_.contains(message.copy(text = Some("Gif")))), true)
    assertIO(result.map(_.contains(message.copy(text = Some("Text")))), true)
    assertIO(result.map(_.contains(message.copy(text = Some("Video")))), true)
  }
}
