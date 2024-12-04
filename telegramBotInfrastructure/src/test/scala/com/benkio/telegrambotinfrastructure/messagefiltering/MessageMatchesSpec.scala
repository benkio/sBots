package com.benkio.telegrambotinfrastructure.messagefiltering

import io.circe.parser.decode
import io.circe.syntax.*
import cats.effect.IO

import com.benkio.telegrambotinfrastructure.model.*
import munit.FunSuite
import telegramium.bots.Chat
import telegramium.bots.Message
import telegramium.bots.User

class MessageMatchesSpec extends FunSuite {

  val inputMediafile: List[MediaFile] = List(
    Mp3File("audio.mp3"),
    PhotoFile("picture.jpg"),
    PhotoFile("picture.png"),
    VideoFile("video.mp4"),
    GifFile("a.gif")
  )

  val replyBundleInput: ReplyBundleMessage[IO] = ReplyBundleMessage[IO](
    trigger = TextTrigger(
      StringTextTriggerValue("test"),
      StringTextTriggerValue("some other long trigger"),
      RegexTextTriggerValue("test regex with (optional|maybe)? values".r, 28),
    ),
    reply = MediaReply[IO](mediaFiles = IO.pure(inputMediafile))
  )

  val ignoreMessagePrefix: Some[String] = Some("!")

  test("doesMatch should return None when the messageText starts with the ignoreMessagePrefix") {
    val messageText = "!messageIgnored"
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some(messageText))

