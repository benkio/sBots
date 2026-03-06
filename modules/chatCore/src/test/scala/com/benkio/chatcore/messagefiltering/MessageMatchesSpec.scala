package com.benkio.chatcore.messagefiltering

import com.benkio.chatcore.messagefiltering.MessageMatches
import com.benkio.chatcore.model.reply.GifFile
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.MediaReply
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PhotoFile
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.reply.VideoFile
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.LeftMemberTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageLengthTrigger
import com.benkio.chatcore.model.NewMemberTrigger
import com.benkio.chatcore.model.RegexTextTriggerValue
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTrigger
import com.benkio.chatcore.model.Trigger
import io.circe.parser.decode
import io.circe.syntax.*
import munit.FunSuite

class MessageMatchesSpec extends FunSuite {

  val inputMediafile: List[MediaFile] = List(
    Mp3File("audio.mp3"),
    PhotoFile("picture.jpg"),
    PhotoFile("picture.png"),
    VideoFile("video.mp4"),
    GifFile("aGif.mp4")
  )

  val replyBundleInput: ReplyBundleMessage = ReplyBundleMessage(
    trigger = TextTrigger(
      StringTextTriggerValue("test"),
      StringTextTriggerValue("some other long trigger"),
      RegexTextTriggerValue("test regex with (optional|maybe)? values".r, 23)
    ),
    reply = MediaReply(mediaFiles = inputMediafile),
    matcher = MessageMatches.ContainsOnce
  )

  val ignoreMessagePrefix: Some[String] = Some("!")

  test("doesMatch should return None when the message text starts with the ignoreMessagePrefix") {
    val matchingMessageText = "!messageIgnored test"
    val testMessage         =
      Message(0, date = 0, chatId = ChatId(0), chatType = "private", text = Some(matchingMessageText))

    val result = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)

