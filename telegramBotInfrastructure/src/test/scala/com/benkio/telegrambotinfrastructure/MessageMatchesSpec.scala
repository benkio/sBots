package com.benkio.telegrambotinfrastructure

import cats.effect.IO
import com.benkio.telegrambotinfrastructure.model._
import munit.FunSuite
import telegramium.bots.Chat
import telegramium.bots.Message

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
      StringTextTriggerValue("test")
    ),
    mediafiles = inputMediafile
  )

  val ignoreMessagePrefix = Some("!")

  test("doesMatch should return false when the messageText starts with the ignoreMessagePrefix") {
    val messageText = "!messageIgnored"
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some(messageText))

    val result = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)

    assert(!result)
  }
  test("doasMatch should return false when the input text is shorter then what specified in MessageLengthTrigger") {
    val replyBundleInputLength = replyBundleInput.copy(
      trigger = MessageLengthTrigger(10)
    )
    val messageText = "shortText"
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some(messageText))
    val result      = MessageMatches.doesMatch(replyBundleInputLength, testMessage, ignoreMessagePrefix)

    assert(!result)
  }
  test("doesMatch should return false when the input text does not contain(ContainsOnce) the trigger") {
    val messageText = "text"
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some(messageText))
    val result      = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)

    assert(!result)
  }
  test("doesMatch should return false when the input text does not contain(ContainsAll) the triggers") {
    val replyBundleInputLength = replyBundleInput.copy(
      trigger = TextTrigger(
        StringTextTriggerValue("test"),
        StringTextTriggerValue("missing")
      ),
      matcher = ContainsAll
    )
    val messageText = "test shortText"
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some(messageText))
    val result      = MessageMatches.doesMatch(replyBundleInputLength, testMessage, ignoreMessagePrefix)

    assert(!result)
  }
  test("doesMatch should return false when the input message contains empty list of new members in NewMemberTrigger") { ??? }

  test("doesMatch should return true when the input text is longer then what specified in MessageLengthTrigger") {
    val replyBundleInputLength = replyBundleInput.copy(
      trigger = MessageLengthTrigger(10)
    )
    val messageText = "longerMessage"
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some(messageText))
    val result      = MessageMatches.doesMatch(replyBundleInputLength, testMessage, ignoreMessagePrefix)

    assert(result)
  }
  test("doesMatch should return true when the input text does contain(ContainsOnce) the trigger") {
    val messageText = "test text"
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some(messageText))
    val result      = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)

    assert(result)
  }
  test("doesMatch should return true when the input text does not contain(ContainsAll) the triggers") {
    val replyBundleInputLength = replyBundleInput.copy(
      trigger = TextTrigger(
        StringTextTriggerValue("test"),
        StringTextTriggerValue("missing")
      ),
      matcher = ContainsAll
    )
    val messageText = "test shortText is not missing"
    val testMessage = Message(0, date = 0, chat = Chat(0, `type` = "private"), text = Some(messageText))
    val result      = MessageMatches.doesMatch(replyBundleInputLength, testMessage, ignoreMessagePrefix)

    assert(result)
  }
  test("doesMatch should return true when the input message contains non empty list of new members in NewMemberTrigger") { ??? }
}
