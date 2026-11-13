package com.benkio.chatircadapter.outbound

import com.benkio.chatcore.model.reply.ReplyTransformer

/** A text as the lines IRC can carry: a message is one line, and a line is at most 512 bytes with the prefix the server
  * adds and the line end.
  */
object IrcLines {

  /** Cuts the text in lines. The line breaks start a new line (the empty ones are dropped), the other control characters
    * are removed (a line break or a code in a text sent as it is would be taken as a command by the server, or as
    * formatting), a line over the limit of characters is cut on a space when it can, and then over the limit of bytes:
    * the multibyte characters make a line longer in bytes than in characters. More than `maxLines` lines are cut, with a
    * last line that says how many were left out.
    */
  def split(text: String, maxChars: Int, maxBytes: Int, maxLines: Int): List[String] = {
    val lines = text
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
    if lines.size <= maxLines then lines
    else lines.take(maxLines - 1) :+ s"(${lines.size - maxLines + 1} more lines not shown)"
  }

  /** Cuts a line in parts of at most `maxBytes` bytes in UTF-8, between characters. */
  def splitBytes(line: String, maxBytes: Int): List[String] = {
    require(maxBytes >= 4, s"maxBytes must hold a character: $maxBytes")
    def go(rest: List[Int], current: StringBuilder, bytes: Int, parts: List[String]): List[String] = rest match {
      case Nil         => (if current.isEmpty then parts else current.toString :: parts).reverse
      case cp :: tail =>
        val size = new String(Character.toChars(cp)).getBytes("UTF-8").length
        if bytes + size > maxBytes then go(rest = rest, current = new StringBuilder, bytes = 0, parts = current.toString :: parts)
        else go(rest = tail, current = current.appendAll(Character.toChars(cp)), bytes = bytes + size, parts = parts)
    }
    go(rest = line.codePoints.toArray.toList, current = new StringBuilder, bytes = 0, parts = List.empty)
  }
}
