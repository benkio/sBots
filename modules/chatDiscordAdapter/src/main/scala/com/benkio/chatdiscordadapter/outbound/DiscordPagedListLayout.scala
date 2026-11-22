package com.benkio.chatdiscordadapter.outbound

import com.benkio.chatcore.conversions.YouTubeTimestamp.finiteDurationToYoutubeTimestamp
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.PagedItem
import com.benkio.chatcore.model.reply.PagedList
import com.benkio.chatcore.model.reply.PagedListAction
import com.benkio.chatdiscordadapter.model.DiscordLimits

/** A [[PagedList]] as a Discord message. Telegram's layout, a row per item, would need more than the 5 rows of Discord,
  * so: the page as numbered text, rows of at most five buttons that pick an item (labelled with the number of the item
  * in the text), and a row with previous and next, when there is more than a page. The ids of the buttons are the ones
  * of the [[PagedListAction]].
  */
object DiscordPagedListLayout {

  val maxLabelLength: Int   = 80
  val maxButtonsPerRow: Int = 5
  val maxRows: Int          = 5

  def render(pagedList: PagedList): DiscordButtonMessage = {
    val pickRow = pagedList.pageItems.zipWithIndex.flatMap { case (item, index) =>
      pickAction(item)
        .flatMap(_.toId)
        .map(id => DiscordButton(id, s"${pagedList.page * pagedList.pageSize + index + 1}"))
    }
    val navigationRow =
      Option.when(pagedList.totalPages > 1)(
        List(
          navigation(
            label = "Previous",
            action = PagedListAction.PreviousPage(pagedList.page, pagedList.commandKey),
            disabled = !pagedList.hasPrevious
          ),
          navigation(
            label = "Next",
            action = PagedListAction.NextPage(pagedList.page, pagedList.commandKey),
            disabled = !pagedList.hasNext
          )
        ).flatten
      )
    DiscordButtonMessage(
      text = PagedList.toPlainText(pagedList).take(DiscordLimits.maxTextMessageLength),
      rows = pickRow
        .grouped(maxButtonsPerRow)
        .take(maxRows - navigationRow.size.min(1))
        .toList ++ navigationRow.toList
    )
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
