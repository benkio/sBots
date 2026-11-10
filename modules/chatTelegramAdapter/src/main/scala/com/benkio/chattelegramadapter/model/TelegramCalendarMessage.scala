package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.reply.Markdown
import com.benkio.chatcore.model.show.CalendarMessage
import com.benkio.chatcore.model.show.Show

/** The daily calendar message sized for Telegram: the platform-agnostic message with the Telegram limits applied. */
object TelegramCalendarMessage {

  private def remaining(count: Int): String = s"\\.\\.\\.and $count more"

  private val separator = "\n\n"

  /** Within the limit the message is untouched. Otherwise the list (the blocks after the header, which are separated by
    * blank lines) is cut, ending with a "...and N more" block, to stay within `maxLength`.
    */
  def limit(message: Markdown, maxLength: Int = TelegramLimits.maxTextMessageLength): Markdown =
    if message.value.length <= maxLength then message
    else {
      val blocks = message.value.split(separator, -1).toList
      val head   = blocks.head
      val list   = blocks.tail
      // each kept block costs its length plus a separator; the trailer is sized for the worst case (all blocks dropped)
      val budget = maxLength - head.length - separator.length - remaining(list.size).length
      val kept   = list
        .scanLeft(0)((used, block) => used + block.length + separator.length)
        .tail
        .zip(list)
        .takeWhile { case (used, _) => used <= budget }
        .map(_._2)
      message.copy(value = ((head :: kept) :+ remaining(list.size - kept.size)).mkString(separator))
    }

  def render(botName: String, shows: List[Show]): Option[Markdown] =
    CalendarMessage.render(botName, shows).map(limit(_))
}
