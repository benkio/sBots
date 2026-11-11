package com.benkio.chatcore.conversions

import scala.util.matching.Regex

object MarkdownConversions {

  // The characters that must not be taken as formatting are replaced by a private use character until the end
  private val hiddenBase: Int = 0xe100

  private def hide(c: Char): Char = (hiddenBase + c.toInt).toChar

  private def reveal(text: String): String =
    text.map(c => if c >= hide(0.toChar) && c <= hide(0x7f.toChar) then (c - hiddenBase).toChar else c)

  private val escapedSpecial: Regex   = """\\([_*\[\]()~`>#+\-=|{}.!\\])""".r
  private val link: Regex             = """\[([^\]]*)\]\(([^)\s]*)\)""".r
  private val fence: Regex            = """(?m)^```.*\n?""".r
  private val code: Regex             = """`([^`]*)`""".r
  private val bold: Regex             = """\*\*(.+?)\*\*|__(.+?)__""".r
  private val strike: Regex           = """~~(.+?)~~""".r
  private val italicStar: Regex       = """(?<![\w*])\*(?!\s)(.+?)(?<!\s)\*(?![\w*])""".r
  private val italicUnderscore: Regex = """(?<!\w)_(?!\s)(.+?)(?<!\s)_(?!\w)""".r
  private val heading: Regex          = """(?m)^#{1,6}\s+""".r
  private val quote: Regex            = """(?m)^>\s?""".r

  private def replace(regex: Regex, text: String)(f: Regex.Match => String): String =
    regex.replaceAllIn(text, m => Regex.quoteReplacement(f(m)))

  /** Hides the characters that would be taken as formatting from the next steps. */
  private def protect(text: String): String =
    text.map(c => if "_*~`".contains(c) then hide(c) else c)

  /** Turns a markdown text into a plain one, for the platforms without markdown: the formatting is removed, the links
    * become `text (url)` and the escaped characters are shown literally.
    */
  def toPlainText(markdown: String): String = {
    val unescaped = replace(escapedSpecial, markdown)(m => hide(m.group(1).head).toString)
    val linked    = replace(link, unescaped) { m =>
      val text = m.group(1)
      val url  = m.group(2)
      if text.isEmpty || text == url then protect(url) else s"$text (${protect(url)})"
    }
    val unformatted = List[String => String](
      replace(fence, _)(_ => ""),
      replace(code, _)(_.group(1)),
      replace(bold, _)(m => Option(m.group(1)).getOrElse(m.group(2))),
      replace(strike, _)(_.group(1)),
      replace(italicStar, _)(_.group(1)),
      replace(italicUnderscore, _)(_.group(1)),
      replace(heading, _)(_ => ""),
      replace(quote, _)(_ => "")
    ).foldLeft(linked)((text, step) => step(text))
    reveal(unformatted)
  }
}
