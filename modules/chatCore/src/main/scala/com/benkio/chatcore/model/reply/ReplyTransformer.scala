package com.benkio.chatcore.model.reply

import com.benkio.chatcore.conversions.MarkdownConversions
import com.benkio.chatcore.model.MarkdownSupport
import com.benkio.chatcore.model.PlatformCapabilities
import org.http4s.Uri

import scala.annotation.tailrec
import scala.concurrent.duration.FiniteDuration

/** Adapts the replies to what a platform can send, see [[PlatformCapabilities]]. It is pure: the link of a media (a
  * [[MediaFile]] only knows its name) is provided by the caller.
  */
object ReplyTransformer {

  /** The link of a media, if it has one. */
  type MediaLink = MediaFile => Option[Uri]

  /** The transformation of a reply value, which can become none, one or many values:
    *   - text and markdown longer than the platform limit are split, preferably on a line break, then on a space
    *   - markdown becomes plain text when the platform has no markdown
    *   - the time to live of a text is dropped when the platform cannot delete messages
    *   - a media that the platform cannot send becomes its link as a text, or it is dropped when it has none
    *   - a paged list becomes a numbered text on a platform without buttons
    *   - everything else, e.g. the adapter specific values, is left as it is
    */
  def transform(capabilities: PlatformCapabilities, mediaLink: MediaLink = _ => None)(
      value: ReplyValue
  ): List[ReplyValue] = value match {
    case text: Text         => textValues(capabilities, text)
    case markdown: Markdown =>
      capabilities.markdown match {
        case MarkdownSupport.Native => markdownValues(capabilities, markdown)
        case MarkdownSupport.Plain  =>
          textValues(capabilities, Text(MarkdownConversions.toPlainText(markdown.value), markdown.timeToLive))
      }
    case mediaFile: MediaFile =>
      if capabilities.supports(mediaFile) then List(mediaFile)
      else mediaLink(mediaFile).toList.flatMap(uri => textValues(capabilities, Text(uri.renderString)))
    case pagedList: PagedList =>
      if capabilities.supportsButtons then List(pagedList)
      else textValues(capabilities, Text(PagedList.toPlainText(pagedList)))
    case other => List(other)
  }

  def transformAll(capabilities: PlatformCapabilities, mediaLink: MediaLink = _ => None)(
      values: List[ReplyValue]
  ): List[ReplyValue] = values.flatMap(transform(capabilities, mediaLink))

  /** The media sources that the platform can use: the ones that are not an url (`Left`, references to files already
    * uploaded to a platform) only if the platform accepts them.
    */
  def usableSources(capabilities: PlatformCapabilities)(
      sources: List[Either[String, Uri]]
  ): List[Either[String, Uri]] =
    if capabilities.acceptsFileIds then sources else sources.filter(_.isRight)

  /** The message can only be deleted after its time to live if the platform supports the deletion. */
  private def timeToLive(capabilities: PlatformCapabilities, ttl: Option[FiniteDuration]): Option[FiniteDuration] =
    ttl.filter(_ => capabilities.supportsDelete)

  private def textValues(capabilities: PlatformCapabilities, text: Text): List[ReplyValue] =
    splitText(text.value, capabilities.maxTextLength).map(Text(_, timeToLive(capabilities, text.timeToLive)))

  private def markdownValues(capabilities: PlatformCapabilities, markdown: Markdown): List[ReplyValue] =
    splitText(markdown.value, capabilities.maxTextLength).map(
      Markdown(_, timeToLive(capabilities, markdown.timeToLive))
    )

  /** Splits a text in chunks of at most `maxLength` characters, cutting on the last line break, otherwise on the last
    * space, otherwise (no break available) in the middle of a word. The break character is not kept.
    */
  def splitText(text: String, maxLength: Int): List[String] = {
    require(maxLength > 0, s"maxLength must be positive: $maxLength")

    def breakAt(window: String, char: Char): Option[Int] =
      Some(window.lastIndexOf(char.toInt)).filter(_ > 0)

    @tailrec
    def go(rest: String, chunks: List[String]): List[String] =
      if rest.length <= maxLength then (rest :: chunks).reverse
      else {
        val window = rest.take(maxLength + 1)
        val cut    = breakAt(window, '\n').orElse(breakAt(window, ' '))
        cut match {
          case Some(index) => go(rest.drop(index + 1), rest.take(index) :: chunks)
          case None        =>
            val hardCut =
              if maxLength > 1 && Character.isHighSurrogate(rest.charAt(maxLength - 1)) then maxLength - 1
              else maxLength
            go(rest.drop(hardCut), rest.take(hardCut) :: chunks)
        }
      }

    go(text, List.empty).filter(_.nonEmpty) match {
      case Nil if text.isEmpty => List(text)
      case chunks              => chunks
    }
  }
}
