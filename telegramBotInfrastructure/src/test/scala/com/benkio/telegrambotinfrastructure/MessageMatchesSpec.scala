package com.benkio.telegrambotinfrastructure

import cats.effect.IO
import com.benkio.telegrambotinfrastructure.model._
import munit.FunSuite

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

    val result = MessageMatches.doesMatch(replyBundleInput, messageText, ignoreMessagePrefix)

    assert(!result)
  }
  test("doasMatch should return false when the input text is shorter then what specified in MessageLengthTrigger") {
    val replyBundleInputLength = replyBundleInput.copy(
      trigger = MessageLengthTrigger(10)
    )
    val messageText = "shortText"
    val result      = MessageMatches.doesMatch(replyBundleInputLength, messageText, ignoreMessagePrefix)

    assert(!result)
  }
  test("doesMatch should return false when the input text does not contain(ContainsOnce) the trigger") {
    val messageText = "text"
    val result      = MessageMatches.doesMatch(replyBundleInput, messageText, ignoreMessagePrefix)

    assert(!result)
  }
  test("doesMatch should return false when he input text does not contain(ContainsAll) the triggers") {
    val replyBundleInputLength = replyBundleInput.copy(
      trigger = TextTrigger(
        StringTextTriggerValue("test"),
        StringTextTriggerValue("missing")
      ),
      matcher = ContainsAll
    )
    val messageText = "test shortText"
    val result      = MessageMatches.doesMatch(replyBundleInputLength, messageText, ignoreMessagePrefix)

    assert(!result)
  }

  test("doesMatch should return true when the input text is longer then what specified in MessageLengthTrigger") {
    val replyBundleInputLength = replyBundleInput.copy(
      trigger = MessageLengthTrigger(10)
    )
    val messageText = "longerMessage"
    val result      = MessageMatches.doesMatch(replyBundleInputLength, messageText, ignoreMessagePrefix)

    assert(result)
  }
  test("doesMatch should return true when the input text does contain(ContainsOnce) the trigger") {
    val messageText = "test text"
    val result      = MessageMatches.doesMatch(replyBundleInput, messageText, ignoreMessagePrefix)

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
    val result      = MessageMatches.doesMatch(replyBundleInputLength, messageText, ignoreMessagePrefix)

    assert(result)
  }
}
