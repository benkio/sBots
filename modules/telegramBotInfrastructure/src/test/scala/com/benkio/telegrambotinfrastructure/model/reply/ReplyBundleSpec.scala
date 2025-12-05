package com.benkio.telegrambotinfrastructure.model.reply

import cats.effect.*
import cats.syntax.all.*
import com.benkio.telegrambotinfrastructure.model.RegexTextTriggerValue
import com.benkio.telegrambotinfrastructure.model.StringTextTriggerValue
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import com.benkio.telegrambotinfrastructure.repository.Repository
import com.benkio.telegrambotinfrastructure.http.telegramreply.TelegramReply
import io.circe.parser.decode
import io.circe.syntax.*
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite
import telegramium.bots.high.Api
import telegramium.bots.Message

class ReplyBundleSpec extends CatsEffectSuite {

  given log: LogWriter[IO]                            = consoleLogUpToLevel(LogLevels.Info)
  given telegramReplyValue: TelegramReply[ReplyValue] = new TelegramReply[ReplyValue] {
    override def reply[F[_]: Async: LogWriter: Api](
        reply: ReplyValue,
        msg: Message,
        repository: Repository[F],
        replyToMessage: Boolean
    ): F[List[Message]] = {
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
  }

  val inputMediafile: List[MediaFile] = List(
    Mp3File("audio.mp3"),
    PhotoFile("picture.jpg"),
    PhotoFile("picture.png"),
    GifFile("aGif.mp4"),
    VideoFile("video.mp4"),
    Document("document.pdf")
  )

  test("prettyPrint of ReplyBundle should return the expected string") {
    val replyBundleInput: ReplyBundle[IO] = ReplyBundleMessage[IO](
      trigger = TextTrigger(
        StringTextTriggerValue("stringTextTriggerValue"),
        RegexTextTriggerValue("regexTextTriggerValue".r)
      ),
      reply = MediaReply[IO](mediaFiles = inputMediafile.pure[IO])
    )
    val result: IO[String] = replyBundleInput.prettyPrint()

    assertIO(
      result,
      """--------------------------------------------------
        |audio.mp3                 | stringTextTriggerValue
        |picture.jpg               | regexTextTriggerValue
        |picture.png               | 
        |aGif.mp4                  | 
        |video.mp4                 | 
        |document.pdf              | 
        |--------------------------------------------------
        |""".stripMargin
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
        |  "matcher" : "ContainsOnce"
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
        |            "filepath" : "ytai_NoPainNoGainGif.mp4",
        |            "replyToMessage" : false
        |          }
        |        }
        |      ],
        |      "replyToMessage" : false
        |    }
        |  },
        |  "matcher" : "ContainsOnce"
        |}""".stripMargin,
      """{
        |  "trigger" : {
        |    "TextTrigger" : {
        |      "triggers" : [
        |        {
        |          "RegexTextTriggerValue" : {
        |            "trigger" : "donne (vissute|con le palle)",
        |            "regexLength" : null
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
        |  "matcher" : "ContainsOnce"
        |}""".stripMargin
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
