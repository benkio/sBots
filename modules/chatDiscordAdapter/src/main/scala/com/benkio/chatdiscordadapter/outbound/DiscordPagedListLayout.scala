package com.benkio.chatdiscordadapter.outbound

import com.benkio.chatcore.conversions.YouTubeTimestamp.finiteDurationToYoutubeTimestamp
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.reply.PagedListAction
import com.benkio.chatdiscordadapter.model.DiscordCapabilities
import com.benkio.chatdiscordadapter.model.DiscordLimits

/** A [[PagedList]] as a Discord message. Telegram's layout, a row per item, would need more than the 5 rows of Discord,
  * so: the page as numbered text, rows of at most five buttons that pick an item (labelled with the number of the item
  * in the text), and a row with previous and next, when there is more than a page. The media Discord cannot send (e.g.
  * Telegram stickers) are left out of both the text and the buttons, and the numbering stays contiguous. The ids of the
  * buttons are the ones of the [[PagedListAction]].
  */
object DiscordPagedListLayout {

  val maxLabelLength: Int   = 80
  val maxButtonsPerRow: Int = 5
  val maxRows: Int          = 5

  def render(pagedList: PagedList): DiscordButtonMessage = {
    val visible = supported(pagedList)
    val pickRow = visible.pageItems.zipWithIndex.flatMap { case (item, index) =>
      pickAction(item)
        .flatMap(_.toId)
        .map(id => DiscordButton(id, s"${visible.page * visible.pageSize + index + 1}"))
    }
    val navigationRow =
      Option.when(visible.totalPages > 1)(
        List(
          navigation(
            label = "Previous",
            action = PagedListAction.PreviousPage(visible.page, visible.commandKey),
            disabled = !visible.hasPrevious
          ),
          navigation(
            label = "Next",
            action = PagedListAction.NextPage(visible.page, visible.commandKey),
            disabled = !visible.hasNext
          )
        ).flatten
      )
    DiscordButtonMessage(
      text = PagedList.toPlainText(visible).take(DiscordLimits.maxTextMessageLength),
      rows = pickRow
        .grouped(maxButtonsPerRow)
        .take(maxRows - navigationRow.size.min(1))
        .toList ++ navigationRow.toList
    )
  }

  /** The list as Discord can show it: the media it cannot send are dropped, so their text and buttons go with them. The
    * page is clamped to the filtered list, which keeps the numbering and the navigation consistent.
    */
  private def supported(pagedList: PagedList): PagedList = {
    val items = pagedList.items.filter {
      case PagedItem.ValueItem(mediaFile: MediaFile) => DiscordCapabilities.value.supports(mediaFile)
      case _                                         => true
    }
    val totalPages = math.max(1, (items.length + pagedList.pageSize - 1) / pagedList.pageSize)
    pagedList.copy(items = items, page = pagedList.page.min(totalPages - 1).max(0))
  }

  /** What picking the item does: the media file, the show or a trigger candidate. The texts have nothing to pick. */
  private def pickAction(item: PagedItem): Option[PagedListAction] = item match {
    case PagedItem.ShowItem(show) =>
      Some(PagedListAction.Show(show.id, show.timestamp.map(finiteDurationToYoutubeTimestamp)))
    case PagedItem.ValueItem(mediaFile: MediaFile) => Some(PagedListAction.Media(mediaFile.filename))
    case PagedItem.ValueItem(_)                    => None
    case PagedItem.TriggerItem(value)              => Some(PagedListAction.Trigger(value))
  }

  private def navigation(label: String, action: PagedListAction, disabled: Boolean): Option[DiscordButton] =
    action.toId.map(DiscordButton(_, label, disabled))
}
