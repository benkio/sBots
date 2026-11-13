package com.benkio.chatircadapter.outbound

import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*

class IrcLinesSpec extends ScalaCheckSuite {

  private def split(text: String, maxChars: Int = 400, maxBytes: Int = 400, maxLines: Int = 20) =
    IrcLines.split(text, maxChars, maxBytes, maxLines)

  test("a short text is one line") {
    assertEquals(split(text = "hello"), List("hello"))
  }

  test("the line breaks make lines, the empty ones are dropped") {
    assertEquals(split(text = "one\n\ntwo\r\nthree\rfour\n  \n"), List("one", "two", "three", "four"))
  }

  test("the control characters are removed: no code or command slips in through a text") {
    assertEquals(split(text = "a\u0001b\u0000c\u001bd\u007fe"), List("abcde"))
    assertEquals(split(text = "hi\r\nQUIT :bye"), List("hi", "QUIT :bye"))
  }

  test("a line over the limit of characters is cut on a space") {
    assertEquals(split(text = "aaa bbb ccc", maxChars = 7), List("aaa bbb", "ccc"))
  }

  test("a line over the limit of bytes is cut between characters") {
    val text  = "ä" * 300 // 2 bytes each
    val parts = split(text = text, maxChars = 400, maxBytes = 100)
    assertEquals(parts.map(_.length), List(50, 50, 50, 50, 50, 50))
    assertEquals(parts.mkString, text)
  }

  test("a character of four bytes is not cut in two") {
    val parts = IrcLines.splitBytes("😀" * 5, 8)
    assertEquals(parts, List("😀😀", "😀😀", "😀"))
  }

  test("more lines than the limit are cut, saying how many were left out") {
    val result = split(text = (1 to 30).map(i => s"line$i").mkString("\n"), maxLines = 5)
    assertEquals(result, List("line1", "line2", "line3", "line4", "(26 more lines not shown)"))
  }

  property("every line is within the limits, and no line has a line break") {
    forAll(Gen.stringOf(Gen.oneOf(Gen.alphaNumChar, Gen.const(' '), Gen.const('\n'), Gen.const('ä'), Gen.const('é')))) {
      text =>
        split(text = text, maxChars = 60, maxBytes = 80, maxLines = 1000).forall(line =>
          line.length <= 60 && line.getBytes("UTF-8").length <= 80 && !line.contains('\n') && line.nonEmpty
        )
    }
  }
}
