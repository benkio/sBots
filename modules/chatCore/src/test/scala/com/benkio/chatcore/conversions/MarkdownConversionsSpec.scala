package com.benkio.chatcore.conversions

import com.benkio.chatcore.model.reply.Markdown
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*

class MarkdownConversionsSpec extends ScalaCheckSuite {

  test("toPlainText should remove the emphasis markers") {
    assertEquals(
      MarkdownConversions.toPlainText("**bold**, __bold__, *italic*, _italic_ and ~~strike~~"),
      "bold, bold, italic, italic and strike"
    )
  }

  test("toPlainText should write the links as `text (url)`") {
    assertEquals(
      MarkdownConversions.toPlainText("see [the title](https://example.com/a_b_c) now"),
      "see the title (https://example.com/a_b_c) now"
    )
  }

  test("toPlainText should write a link whose text is the url once") {
    assertEquals(MarkdownConversions.toPlainText("[https://x.y/a_b_](https://x.y/a_b_)"), "https://x.y/a_b_")
  }

  test("toPlainText should remove headings, quotes, code and fences") {
    assertEquals(
      MarkdownConversions.toPlainText("# Title\n> quoted\nsome `code` here\n```scala\nval a = 1\n```\nend"),
      "Title\nquoted\nsome code here\nval a = 1\nend"
    )
  }

  test("toPlainText should keep the underscores inside the words") {
    assertEquals(MarkdownConversions.toPlainText("snake_case_name"), "snake_case_name")
  }

  test("toPlainText should show the escaped characters literally") {
    assertEquals(MarkdownConversions.toPlainText("\\*not bold\\* 1\\. a\\_b"), "*not bold* 1. a_b")
  }

  private val textGen: Gen[String] =
    Gen.listOf(Gen.oneOf(Gen.alphaNumChar, Gen.oneOf(" _*[]()~`>#+-=|{}.!\\".toList))).map(_.mkString)

  property("toPlainText should give back a text escaped with Markdown.escape") {
    forAll(textGen) { text =>
      assertEquals(MarkdownConversions.toPlainText(Markdown.escape(text)), text)
    }
  }
}
