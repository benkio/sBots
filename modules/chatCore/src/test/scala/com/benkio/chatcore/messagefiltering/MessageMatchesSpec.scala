package com.benkio.chatcore.messagefiltering

import com.benkio.chatcore.model.reply.MediaReply
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.ReplyBundleMessage
import com.benkio.chatcore.model.ChatId
import com.benkio.chatcore.model.LeftMemberTrigger
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.MessageLengthTrigger
import com.benkio.chatcore.model.NewMemberTrigger
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTrigger
import com.benkio.chatcore.Arbitraries.given
import io.circe.parser.decode
import io.circe.syntax.*
import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop.*

class MessageMatchesSpec extends ScalaCheckSuite {

  private val ignorePrefix = "!"

  private def bundle(
      trigger: com.benkio.chatcore.model.MessageTrigger,
      matcher: MessageMatches = MessageMatches.ContainsOnce
  ): ReplyBundleMessage =
    ReplyBundleMessage(
      trigger = trigger,
      reply = MediaReply(Set(Mp3File("audio.mp3"))),
      matcher = matcher
    )

  property("doesMatch returns None when text or caption starts with ignore prefix") {
    forAll(arbitrary[Message], Gen.alphaStr) { (base: Message, body: String) =>
      val ignoredText = base.copy(text = Some(ignorePrefix + body), caption = None)
      val ignoredCap  = base.copy(text = None, caption = Some(ignorePrefix + body))
      val b           = bundle(TextTrigger(StringTextTriggerValue("x")))
      assert(MessageMatches.doesMatch(b, ignoredText, Some(ignorePrefix)).isEmpty)
      assert(MessageMatches.doesMatch(b, ignoredCap, Some(ignorePrefix)).isEmpty)
    }
  }

  property("MessageLengthTrigger matches iff content length >= threshold") {
    forAll(Gen.choose(0, 50), Gen.alphaStr) { (threshold: Int, content: String) =>
      val b   = bundle(MessageLengthTrigger(threshold))
      val msg = Message(0, 0L, ChatId(0L), "private", text = Some(content))
      val got = MessageMatches.doesMatch(b, msg, None)
      if content.size >= threshold then assert(got.isDefined)
      else assert(got.isEmpty)
    }
  }

  property("NewMemberTrigger matches iff newChatMembers is nonEmpty") {
    forAll { (msg: Message) =>
      val b   = bundle(NewMemberTrigger)
      val got = MessageMatches.doesMatch(b, msg, None)
      assertEquals(got.isDefined, msg.newChatMembers.nonEmpty)
    }
  }

  property("LeftMemberTrigger matches iff leftChatMember is defined") {
    forAll { (msg: Message) =>
      val b   = bundle(LeftMemberTrigger)
      val got = MessageMatches.doesMatch(b, msg, None)
      assertEquals(got.isDefined, msg.leftChatMember.nonEmpty)
    }
  }

  property("ContainsOnce matches when at least one string trigger is contained") {
    forAll { (trigger: StringTextTriggerValue) =>
      val b = bundle(TextTrigger(trigger), MessageMatches.ContainsOnce)
      val matching =
        Message(0, 0L, ChatId(0L), "private", text = Some(s"xx${trigger.trigger}yy"))
      val missing = Message(0, 0L, ChatId(0L), "private", text = Some("000000000000"))
      assert(MessageMatches.doesMatch(b, matching, None).isDefined)
      assert(MessageMatches.doesMatch(b, missing, None).isEmpty)
    }
  }

  property("ContainsAll matches iff every string trigger is contained") {
    forAll(
      Gen
        .choose(1, 4)
        .flatMap(n =>
          Gen
            .listOfN(n, Gen.listOfN(5, Gen.alphaLowerChar).map(_.mkString))
            .map(_.distinct)
            .suchThat(_.size == n)
        )
    ) { (needles: List[String]) =>
      val triggers = needles.map(StringTextTriggerValue.apply)
      val b        = bundle(TextTrigger(triggers*), MessageMatches.ContainsAll)
      val matching =
        Message(0, 0L, ChatId(0L), "private", text = Some(needles.mkString(" ")))
      assert(MessageMatches.doesMatch(b, matching, None).isDefined)
      if needles.size > 1 then {
        val missingOne =
          Message(0, 0L, ChatId(0L), "private", text = Some(needles.dropRight(1).mkString(" ")))
        assert(MessageMatches.doesMatch(b, missingOne, None).isEmpty)
      }
    }
  }

  // Golden case: longest ContainsOnce trigger wins
  test("ContainsOnce prefers the longest matching string trigger") {
    val b = bundle(
      TextTrigger(
        StringTextTriggerValue("test"),
        StringTextTriggerValue("some other long trigger")
      )
    )
    val msg = Message(
      0,
      0L,
      ChatId(0L),
      "private",
      text = Some("message matching twice, the short trigger and some other long trigger in test text")
    )
    val result = MessageMatches.doesMatch(b, msg, Some(ignorePrefix))
    assertEquals(result.map(_._1), Some(TextTrigger(StringTextTriggerValue("some other long trigger"))))
  }

  test("MessageMatches JSON decode/encode should work as expected") {
    val jsonInputs = List(""""ContainsOnce"""", """"ContainsAll"""")
    for inputString <- jsonInputs yield {
      val eitherMessageMatches = decode[MessageMatches](inputString)
      eitherMessageMatches.fold(
        e => fail("failed in parsing the input string as MessageMatches", e),
        mm => assertEquals(mm.asJson.toString, inputString)
      )
    }
  }
}