    val result = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)

    assert(result.isEmpty)
  }
  test("doasMatch should return None when the input text is shorter then what specified in MessageLengthTrigger") {
    val replyBundleInputLength = replyBundleInput.copy(
      trigger = MessageLengthTrigger(10)
    )
    val messageText = "shortText"
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some(messageText))
    val result      = MessageMatches.doesMatch(replyBundleInputLength, testMessage, ignoreMessagePrefix)

    assert(result.isEmpty)
  }
  test("doesMatch should return None when the input text does not contain(ContainsOnce) the trigger") {
    val messageText = "text"
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some(messageText))
    val result      = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)

    assert(result.isEmpty)
  }
  test("doesMatch should return None when the input text does not contain(MessageMatches.ContainsAll) the triggers") {
    val replyBundleInputLength = replyBundleInput.copy(
      trigger = TextTrigger(
        StringTextTriggerValue("test"),
        StringTextTriggerValue("missing")
      ),
      matcher = MessageMatches.ContainsAll
    )
    val messageText = "test shortText"
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some(messageText))
    val result      = MessageMatches.doesMatch(replyBundleInputLength, testMessage, ignoreMessagePrefix)

    assert(result.isEmpty)
  }
  test("doesMatch should return None when the input message contains empty list of new members in NewMemberTrigger") {
    val replyBundleInputNewMembers = replyBundleInput.copy(
      trigger = NewMemberTrigger
    )
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = None, newChatMembers = List.empty)

    val result = MessageMatches.doesMatch(replyBundleInputNewMembers, testMessage, ignoreMessagePrefix)
    assert(result.isEmpty)
  }
  test("doesMatch should return None when the input message contains No left member in LeftMemberTrigger") {
    val replyBundleInputLeaveMembers = replyBundleInput.copy(
      trigger = LeftMemberTrigger
    )
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = None, leftChatMember = None)

    val result = MessageMatches.doesMatch(replyBundleInputLeaveMembers, testMessage, ignoreMessagePrefix)
    assert(result.isEmpty)
  }

  test(
    "doesMatch should return Some(trigger, replyMessageBundle) when the input text is longer then what specified in MessageLengthTrigger"
  ) {
    val replyBundleInputLength = replyBundleInput.copy(
      trigger = MessageLengthTrigger(10)
    )
    val messageText = "longerMessage"
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some(messageText))
    val result      = MessageMatches.doesMatch(replyBundleInputLength, testMessage, ignoreMessagePrefix)
    val expected: Option[(Trigger, ReplyBundleMessage[IO])] = Some(MessageLengthTrigger(10), replyBundleInputLength)

    assertEquals(result, expected)
  }
  test(
    "doesMatch should return Some(trigger, replyMessageBundle) when the input text does contain(ContainsOnce) the trigger"
  ) {
    val messageText = "test text"
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some(messageText))
    val result      = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)
    val expected: Option[(Trigger, ReplyBundleMessage[IO])] =
      Some(TextTrigger(StringTextTriggerValue("test")), replyBundleInput)

    assertEquals(result, expected)
  }
  test(
    "doesMatch should return Some(longestTrigger, replyMessageBundle) when the input text does contain(ContainsOnce) the trigger"
  ) {
    val messageText = "message matching twice, the short trigger and some other long trigger in test text"
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some(messageText))
    val result      = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)
    val expected: Option[(Trigger, ReplyBundleMessage[IO])] =
      Some(TextTrigger(StringTextTriggerValue("some other long trigger")), replyBundleInput)

    assertEquals(result, expected)
  }
  test(
    "doesMatch should return Some(longestTrigger, replyMessageBundle) when the input text does contain(MessageMatches.ContainsAll) the triggers"
  ) {
    val replyBundleInputLength = replyBundleInput.copy(
      trigger = TextTrigger(
        StringTextTriggerValue("test"),
        StringTextTriggerValue("missing")
      ),
      matcher = MessageMatches.ContainsAll
    )
    val messageText = "test shortText is not missing"
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some(messageText))
    val result      = MessageMatches.doesMatch(replyBundleInputLength, testMessage, ignoreMessagePrefix)
    val expected: Option[(Trigger, ReplyBundleMessage[IO])] =
      Some(
        TextTrigger(
          StringTextTriggerValue("missing"),
          StringTextTriggerValue("test")
        ),
        replyBundleInputLength
      )

    assertEquals(result, expected)
  }
  test(
    "doesMatch should return Some(NewMemberTrigger, replyMessageBundle) when the input message contains non empty list of new members in NewMemberTrigger"
  ) {
    val replyBundleInputNewMembers = replyBundleInput.copy(
      trigger = NewMemberTrigger
    )

    val testMessage = Message(
      messageId = 67715,
      from = Some(User(23769493, false, "Benkio", None, Some("Benkio"), Some("en"), None, None, None)),
      date = 1653503377,
      chat = Chat(
        -444726279,
        "group",
        Some("Via delle Albizzie 22"),
      ),
      newChatMembers = List(User(87680068, false, "Silvio", None, None, Some("it"), None, None, None)),
    )
    val expected: Option[(Trigger, ReplyBundleMessage[IO])] = Some(NewMemberTrigger, replyBundleInputNewMembers)

    val result = MessageMatches.doesMatch(replyBundleInputNewMembers, testMessage, ignoreMessagePrefix)
    assertEquals(result, expected)
  }
  test(
    "doesMatch should return Some(LeftMemberTrigger, replyMessageBundle) when the input message contains non empty list of left members in LeftMemberTrigger"
  ) {
    val replyBundleInputLeaveMembers = replyBundleInput.copy(
      trigger = LeftMemberTrigger
    )

    val testMessage = Message(
      messageId = 67715,
      from = Some(User(23769493, false, "Benkio", None, Some("Benkio"), Some("en"), None, None, None)),
      date = 1653503377,
      chat = Chat(
        -444726279,
        "group",
        Some("Via delle Albizzie 22"),
      ),
      leftChatMember = Some(User(87680068, false, "Silvio", None, None, Some("it"), None, None, None)),
    )
    val expected: Option[(Trigger, ReplyBundleMessage[IO])] = Some(LeftMemberTrigger, replyBundleInputLeaveMembers)

    val result = MessageMatches.doesMatch(replyBundleInputLeaveMembers, testMessage, ignoreMessagePrefix)
    assertEquals(result, expected)
  }

  test("MessageMatches JSON Decoder/Encoder should works as expected") {
    val jsonInputs = List(
      """"ContainsOnce"""",
      """"ContainsAll"""",
    )

    for inputString <- jsonInputs
    yield {
      val eitherMessageTrigger = decode[MessageMatches](inputString)
      eitherMessageTrigger.fold(
        e => fail("failed in parsing the input string as message matches", e),
        ms => assertEquals(ms.asJson.toString, inputString)
      )
    }
  }
}
