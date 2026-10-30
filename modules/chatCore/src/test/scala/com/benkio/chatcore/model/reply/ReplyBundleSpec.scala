package com.benkio.chatcore.model.reply

import cats.effect.IO
import com.benkio.chatcore.messagefiltering.FilteringOlder
import com.benkio.chatcore.messagefiltering.MessageMatches
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.CommandInstructionData
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.RegexTextTriggerValue
import com.benkio.chatcore.model.SBotInfo.SBotName
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTrigger
import com.benkio.chatcore.repository.JsonDataRepository
import com.benkio.chatcore.Arbitraries.given
import com.benkio.chatcore.Logger.given
import io.circe.parser.decode
import io.circe.syntax.*
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.effect.PropF
import org.scalacheck.Prop.forAll

import java.time.Instant

class ReplyBundleSpec extends CatsEffectSuite with ScalaCheckEffectSuite {

  private val commandsJsonFilename: String = "sbot_commands.json"
  val repo                                 = JsonDataRepository[IO]()

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
      reply = MediaReply(mediaFiles = inputMediafile.toSet),
      matcher = MessageMatches.ContainsAll
    )
    val result: String = replyBundleInput.prettyPrint()

    val expected =
      "| aGif.mp4                                                         | stringTextTriggerValue                                                                                                                                                                     |\n" +
        "| audio.mp3                                                        | regexTextTriggerValue                                                                                                                                                                      |\n" +
        "| document.pdf                                                     |                                                                                                                                                                                            |\n" +
        "| picture.jpg                                                      |                                                                                                                                                                                            |\n" +
        "| picture.png                                                      |                                                                                                                                                                                            |\n" +
        "| video.mp4                                                        |                                                                                                                                                                                            |\n" +
        "|                                                                  |                                                                                                                                                                                            |\n"
    assertEquals(result, expected)
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

  property("ReplyBundleMessage.selectReplyBundle should pick the highest priority message bundle") {
    val shortWordGen = Gen.alphaLowerStr.suchThat(_.length >= 3)
    val suffixGen    = Gen.alphaLowerStr.suchThat(_.nonEmpty)
    forAll(shortWordGen, suffixGen) { (shortWord: String, suffix: String) =>
      val longWord = s"$shortWord $suffix"
      val message: Message = Message(
        messageId = 1,
        date = Instant.now.getEpochSecond(),
        chatId = ChatId(1L),
        chatType = "private",
        text = Some(s"message containing $longWord"),
        caption = None
      )
      val shortTrigger: ReplyBundleMessage = ReplyBundleMessage.textToText(shortWord)("short")
      val longTrigger: ReplyBundleMessage  = ReplyBundleMessage.textToText(longWord)("long")

      val result = ReplyBundleMessage.selectReplyBundle(
        msg = message,
        messageRepliesData = List(shortTrigger, longTrigger),
        ignoreMessagePrefix = None,
        disableForward = false
      )

      assertEquals(result, Some(longTrigger))
    }
  }

  property("ReplyBundleCommand.selectCommandReplyBundle should match command@botname form") {
    val commandGen = Gen.alphaLowerStr.suchThat(_.nonEmpty)
    forAll(commandGen) { (commandName: String) =>
      val commandWithoutAt = ReplyBundleCommand(
        trigger = CommandTrigger(commandName),
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
        text = Some(s"/$commandName@SampleWebhookBot"),
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

      assertEquals(selected.map(_.trigger.command), Some(commandName))
      assertEquals(noneSelected, None)
    }
  }

  property("ReplyBundleCommand.selectCommandReplyBundle should return None when message text is missing") {
    val commandGen = Gen.alphaLowerStr.suchThat(_.nonEmpty)
    forAll(commandGen) { (commandName: String) =>
      val commandReply = ReplyBundleCommand(
        trigger = CommandTrigger(commandName),
        reply = TextReply.fromList("one")(false),
        instruction = CommandInstructionData.NoInstructions
      )
      val messageWithoutText = Message(
        messageId = 7,
        date = Instant.now.getEpochSecond(),
        chatId = ChatId(1L),
        chatType = "private",
        text = None,
        caption = None
      )

      val result = ReplyBundleCommand.selectCommandReplyBundle(
        msg = messageWithoutText,
        allCommandRepliesData = List(commandReply),
        botName = SBotName("SampleWebhookBot")
      )

      assertEquals(result, None)
    }
  }

  property("ReplyBundleCommand.selectCommandReplyBundle should match exact command and command with arguments") {
    val validCommand = Gen.alphaLowerStr.suchThat(_.nonEmpty)
    val validArgs    = Gen.alphaNumStr.suchThat(_.nonEmpty)
    forAll(validCommand, validArgs) { (commandName: String, args: String) =>
      val commandReply = ReplyBundleCommand(
        trigger = CommandTrigger(commandName),
        reply = TextReply.fromList("one")(false),
        instruction = CommandInstructionData.NoInstructions
      )
      val exactMessage = Message(
        messageId = 9,
        date = Instant.now.getEpochSecond(),
        chatId = ChatId(1L),
        chatType = "private",
        text = Some(s"/$commandName"),
        caption = None
      )
      val withArgsMessage = Message(
        messageId = 10,
        date = Instant.now.getEpochSecond(),
        chatId = ChatId(1L),
        chatType = "private",
        text = Some(s"/$commandName $args"),
        caption = None
      )

      val exactResult = ReplyBundleCommand.selectCommandReplyBundle(
        msg = exactMessage,
        allCommandRepliesData = List(commandReply),
        botName = SBotName("SampleWebhookBot")
      )
      val withArgsResult = ReplyBundleCommand.selectCommandReplyBundle(
        msg = withArgsMessage,
        allCommandRepliesData = List(commandReply),
        botName = SBotName("SampleWebhookBot")
      )

      assertEquals(exactResult.map(_.trigger.command), Some(commandName))
      assertEquals(withArgsResult.map(_.trigger.command), Some(commandName))
    }
  }

  property("ReplyBundle extension helpers should report media and non-media replies correctly") {
    val triggerGen = Gen.alphaNumStr.suchThat(_.nonEmpty)
    forAll(triggerGen) { (triggerText: String) =>
      val mediaBundle = ReplyBundleMessage(
        trigger = TextTrigger(StringTextTriggerValue(triggerText)),
        reply = MediaReply.fromList(List(Mp3File("track.mp3"))),
        matcher = MessageMatches.ContainsOnce
      )
      val textBundle = ReplyBundleMessage.textToText(s"$triggerText-text")("hello")

      assert(mediaBundle.containsMediaReply(mediaBundle))
      assert(!textBundle.containsMediaReply(textBundle))
      assertEquals(mediaBundle.getMediaFiles, Set[MediaFile](Mp3File("track.mp3")))
      assertEquals(textBundle.getMediaFiles, Set.empty)
    }
  }

  property("ReplyBundleMessage convenience constructors should build MediaReply payloads with ContainsOnce matcher") {
    val nonEmptyText = Gen.alphaNumStr.suchThat(_.nonEmpty)
    forAll(nonEmptyText) { (triggerText: String) =>
      val videoBundle   = ReplyBundleMessage.textToVideo(triggerText)(VideoFile("clip.mp4"))
      val mp3Bundle     = ReplyBundleMessage.textToMp3(triggerText)(Mp3File("track.mp3"))
      val gifBundle     = ReplyBundleMessage.textToGif(triggerText)(GifFile("anim.Gif.mp4"))
      val photoBundle   = ReplyBundleMessage.textToPhoto(triggerText)(PhotoFile("image.jpg"))
      val stickerBundle = ReplyBundleMessage.textToSticker(triggerText)(Sticker("pack.sticker"))

      assertEquals(videoBundle.matcher, MessageMatches.ContainsOnce)
      assertEquals(mp3Bundle.matcher, MessageMatches.ContainsOnce)
      assertEquals(gifBundle.matcher, MessageMatches.ContainsOnce)
      assertEquals(photoBundle.matcher, MessageMatches.ContainsOnce)
      assertEquals(stickerBundle.matcher, MessageMatches.ContainsOnce)

      assert(videoBundle.reply.isInstanceOf[MediaReply])
      assert(mp3Bundle.reply.isInstanceOf[MediaReply])
      assert(gifBundle.reply.isInstanceOf[MediaReply])
      assert(photoBundle.reply.isInstanceOf[MediaReply])
      assert(stickerBundle.reply.isInstanceOf[MediaReply])
    }
  }

  property("ReplyBundle.selectReplyBundle should return None for forwarded messages when forwarding is disabled") {
    val triggerGen = Gen.alphaLowerStr.suchThat(_.nonEmpty)
    forAll(triggerGen) { (triggerText: String) =>
      val now              = Instant.now.getEpochSecond()
      val forwardedMessage = Message(
        messageId = 8,
        date = now,
        chatId = ChatId(1L),
        chatType = "private",
        text = Some(triggerText),
        isForward = true
      )
      val bundle = ReplyBundleMessage.textToText(triggerText)("matched")

      val result = ReplyBundleMessage.selectReplyBundle(
        msg = forwardedMessage,
        messageRepliesData = List(bundle),
        ignoreMessagePrefix = None,
        disableForward = true
      )

      assertEquals(result, None)
    }
  }

  property("ReplyBundle.selectReplyBundle should match forwarded messages when forwarding is enabled") {
    val triggerGen = Gen.alphaLowerStr.suchThat(_.nonEmpty)
    forAll(triggerGen) { (triggerText: String) =>
      val now              = Instant.now.getEpochSecond()
      val forwardedMessage = Message(
        messageId = 11,
        date = now,
        chatId = ChatId(1L),
        chatType = "private",
        text = Some(triggerText),
        isForward = true
      )
      val bundle = ReplyBundleMessage.textToText(triggerText)("matched")

      val result = ReplyBundleMessage.selectReplyBundle(
        msg = forwardedMessage,
        messageRepliesData = List(bundle),
        ignoreMessagePrefix = None,
        disableForward = false
      )

      assertEquals(result, Some(bundle))
    }
  }

  property("ReplyBundle.selectReplyBundle should return None for messages older than the threshold") {
    val triggerGen = Gen.alphaLowerStr.suchThat(_.nonEmpty)
    forAll(triggerGen) { (triggerText: String) =>
      val now        = Instant.now.getEpochSecond()
      val oldMessage = Message(
        messageId = 12,
        date = now - FilteringOlder.olderThreshold.toSeconds - 1,
        chatId = ChatId(1L),
        chatType = "private",
        text = Some(triggerText)
      )
      val bundle = ReplyBundleMessage.textToText(triggerText)("matched")

      val result = ReplyBundleMessage.selectReplyBundle(
        msg = oldMessage,
        messageRepliesData = List(bundle),
        ignoreMessagePrefix = None,
        disableForward = false
      )

      assertEquals(result, None)
    }
  }

  property("ReplyBundle.prettyPrint should escape pipe characters in trigger text") {
    val leftGen  = Gen.alphaNumStr.suchThat(_.nonEmpty)
    val rightGen = Gen.alphaNumStr.suchThat(_.nonEmpty)
    forAll(leftGen, rightGen) { (left: String, right: String) =>
      val bundle  = ReplyBundleMessage.textToText(s"$left|$right")("text")
      val printed = bundle.prettyPrint()
      assert(printed.contains(s"$left\\|$right"))
    }
  }

  test("ReplyBundleCommand.from should return command bundle from command key") {
    PropF.forAllF { (commandKey: CommandKey) =>
      for {
        replyBundleCommands <- repo.loadData[ReplyBundleCommand](commandsJsonFilename)
      } yield {
        val checkFunction: ReplyBundleCommand => Boolean =
          replyBundleCommand => replyBundleCommand.trigger.command == commandKey.asString
        val check                                             = replyBundleCommands.find(checkFunction(_))
        val optReplyBundleCommand: Option[ReplyBundleCommand] = ReplyBundleCommand.from(commandKey, replyBundleCommands)
        assert(
          optReplyBundleCommand.fold(check.isEmpty)(checkFunction)
        )
      }
    }
  }
}
