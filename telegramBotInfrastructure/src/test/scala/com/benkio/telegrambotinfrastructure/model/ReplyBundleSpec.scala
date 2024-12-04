package com.benkio.telegrambotinfrastructure.model

import cats.Applicative
import cats.effect.*
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.mocks.ApiMock.given
import com.benkio.telegrambotinfrastructure.mocks.ResourceAccessMock
import com.benkio.telegrambotinfrastructure.resources.ResourceAccess
import com.benkio.telegrambotinfrastructure.telegram.TelegramReply
import io.circe.parser.decode
import io.circe.syntax.*
import log.effect.LogLevels
import log.effect.LogWriter
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import munit.CatsEffectSuite
import telegramium.bots.Chat
import telegramium.bots.Message
import telegramium.bots.high.Api

class ReplyBundleSpec extends CatsEffectSuite {

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

  test("prettyPrint of ReplyBundle should return the expected string") {
    val replyBundleInput: ReplyBundle[IO] = ReplyBundleMessage[IO](
      trigger = TextTrigger(
        StringTextTriggerValue("stringTextTriggerValue"),
        RegexTextTriggerValue("regexTextTriggerValue".r, 21)
      ),
      reply = MediaReply[IO](mediaFiles = inputMediafile.pure[IO])
    )
    val result: IO[String] = replyBundleInput.prettyPrint()

    assertIO(
      result,
      """--------------------------------------------------
audio.mp3                 | stringTextTriggerValue
picture.jpg               | regexTextTriggerValue
picture.png               | 
a.gif                     | 
video.mp4                 | 
--------------------------------------------------
"""
    )
  }

  test("Reply Bundle Message JSON decode/encode should work as expected") {
    val jsonInputs = List(
      """{
        |  "trigger" : {
        |    "TextTrigger" : {
        |      "triggers" : [
        |        {
        |          "StringTextTriggerValue" : "crociate"
        |        }
        |      ]
        |    }
        |  },
        |  "reply" : {
        |    "MediaReply" : {
        |      "mediaFiles" : [
        |        {
        |          "Mp3File" : {
        |            "filepath" : "abar_Crociate.mp3",
        |            "replyToMessage" : false
        |          }
        |        }
        |      ],
        |      "replyToMessage" : false
        |    }
        |  },
        |  "matcher" : "ContainsOnce",
        |  "replySelection" : "RandomSelection"
        |}""".stripMargin,
      """{
        |  "trigger" : {
        |    "TextTrigger" : {
        |      "triggers" : [
        |        {
        |          "StringTextTriggerValue" : "no pain"
        |        },
        |        {
        |          "StringTextTriggerValue" : "no gain"
        |        }
        |      ]
        |    }
        |  },
        |  "reply" : {
        |    "MediaReply" : {
        |      "mediaFiles" : [
        |        {
        |          "GifFile" : {
        |            "filepath" : "ytai_NoPainNoGain.mp4",
        |            "replyToMessage" : false
        |          }
        |        }
        |      ],
        |      "replyToMessage" : false
        |    }
        |  },
        |  "matcher" : "ContainsOnce",
        |  "replySelection" : "RandomSelection"
        |}""".stripMargin,
      """{
        |  "trigger" : {
        |    "TextTrigger" : {
        |      "triggers" : [
        |        {
        |          "RegexTextTriggerValue" : {
        |            "trigger" : "donne (vissute|con le palle)",
        |            "minimalLengthMatch" : 13
        |          }
        |        },
        |        {
        |          "StringTextTriggerValue" : "groupies"
        |        }
        |      ]
        |    }
        |  },
        |  "reply" : {
        |    "MediaReply" : {
        |      "mediaFiles" : [
        |        {
        |          "VideoFile" : {
        |            "filepath" : "rphjb_OcchiAnniSettantaFemmismoControcultura.mp4",
        |            "replyToMessage" : false
        |          }
        |        },
        |        {
        |          "Mp3File" : {
        |            "filepath" : "rphjb_OcchiAnniSettantaFemmismoControcultura.mp3",
        |            "replyToMessage" : false
        |          }
        |        }
        |      ],
        |      "replyToMessage" : false
        |    }
        |  },
        |  "matcher" : "ContainsOnce",
        |  "replySelection" : "RandomSelection"
        |}""".stripMargin.stripMargin
    )

    for inputString <- jsonInputs
    yield {
      val eitherMessageTrigger = decode[ReplyBundleMessage[SyncIO]](inputString)
      eitherMessageTrigger.fold(
        e => fail("failed in parsing the input string as reply bundle message", e),
        ms => assertEquals(ms.asJson.toString, inputString)
      )
    }
  }
}
