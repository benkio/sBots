package com.benkio.chatircadapter.outbound

import com.benkio.chatcore.model.reply.ReplyTransformer

/** A text as the lines IRC can carry: a message is one line, and a line is at most 512 bytes with the prefix the server
  * adds and the line end.
  */
object IrcLines {

  /** Cuts the text in lines without applying a maximum line count. The line breaks start a new line (the empty ones are
    * dropped), the other control characters are removed, a line over the limit of characters is cut on a space when it
    * can, and then over the limit of bytes.
    */
  def toLines(text: String, maxChars: Int, maxBytes: Int): List[String] =
    text
      .replace("\r\n", "\n")
      .replace('\r', '\n')
      .filterNot(c => (c < ' ' && c != '\n') || c == '\u007f')
      .split('\n')
      .toList
      .map(_.stripTrailing)
      .filter(_.nonEmpty)
      .flatMap(line => ReplyTransformer.splitText(line, maxChars))
      .flatMap(splitBytes(_, maxBytes))
      .filter(_.nonEmpty)

  /** Caps `lines` at `maxLines`, adding a final line that says how many were left out when the limit is exceeded. */
  def capLines(lines: List[String], maxLines: Int): List[String] =
    if lines.size <= maxLines then lines
    else lines.take(maxLines - 1) :+ s"(${lines.size - maxLines + 1} more lines not shown)"

  /** Cuts the text in lines. More than `maxLines` lines are cut, with a last line that says how many were left out. */
  def split(text: String, maxChars: Int, maxBytes: Int, maxLines: Int): List[String] =
    capLines(toLines(text = text, maxChars = maxChars, maxBytes = maxBytes), maxLines)

  /** Cuts a line in parts of at most `maxBytes` bytes in UTF-8, between characters. */
  def splitBytes(line: String, maxBytes: Int): List[String] = {
    require(maxBytes >= 4, s"maxBytes must hold a character: $maxBytes")
    def go(rest: List[Int], current: StringBuilder, bytes: Int, parts: List[String]): List[String] = rest match {
      case Nil        => (if current.isEmpty then parts else current.toString :: parts).reverse
      case cp :: tail =>
        val size = new String(Character.toChars(cp)).getBytes("UTF-8").length
        if bytes + size > maxBytes then go(
          rest = rest,
          current = new StringBuilder,
          bytes = 0,
          parts = current.toString :: parts
        )
        else go(rest = tail, current = current.appendAll(Character.toChars(cp)), bytes = bytes + size, parts = parts)
    }
    go(rest = line.codePoints.toArray.toList, current = new StringBuilder, bytes = 0, parts = List.empty)
  }
}
