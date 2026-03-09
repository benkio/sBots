package com.benkio.chatcore.model.reply

import com.benkio.chatcore.messagefiltering.MessageMatches
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.RegexTextTriggerValue
import com.benkio.chatcore.model.SBotInfo.SBotName
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTrigger
import io.circe.parser.decode
import io.circe.syntax.*
import munit.CatsEffectSuite

import java.time.Instant

class ReplyBundleSpec extends CatsEffectSuite {

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

  test("ReplyBundleMessage.selectReplyBundle should pick the highest priority message bundle") {
    val message: Message = Message(
      messageId = 1,
      date = Instant.now.getEpochSecond(),
      chatId = ChatId(1L),
      chatType = "private",
      text = Some("this is a long trigger"),
      caption = None
    )
    val shortTrigger: ReplyBundleMessage = ReplyBundleMessage.textToText("trigger")("short")
    val longTrigger: ReplyBundleMessage  = ReplyBundleMessage.textToText("this is a long trigger")("long")

    val result = ReplyBundleMessage.selectReplyBundle(
      msg = message,
      messageRepliesData = List(shortTrigger, longTrigger),
      ignoreMessagePrefix = None,
      disableForward = false
    )

    assertEquals(result, Some(longTrigger))
  }

  test("ReplyBundleCommand.selectCommandReplyBundle should match command and username forms") {
    val commandWithoutAt = ReplyBundleCommand(
      trigger = CommandTrigger("testcommand"),
      reply = TextReply.fromList("one")(false),
      instruction = CommandInstructionData.NoInstructions
    )
    val commandWithArg = ReplyBundleCommand(
      trigger = CommandTrigger("other"),
      reply = TextReply.fromList("two")(false),
      instruction = CommandInstructionData.NoInstructions
    )

    val matchingMessage: Message = Message(
      messageId = 2,
      date = Instant.now.getEpochSecond(),
      chatId = ChatId(1L),
      chatType = "private",
      text = Some("/testcommand@SampleWebhookBot"),
      caption = None
    )
    val nonMatchingMessage: Message = Message(
      messageId = 3,
      date = Instant.now.getEpochSecond(),
      chatId = ChatId(1L),
      chatType = "private",
      text = Some("/othercommand"),
      caption = None
    )

    val selected = ReplyBundleCommand.selectCommandReplyBundle(
      msg = matchingMessage,
      allCommandRepliesData = List(commandWithArg, commandWithoutAt),
      botName = SBotName("SampleWebhookBot")
    )
    val noneSelected = ReplyBundleCommand.selectCommandReplyBundle(
      msg = nonMatchingMessage,
      allCommandRepliesData = List(commandWithArg, commandWithoutAt),
      botName = SBotName("SampleWebhookBot")
    )

    assertEquals(selected.map(_.trigger.command), Some("testcommand"))
    assertEquals(noneSelected, None)
  }
}
