package com.benkio.telegramBotInfrastructure

import com.benkio.telegramBotInfrastructure.ContainsAll
import com.benkio.telegramBotInfrastructure.model._
import org.scalatest._

class MessageMatchesSpec extends WordSpec with Matchers {

  val inputMediafile: List[MediaFile] = List(
    Mp3File("audio.mp3"),
    PhotoFile("picture.jpg"),
    PhotoFile("picture.png"),
    GifFile("a.gif")
  )

  val replyBundleInput: ReplyBundleMessage = ReplyBundleMessage(
    trigger = TextTrigger(List(StringTextTriggerValue("test"))),
    mediafiles = inputMediafile
  )

  val ignoreMessagePrefix = Some("!")

  "doesMatch" should {
    "return false" when {
      "the messageText starts with the ignoreMessagePrefix" in {
        val messageText = "!messageIgnored"

        val result = MessageMatches.doesMatch(replyBundleInput, messageText, ignoreMessagePrefix)

        result shouldEqual false
      }
      "the input text is shorter then what specified in MessageLengthTrigger" in {
        val replyBundleInputLength = replyBundleInput.copy(
          trigger = MessageLengthTrigger(10)
        )
        val messageText = "shortText"
        val result      = MessageMatches.doesMatch(replyBundleInputLength, messageText, ignoreMessagePrefix)

        result shouldEqual false
      }
      "the input text does not contain(ContainsOnce) the trigger" in {
        val messageText = "text"
        val result      = MessageMatches.doesMatch(replyBundleInput, messageText, ignoreMessagePrefix)

        result shouldEqual false
      }
      "the input text does not contain(ContainsAll) the triggers" in {
        val replyBundleInputLength = replyBundleInput.copy(
          trigger = TextTrigger(List(StringTextTriggerValue("test"), StringTextTriggerValue("missing"))),
          matcher = ContainsAll
        )
        val messageText = "test shortText"
        val result      = MessageMatches.doesMatch(replyBundleInputLength, messageText, ignoreMessagePrefix)

        result shouldEqual false
      }
    }
    "return true" when {
      "the input text is longer then what specified in MessageLengthTrigger" in {
        val replyBundleInputLength = replyBundleInput.copy(
          trigger = MessageLengthTrigger(10)
        )
        val messageText = "longerMessage"
        val result      = MessageMatches.doesMatch(replyBundleInputLength, messageText, ignoreMessagePrefix)

        result shouldEqual true
      }
      "the input text does contain(ContainsOnce) the trigger" in {
        val messageText = "test text"
        val result      = MessageMatches.doesMatch(replyBundleInput, messageText, ignoreMessagePrefix)

        result shouldEqual true
      }
      "the input text does not contain(ContainsAll) the triggers" in {
        val replyBundleInputLength = replyBundleInput.copy(
          trigger = TextTrigger(List(StringTextTriggerValue("test"), StringTextTriggerValue("missing"))),
          matcher = ContainsAll
        )
        val messageText = "test shortText is not missing"
        val result      = MessageMatches.doesMatch(replyBundleInputLength, messageText, ignoreMessagePrefix)

        result shouldEqual true
      }
    }
  }
}
