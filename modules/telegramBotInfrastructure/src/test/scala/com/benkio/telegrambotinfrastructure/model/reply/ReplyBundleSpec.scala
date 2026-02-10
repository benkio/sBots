package com.benkio.telegrambotinfrastructure.model.reply

import cats.effect.*
import com.benkio.telegrambotinfrastructure.messagefiltering.MessageMatches
import com.benkio.telegrambotinfrastructure.model.RegexTextTriggerValue
import com.benkio.telegrambotinfrastructure.model.StringTextTriggerValue
import com.benkio.telegrambotinfrastructure.model.TextTrigger
import io.circe.parser.decode
import io.circe.syntax.*
import log.effect.fs2.SyncLogWriter.consoleLogUpToLevel
import log.effect.LogLevels
import log.effect.LogWriter
import munit.CatsEffectSuite

class ReplyBundleSpec extends CatsEffectSuite {

  given log: LogWriter[IO]            = consoleLogUpToLevel(LogLevels.Info)
  val inputMediafile: List[MediaFile] = List(
    Mp3File("audio.mp3"),
    PhotoFile("picture.jpg"),
    PhotoFile("picture.png"),
    GifFile("aGif.mp4"),
    VideoFile("video.mp4"),
    Document("document.pdf")
  )

  test("prettyPrint of ReplyBundle should return the expected string") {
    val replyBundleInput: ReplyBundle = ReplyBundleMessage(
      trigger = TextTrigger(
        StringTextTriggerValue("stringTextTriggerValue"),
        RegexTextTriggerValue("regexTextTriggerValue".r, 19)
      ),
      reply = MediaReply(mediaFiles = inputMediafile),
      matcher = MessageMatches.ContainsAll
    )
    val result: String = replyBundleInput.prettyPrint()

    assertEquals(
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
        |            "regexLength" : 14
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
      val eitherMessageTrigger = decode[ReplyBundleMessage](inputString)
      eitherMessageTrigger.fold(
        e => fail("failed in parsing the input string as reply bundle message", e),
        ms => assertEquals(ms.asJson.toString, inputString)
      )
    }
  }
}
