package com.benkio.replieseditor.load

import com.benkio.replieseditor.module.*
import io.circe.parser.parse
import munit.FunSuite

class RepliesJsonMappingSuite extends FunSuite {

  test("extractEditableEntry parses MediaReply into file reply items") {
    val jsonStr =
      """
        |{
        |  "trigger": { "TextTrigger": { "triggers": [ { "StringTextTriggerValue": "hello" } ] } },
        |  "reply": {
        |    "MediaReply": {
        |      "mediaFiles": [
        |        { "Mp3File": { "filepath": "bot_x.mp3", "replyToMessage": false } }
        |      ],
        |      "replyToMessage": false
        |    }
        |  },
        |  "matcher": "ContainsOnce"
        |}
        |""".stripMargin

    val json = parse(jsonStr).toOption.get
    val e    = RepliesJsonMapping.extractEditableEntry(json).get
    assertEquals(e.matcher, "ContainsOnce")
    assertEquals(e.triggers.map(_.value), Vector("hello"))
    assertEquals(e.replies, Vector(ReplyItem(ReplyItemKind.File, "bot_x.mp3")))
  }

  test("extractEditableEntry parses MarkdownFile media entries") {
    val jsonStr =
      """
        |{
        |  "trigger": { "TextTrigger": { "triggers": [ { "StringTextTriggerValue": "md" } ] } },
        |  "reply": {
        |    "MediaReply": {
        |      "mediaFiles": [
        |        { "MarkdownFile": { "filepath": "bot_x.md", "replyToMessage": false } }
        |      ],
        |      "replyToMessage": false
        |    }
        |  },
        |  "matcher": "ContainsOnce"
        |}
        |""".stripMargin

    val json = parse(jsonStr).toOption.get
    val e    = RepliesJsonMapping.extractEditableEntry(json).get
    assertEquals(e.replies, Vector(ReplyItem(ReplyItemKind.File, "bot_x.md")))
  }

  test("extractEditableEntry parses TextReply into text reply items") {
    val jsonStr =
      """
        |{
        |  "trigger": { "TextTrigger": { "triggers": [ { "StringTextTriggerValue": "ciao" } ] } },
        |  "reply": { "TextReply": { "text": [ "one", "two" ], "replyToMessage": false } },
        |  "matcher": "ContainsOnce"
        |}
        |""".stripMargin

    val json = parse(jsonStr).toOption.get
    val e    = RepliesJsonMapping.extractEditableEntry(json).get
    assertEquals(e.replies, Vector(ReplyItem(ReplyItemKind.Text, "one"), ReplyItem(ReplyItemKind.Text, "two")))
  }

  test("buildJsonFromEditable produces MediaReply when replies are all files") {
    val e =
      EditableEntry(
        replies = Vector(ReplyItem(ReplyItemKind.File, "bot_a.mp3")),
        triggers = Vector(TriggerEdit(TriggerKind.PlainString, "t", None)),
        matcher = "ContainsOnce"
      )

    val out = RepliesJsonMapping.buildJsonFromEditable(e).toOption.get
    val c   = out.hcursor
    assert(c.downField("reply").downField("MediaReply").succeeded)
    assertEquals(
      c.downField("reply")
        .downField("MediaReply")
        .downField("mediaFiles")
        .downArray
        .downField("Mp3File")
        .downField("filepath")
        .as[String]
        .toOption,
      Some("bot_a.mp3")
    )
  }

  test("buildJsonFromEditable maps .md files to MarkdownFile wrapper") {
    val e =
      EditableEntry(
        replies = Vector(ReplyItem(ReplyItemKind.File, "bot_a.md")),
        triggers = Vector(TriggerEdit(TriggerKind.PlainString, "t", None)),
        matcher = "ContainsOnce"
      )

    val out = RepliesJsonMapping.buildJsonFromEditable(e).toOption.get
    val c   = out.hcursor
    assertEquals(
      c.downField("reply")
        .downField("MediaReply")
        .downField("mediaFiles")
        .downArray
        .downField("MarkdownFile")
        .downField("filepath")
        .as[String]
        .toOption,
      Some("bot_a.md")
    )
  }

  test("buildJsonFromEditable produces TextReply when replies are all texts") {
    val e =
      EditableEntry(
        replies = Vector(ReplyItem(ReplyItemKind.Text, "a"), ReplyItem(ReplyItemKind.Text, "b")),
        triggers = Vector(TriggerEdit(TriggerKind.PlainString, "t", None)),
        matcher = "ContainsOnce"
      )

    val out = RepliesJsonMapping.buildJsonFromEditable(e).toOption.get
    val c   = out.hcursor
    assert(c.downField("reply").downField("TextReply").succeeded)
    assertEquals(
      c.downField("reply").downField("TextReply").downField("text").as[Vector[String]].toOption,
      Some(Vector("a", "b"))
    )
  }

  test("buildJsonFromEditable rejects mixing file and text replies") {
    val e =
      EditableEntry(
        replies = Vector(ReplyItem(ReplyItemKind.File, "x.mp3"), ReplyItem(ReplyItemKind.Text, "oops")),
        triggers = Vector(TriggerEdit(TriggerKind.PlainString, "t", None)),
        matcher = "ContainsOnce"
      )

    val out = RepliesJsonMapping.buildJsonFromEditable(e)
    assert(out.isLeft)
  }

  test("buildJsonFromEditable rejects regex triggers without regexLength") {
    val e =
      EditableEntry(
        replies = Vector(ReplyItem(ReplyItemKind.Text, "a")),
        triggers = Vector(TriggerEdit(TriggerKind.Regex, "x+", None)),
        matcher = "ContainsOnce"
      )

    val out = RepliesJsonMapping.buildJsonFromEditable(e)
    assert(out.isLeft)
  }
}
