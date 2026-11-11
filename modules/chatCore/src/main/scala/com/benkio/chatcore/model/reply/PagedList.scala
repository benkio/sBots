package com.benkio.chatcore.model.reply

import cats.data.NonEmptyList
import cats.syntax.all.*
import cats.Show as CatsShow
import com.benkio.chatcore.messagefiltering.getContent
import com.benkio.chatcore.model.show.Show
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Trigger

/** What a [[PagedList]] lists: a show, or one of the values of a reply (a text or a media file). */
enum PagedItem {
  case ShowItem(show: Show)
  case ValueItem(value: ReplyValue)

  /** What the item is called: the title of the show, the text, the name of the media file. */
  def label: String = this match {
    case ShowItem(show)                  => show.title
    case ValueItem(text: Text)           => text.value
    case ValueItem(markdown: Markdown)   => markdown.value
    case ValueItem(mediaFile: MediaFile) => mediaFile.filename
    case ValueItem(other)                => other.toString
  }
}

/** A list the user browses page by page, e.g. the result of a search, whatever the platform: a platform with buttons
  * shows the page with the buttons to move across the pages, one without lists the items of the page as text (see
  * [[ReplyTransformer]]).
  *
  * @param title
  *   what the list is about: the first line is the headline, the rest can echo what was asked for
  * @param page
  *   the page to show, from 0
  * @param commandKey
  *   the command that produced the list, to ask it for another page
  */
final case class PagedList(
    title: String,
    items: List[PagedItem],
    page: Int,
    commandKey: CommandKey,
    pageSize: Int = PagedList.defaultPageSize
) extends ReplyValue {
  require(pageSize > 0, s"pageSize must be positive: $pageSize")

  def pageItems: List[PagedItem] = items.slice(page * pageSize, (page + 1) * pageSize)
  def hasPrevious: Boolean       = page > 0
  def hasNext: Boolean           = (page + 1) * pageSize < items.length
  def totalPages: Int            = math.max(1, (items.length + pageSize - 1) / pageSize)
}

object PagedList {

  val defaultPageSize: Int = 5

  /** The title of the lists that answer a search: how many results there are, what was typed and what was searched. */
  def searchTitle[A: CatsShow](m: Message, input: A, valuesCount: Int): String =
    s"""Input ($valuesCount):
       |${m.getContent.getOrElse("")}
       |
       |${input.show}""".stripMargin

  def shows(m: Message, keywords: String, shows: NonEmptyList[Show], page: Int): PagedList =
    PagedList(
      title = searchTitle(m = m, input = keywords, valuesCount = shows.length),
      items = shows.toList.map(PagedItem.ShowItem(_)),
      page = page,
      commandKey = CommandKey.SearchShow
    )

  def triggers(m: Message, replyBundleMessage: ReplyBundleMessage, page: Int): PagedList = {
    val values = replyBundleMessage.reply.replyValues.toList
    PagedList(
      title = searchTitle(m = m, input = replyBundleMessage.trigger: Trigger, valuesCount = values.size),
      items = values.map(PagedItem.ValueItem(_)),
      page = page,
      commandKey = CommandKey.TriggerSearch
    )
  }

  def topTwenty(mediaFiles: List[MediaFile], page: Int): PagedList =
    PagedList(
      title = "-----Top 20 Triggers-----",
      items = mediaFiles.map(PagedItem.ValueItem(_)),
      page = page,
      commandKey = CommandKey.TopTwenty
    )

  /** The page as plain text, for the platforms with no buttons: the headline, the numbered items (the numbers go on
    * across the pages) and the position among the pages.
    */
  def toPlainText(pagedList: PagedList): String = {
    val headline = pagedList.title.linesIterator.nextOption().getOrElse("")
    val numbered = pagedList.pageItems.zipWithIndex.map { case (item, index) =>
      s"${pagedList.page * pagedList.pageSize + index + 1}. ${item.label}"
    }
    val position = Option.when(pagedList.totalPages > 1)(s"page ${pagedList.page + 1} of ${pagedList.totalPages}")
    (headline :: numbered ++ position.toList).mkString("\n")
  }
}
