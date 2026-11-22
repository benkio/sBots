package com.benkio.chatcore.model.reply

import cats.data.NonEmptyList
import cats.syntax.all.*
import cats.Show as CatsShow
import com.benkio.chatcore.messagefiltering.getContent
import com.benkio.chatcore.model.show.Show
import com.benkio.chatcore.model.CommandKey
import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.Message
import com.benkio.chatcore.model.Trigger

/** What a [[PagedList]] lists: a show, or one of the values of a reply (a text or a media file). */
enum PagedItem {
  case ShowItem(show: Show)
  case ValueItem(value: ReplyValue)
  case TriggerItem(value: String)

  /** What the item is called: the title of the show, the text, the name of the media file, the trigger. */
  def label: String = this match {
    case ShowItem(show)                  => show.title
    case ValueItem(text: Text)           => text.value
    case ValueItem(markdown: Markdown)   => markdown.value
    case ValueItem(mediaFile: MediaFile) => mediaFile.filename
    case ValueItem(other)                => other.toString
    case TriggerItem(value)              => value
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

  /** The one-page keyboard of trigger candidates offered when a trigger search has no confident match. Picking a
    * candidate runs the trigger search again with that candidate as its exact input.
    */
  def triggerSuggestions(query: String, candidates: List[String]): PagedList =
    PagedList(
      title = s"No exact match for '$query'. Did you mean:",
      items = candidates.take(defaultPageSize).map(PagedItem.TriggerItem(_)),
      page = 0,
      commandKey = CommandKey.TriggerSearch,
      pageSize = defaultPageSize
    )

  def topTwenty(mediaFiles: List[MediaFile], page: Int): PagedList =
    PagedList(
      title = "-----Top 20 Triggers-----",
      items = mediaFiles.map(PagedItem.ValueItem(_)),
      page = page,
      commandKey = CommandKey.TopTwenty
    )

  /** The page as plain text, for the platforms with no buttons: the headline, the numbered items (the numbers go on
    * across the pages) and the position among the pages. With `commandSyntax`, the text says how to move the list with
    * its text commands: the `!next`/`!prev` of the next/previous page and the `!pick 1-n` of the items of the page (see
    * [[PagedListTextCommands]]).
    */
  def toPlainText(pagedList: PagedList, commandSyntax: Option[CommandSyntax] = None): String =
    if pagedList.items.nonEmpty && pagedList.items.forall(_.isInstanceOf[PagedItem.TriggerItem]) then
      s"${pagedList.title} ${pagedList.items.map(_.label).mkString(", ")}?"
    else {
      val headline = pagedList.title.linesIterator.nextOption().getOrElse("")
      val numbered = pagedList.pageItems.zipWithIndex.map { case (item, index) =>
        s"${pagedList.page * pagedList.pageSize + index + 1}. ${item.label}"
      }
      val position = Option.when(pagedList.totalPages > 1)(s"page ${pagedList.page + 1} of ${pagedList.totalPages}")
      val hint     = commandSyntax.flatMap(PagedList.navigationHint(pagedList, _))
      (headline :: numbered ++ position.toList ++ hint.toList).mkString("\n")
    }

  /** How to move the list with the text commands of the platform without buttons: `!next`, `!prev` and `!pick 1-5` —
    * `!next`/`!prev` are shown only when there is a next/previous page, `!pick n` with the numbers of the items of the
    * page (`!pick 1-5` when the page is full).
    */
  def navigationHint(pagedList: PagedList, commandSyntax: CommandSyntax): Option[String] = {
    val move = List(
      Option.when(pagedList.hasNext)(s"${commandSyntax.prefix}next"),
      Option.when(pagedList.hasPrevious)(s"${commandSyntax.prefix}prev")
    ).flatten
    val pick = Option.when(pagedList.pageItems.nonEmpty)(s"${commandSyntax.prefix}pick 1-${pagedList.pageItems.size}")
    Option.when((move ++ pick).nonEmpty)((move ++ pick).mkString(" / "))
  }
}