    assert(result.isEmpty)
  }
  test("doesMatch should return None when the message caption starts with the ignoreMessagePrefix") {
    val matchingMessageText = "!messageIgnored test"
    val testMessage         =
      Message(0, date = 0, chatId = ChatId(0), chatType = "private", caption = Some(matchingMessageText))

    val result = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)

    assert(result.isEmpty)
  }
  test("doasMatch should return None when the input text is shorter then what specified in MessageLengthTrigger") {
    val replyBundleInputLength = replyBundleInput.copy(
      trigger = MessageLengthTrigger(10)
    )
    val matchingMessageText = "shortText"
    val testMessage         =
      Message(0, date = 0, chatId = ChatId(0), chatType = "private", text = Some(matchingMessageText))
    val result = MessageMatches.doesMatch(replyBundleInputLength, testMessage, ignoreMessagePrefix)

    assert(result.isEmpty)
  }
  test("doesMatch should return None when the input text does not contain(ContainsOnce) the trigger") {
    val matchingMessageText = "text"
    val testMessage         =
      Message(0, date = 0, chatId = ChatId(0), chatType = "private", text = Some(matchingMessageText))
    val result = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)

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
    val matchingMessageText = "test shortText"
    val testMessage         =
      Message(0, date = 0, chatId = ChatId(0), chatType = "private", text = Some(matchingMessageText))
    val result = MessageMatches.doesMatch(replyBundleInputLength, testMessage, ignoreMessagePrefix)

    assert(result.isEmpty)
  }
  test("doesMatch should return None when the input caption does not contain(ContainsOnce) the trigger") {
    val matchingMessageText = "text"
    val testMessage         =
      Message(0, date = 0, chatId = ChatId(0), chatType = "private", caption = Some(matchingMessageText))
    val result = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)

    assert(result.isEmpty)
  }
  test(
    "doesMatch should return None when the input caption does not contain(MessageMatches.ContainsAll) the triggers"
  ) {
    val replyBundleInputLength = replyBundleInput.copy(
      trigger = TextTrigger(
        StringTextTriggerValue("test"),
        StringTextTriggerValue("missing")
      ),
      matcher = MessageMatches.ContainsAll
    )
    val matchingMessageText = "test shortText"
    val testMessage         =
      Message(0, date = 0, chatId = ChatId(0), chatType = "private", caption = Some(matchingMessageText))
    val result = MessageMatches.doesMatch(replyBundleInputLength, testMessage, ignoreMessagePrefix)

    assert(result.isEmpty)
  }
  test("doesMatch should return None when the input message contains empty list of new members in NewMemberTrigger") {
    val replyBundleInputNewMembers = replyBundleInput.copy(
      trigger = NewMemberTrigger
    )
    val testMessage =
      Message(0, date = 0, chatId = ChatId(0), chatType = "private", text = None, newChatMembers = List.empty)

    val result = MessageMatches.doesMatch(replyBundleInputNewMembers, testMessage, ignoreMessagePrefix)
    assert(result.isEmpty)
  }
  test("doesMatch should return None when the input message contains No left member in LeftMemberTrigger") {
    val replyBundleInputLeaveMembers = replyBundleInput.copy(
      trigger = LeftMemberTrigger
    )
    val testMessage =
      Message(0, date = 0, chatId = ChatId(0), chatType = "private", text = None, leftChatMember = None)

    val result = MessageMatches.doesMatch(replyBundleInputLeaveMembers, testMessage, ignoreMessagePrefix)
    assert(result.isEmpty)
  }

  test(
    "doesMatch should return Some(trigger, replyMessageBundle) when the input text is longer then what specified in MessageLengthTrigger"
  ) {
    val replyBundleInputLength = replyBundleInput.copy(
      trigger = MessageLengthTrigger(10)
    )
    val matchingMessageText = "longerMessage"
    val testMessage         =
      Message(0, date = 0, chatId = ChatId(0), chatType = "private", text = Some(matchingMessageText))
    val result = MessageMatches.doesMatch(replyBundleInputLength, testMessage, ignoreMessagePrefix)
    val expected: Option[(Trigger, ReplyBundleMessage)] = Some(MessageLengthTrigger(10), replyBundleInputLength)

    assertEquals(result, expected)
  }
  test(
    "doesMatch should return Some(trigger, replyMessageBundle) when the input text does contain(ContainsOnce) the trigger"
  ) {
    val matchingMessageText = "test text"
    val testMessage         =
      Message(0, date = 0, chatId = ChatId(0), chatType = "private", text = Some(matchingMessageText))
    val result = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)
    val expected: Option[(Trigger, ReplyBundleMessage)] =
      Some(TextTrigger(StringTextTriggerValue("test")), replyBundleInput)

    assertEquals(result, expected)
  }
  test(
    "doesMatch should return Some(longestTrigger, replyMessageBundle) when the input text does contain(ContainsOnce) the trigger"
  ) {
    val matchingMessageText = "message matching twice, the short trigger and some other long trigger in test text"
    val testMessage         =
      Message(0, date = 0, chatId = ChatId(0), chatType = "private", text = Some(matchingMessageText))
    val result = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)
    val expected: Option[(Trigger, ReplyBundleMessage)] =
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
    val matchingMessageText = "test shortText is not missing"
    val testMessage         =
      Message(0, date = 0, chatId = ChatId(0), chatType = "private", text = Some(matchingMessageText))
    val result = MessageMatches.doesMatch(replyBundleInputLength, testMessage, ignoreMessagePrefix)
    val expected: Option[(Trigger, ReplyBundleMessage)] =
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
    "doesMatch should return Some(trigger, replyMessageBundle) when the input caption does contain(ContainsOnce) the trigger"
  ) {
    val matchingMessageText = "test text"
    val testMessage         =
      Message(0, date = 0, chatId = ChatId(0), chatType = "private", caption = Some(matchingMessageText))
    val result = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)
    val expected: Option[(Trigger, ReplyBundleMessage)] =
      Some(TextTrigger(StringTextTriggerValue("test")), replyBundleInput)

    assertEquals(result, expected)
  }
  test(
    "doesMatch should return Some(longestTrigger, replyMessageBundle) when the input caption does contain(ContainsOnce) the trigger"
  ) {
    val matchingMessageText = "message matching twice, the short trigger and some other long trigger in test text"
    val testMessage         =
      Message(0, date = 0, chatId = ChatId(0), chatType = "private", caption = Some(matchingMessageText))
    val result = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)
    val expected: Option[(Trigger, ReplyBundleMessage)] =
      Some(TextTrigger(StringTextTriggerValue("some other long trigger")), replyBundleInput)

    assertEquals(result, expected)
  }
  test(
    "doesMatch should return Some(longestTrigger, replyMessageBundle) when the input caption does contain(MessageMatches.ContainsAll) the triggers"
  ) {
    val replyBundleInputLength = replyBundleInput.copy(
      trigger = TextTrigger(
        StringTextTriggerValue("test"),
        StringTextTriggerValue("missing")
      ),
      matcher = MessageMatches.ContainsAll
    )
    val matchingMessageText = "test shortText is not missing"
    val testMessage         =
      Message(0, date = 0, chatId = ChatId(0), chatType = "private", caption = Some(matchingMessageText))
    val result = MessageMatches.doesMatch(replyBundleInputLength, testMessage, ignoreMessagePrefix)
    val expected: Option[(Trigger, ReplyBundleMessage)] =
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
      date = 1653503377,
      chatId = ChatId(-444726279),
      chatType = "group",
      newChatMembers = List(com.benkio.chatcore.model.User(id = 87680068, isBot = false, firstName = "Silvio"))
    )
    val expected: Option[(Trigger, ReplyBundleMessage)] = Some(NewMemberTrigger, replyBundleInputNewMembers)

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
      date = 1653503377,
      chatId = ChatId(-444726279),
      chatType = "group",
      leftChatMember = Some(com.benkio.chatcore.model.User(id = 87680068, isBot = false, firstName = "Silvio"))
    )
    val expected: Option[(Trigger, ReplyBundleMessage)] = Some(LeftMemberTrigger, replyBundleInputLeaveMembers)

    val result = MessageMatches.doesMatch(replyBundleInputLeaveMembers, testMessage, ignoreMessagePrefix)
    assertEquals(result, expected)
  }

  test(
    "doesMatch should return Some(trigger, replyMessageBundle) when the input text does contain(ContainsOnce) the trigger and caption doensn't match"
  ) {
    val matchingMessageText    = "test text"
    val nonMatchingMessageText = "text"
    val testMessage            = Message(
      0,
      date = 0,
      chatId = ChatId(0),
      chatType = "private",
      text = Some(matchingMessageText),
      caption = Some(nonMatchingMessageText)
    )
    val result = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)
    val expected: Option[(Trigger, ReplyBundleMessage)] =
      Some(TextTrigger(StringTextTriggerValue("test")), replyBundleInput)

    assertEquals(result, expected)
  }

  test(
    "doesMatch should return Some(trigger, replyMessageBundle) when the input text does contain(ContainsOnce) the trigger and caption as well"
  ) {
    val matchingMessageText = "test text"
    val testMessage         = Message(
      0,
      date = 0,
      chatId = ChatId(0),
      chatType = "private",
      text = Some(matchingMessageText),
      caption = Some(matchingMessageText)
    )
    val result = MessageMatches.doesMatch(replyBundleInput, testMessage, ignoreMessagePrefix)
    val expected: Option[(Trigger, ReplyBundleMessage)] =
      Some(TextTrigger(StringTextTriggerValue("test")), replyBundleInput)

    assertEquals(result, expected)
  }
  test("MessageMatches JSON Decoder/Encoder should works as expected") {
    val jsonInputs = List(
      """"ContainsOnce"""",
      """"ContainsAll""""
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
