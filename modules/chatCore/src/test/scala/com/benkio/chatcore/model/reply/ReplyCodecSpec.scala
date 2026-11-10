package com.benkio.chatcore.model.reply

import com.benkio.chatcore.messagefiltering.MessageMatches
import com.benkio.chatcore.model.SBotInfo
import com.benkio.chatcore.model.SBotInfo.SBotId
import com.benkio.chatcore.model.SBotInfo.SBotName
import com.benkio.chatcore.model.StringTextTriggerValue
import com.benkio.chatcore.model.TextTrigger
import io.circe.parser.decode
import io.circe.syntax.*
import io.circe.Json
import munit.FunSuite

/** `PlainReply` is a layer of the Scala hierarchy only: the JSON of a `Reply` is a contract with the data files. */
class ReplyCodecSpec extends FunSuite {

  private val sBotInfo = SBotInfo(SBotId("sbot"), SBotName("SampleBot"))

  private val textReply: TextReply           = TextReply.fromList("hello")(true)
  private val mediaReply: MediaReply         = MediaReply.fromList(List(Mp3File("audio.mp3")))
  private val effectfulReply: EffectfulReply = EffectfulReply(EffectfulKey.Random(sBotInfo))

  private val textJson: Json = Json.obj(
    "TextReply" -> Json.obj("text" -> Json.arr(Json.fromString("hello")), "replyToMessage" -> Json.True)
  )

  test("the JSON of a reply is flat: the kind is the only key, whatever the layer of the hierarchy") {
    assertEquals((textReply: Reply).asJson, textJson)
    assertEquals((textReply: PlainReply).asJson, textJson)
    assertEquals((mediaReply: Reply).asJson.asObject.map(_.keys.toList), Some(List("MediaReply")))
    assertEquals((effectfulReply: Reply).asJson.asObject.map(_.keys.toList), Some(List("EffectfulReply")))
  }

  test("every kind of reply decodes back from its JSON as a Reply") {
    List[Reply](textReply, mediaReply, effectfulReply).foreach(reply =>
      assertEquals(decode[Reply](reply.asJson.noSpaces), Right(reply))
    )
  }

  test("a plain reply decodes back from its JSON as a PlainReply, with or without the static type") {
    List[PlainReply](textReply, mediaReply).foreach { reply =>
      assertEquals(decode[PlainReply](reply.asJson.noSpaces), Right(reply))
      assertEquals(decode[Reply](reply.asJson.noSpaces), Right(reply))
    }
  }

  test("an effectful reply is not a PlainReply") {
    val json = (effectfulReply: Reply).asJson.noSpaces
    assert(decode[PlainReply](json).isLeft, json)
  }

  test("an unknown kind of reply does not decode") {
    assert(decode[Reply]("""{"UnknownReply": {}}""").isLeft)
    assert(decode[PlainReply]("""{"UnknownReply": {}}""").isLeft)
  }

  test("a message bundle round trips and keeps the flat JSON of its reply") {
    val bundle = ReplyBundleMessage(
      trigger = TextTrigger(StringTextTriggerValue("hello")),
      reply = textReply,
      matcher = MessageMatches.ContainsOnce
    )
    val json = bundle.asJson
    assertEquals(json.hcursor.downField("reply").focus, Some(textJson))
    assertEquals(json.as[ReplyBundleMessage], Right(bundle))
  }

  test("a message bundle with an effectful reply is rejected") {
    val json = ReplyBundleMessage(
      trigger = TextTrigger(StringTextTriggerValue("hello")),
      reply = textReply,
      matcher = MessageMatches.ContainsOnce
    ).asJson.mapObject(_.add("reply", (effectfulReply: Reply).asJson))
    assert(json.as[ReplyBundleMessage].isLeft, json.noSpaces)
  }

  test("a command bundle keeps accepting every kind of reply") {
    List[Reply](textReply, mediaReply, effectfulReply).foreach { reply =>
      val bundle = ReplyBundleCommand(
        trigger = com.benkio.chatcore.model.CommandTrigger("cmd"),
        reply = reply,
        instruction = com.benkio.chatcore.model.CommandInstructionData.NoInstructions
      )
      assertEquals(bundle.asJson.as[ReplyBundleCommand], Right(bundle))
    }
  }
}
