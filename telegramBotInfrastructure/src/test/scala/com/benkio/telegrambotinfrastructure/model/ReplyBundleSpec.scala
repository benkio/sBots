package com.benkio.telegrambotinfrastructure.model

import telegramium.bots.client.Method
import log.effect.LogLevels
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import com.benkio.telegrambotinfrastructure.mocks.ResourceAccessMock
import telegramium.bots.high.Api
import log.effect.LogWriter
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.telegram.TelegramReply
import cats.Applicative
import cats.effect.*
import cats.syntax.all.*
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message

class ReplyBundleSpec extends CatsEffectSuite {

  given api: Api[IO] = new Api[IO] {
    def execute[Res](method: Method[Res]): IO[Res] = IO(???)
  }
  given log: LogWriter[IO] = consoleLogUpToLevel(LogLevels.Info)
  given telegramReplyValue: TelegramReply[ReplyValue] = new TelegramReply[ReplyValue] {
    def reply[F[_]: Async: LogWriter: Api](
        reply: ReplyValue,
        msg: Message,
        resourceAccess: ResourceAccess[F],
        replyToMessage: Boolean
    ): F[List[Message]] = (reply match {
      case _: Mp3File   => List(msg.copy(text = Some("Mp3")))
      case _: GifFile   => List(msg.copy(text = Some("Gif")))
      case _: PhotoFile => List(msg.copy(text = Some("Photo")))
      case _: VideoFile => List(msg.copy(text = Some("Video")))
      case _: Text      => List(msg.copy(text = Some("Text")))
    }).pure[F]
  }

  val inputMediafile: List[MediaFile] = List(
    Mp3File("audio.mp3"),
    PhotoFile("picture.jpg"),
    PhotoFile("picture.png"),
    GifFile("a.gif"),
    VideoFile("video.mp4"),
  )

  test("computeReplyBundle should return the expected message when the ReplyBundle and Message is provided") {

    def input(reply: Reply[IO]): ReplyBundleMessage[IO] =
      ReplyBundleMessage[IO](
        trigger = TextTrigger(
          StringTextTriggerValue("test")
        ),
        reply = reply,
        replySelection = SelectAll
      )

    val replyBundleInput1: ReplyBundleMessage[IO] = input(MediaReply[IO](mediaFiles = inputMediafile.pure[IO]))
    val replyBundleInput2: ReplyBundleMessage[IO] = input(
      TextReply.fromList[IO](
        "this string will be overwritten by the given"
      )(false)
    )

    val message = Message(
      messageId = 0,
      date = 0,
      chat = Chat(id = 0, `type` = "test")
    )

    def computeResult(input: ReplyBundleMessage[IO]): IO[List[Message]] =
      ReplyBundle.computeReplyBundle(
        replyBundle = input,
        message = message,
        filter = Applicative[IO].pure(true),
        resourceAccess = new ResourceAccessMock(List.empty)
      )

    val result1: IO[List[Message]] =
      computeResult(replyBundleInput1)
    val result2: IO[List[Message]] =
      computeResult(replyBundleInput2)

    for {
      _ <- assertIO(result1.map(_.length), 5)
      _ <- assertIO(result1.map(_.contains(message.copy(text = Some("Mp3")))), true)
      _ <- assertIO(result1.map(_.contains(message.copy(text = Some("Photo")))), true)
      _ <- assertIO(result1.map(_.contains(message.copy(text = Some("Gif")))), true)
      _ <- assertIO(result1.map(_.contains(message.copy(text = Some("Video")))), true)
      _ <- assertIO(result2.map(_.length), 1)
      _ <- assertIO(result2.map(_.contains(message.copy(text = Some("Text")))), true)
    } yield ()
  }

  test("prettyPrint of ReplyBundleMessage should return the expected string") {
    val replyBundleInput: ReplyBundleMessage[IO] = ReplyBundleMessage[IO](
      trigger = TextTrigger(
        StringTextTriggerValue("stringTextTriggerValue"),
        RegexTextTriggerValue("regexTextTriggerValue".r, 21)
      ),
      reply = MediaReply[IO](mediaFiles = inputMediafile.pure[IO])
    )
    val result: Array[String] = ReplyBundleMessage.prettyPrint(replyBundleInput).unsafeRunSync().split('\n')
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
