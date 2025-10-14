package com.benkio.telegrambotinfrastructure.model.reply

import cats.data.NonEmptyList
import cats.effect.*
import cats.syntax.all.*
import cats.Applicative
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.RepositoryMock
import com.benkio.telegrambotinfrastructure.model.media.MediaResource.MediaResourceIFile
import com.benkio.telegrambotinfrastructure.model.StringTextTriggerValue
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.telegram.TelegramReply
import com.benkio.telegrambotinfrastructure.ComputeReply
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite
import telegramium.bots.high.Api
import telegramium.bots.Chat
import telegramium.bots.Message

class ComputeReplySpec extends CatsEffectSuite {

  given log: LogWriter[IO]                            = consoleLogUpToLevel(LogLevels.Info)
  given telegramReplyValue: TelegramReply[ReplyValue] = new TelegramReply[ReplyValue] {
    override def reply[F[_]: Async: LogWriter: Api](
        reply: ReplyValue,
        msg: Message,
        repository: Repository[F],
        replyToMessage: Boolean
    ): F[List[Message]] =
      val _ = summon[LogWriter[F]]
      val _ = summon[Api[F]]
      (reply match {
        case _: Mp3File   => List(msg.copy(text = Some("Mp3")))
        case _: GifFile   => List(msg.copy(text = Some("Gif")))
        case _: PhotoFile => List(msg.copy(text = Some("Photo")))
        case _: VideoFile => List(msg.copy(text = Some("Video")))
        case _: Text      => List(msg.copy(text = Some("Text")))
        case _: Document  => List(msg.copy(text = Some("Document")))
        case _: Sticker   => List(msg.copy(text = Some("Sticker")))
      }).pure[F]
  }

  val inputMediafile: List[MediaFile] = List(
    Mp3File("audio.mp3"),
    PhotoFile("picture.jpg"),
    PhotoFile("picture.png"),
    GifFile("a.gif"),
    VideoFile("video.mp4"),
    Document("document.pdf")
  )

  test("ComputeReply.execute should return the expected message when the ReplyBundle and Message is provided") {

    def input(reply: Reply[IO]): ReplyBundleMessage[IO] =
      ReplyBundleMessage[IO](
        trigger = TextTrigger(
          StringTextTriggerValue("test")
        ),
        reply = reply
      )

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
      ComputeReply.execute(
        replyBundle = input,
        message = message,
        filter = Applicative[IO].pure(true),
        repository =
          RepositoryMock((_, _) => NonEmptyList.one(NonEmptyList.one(MediaResourceIFile("not used"))).pure[IO])
      )

    val result2: IO[List[Message]] =
      computeResult(replyBundleInput2)

    for {
      _ <- assertIO(result2.map(_.length), 1)
      _ <- assertIO(result2.map(_.contains(message.copy(text = Some("Text")))), true)
    } yield ()
  }
}
