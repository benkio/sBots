package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.reply.Markdown
import com.benkio.chatcore.model.show.CalendarMessage
import com.benkio.chatcore.model.show.Show

/** The daily calendar message sized for Telegram: the platform-agnostic message with the Telegram limits applied. */
object TelegramCalendarMessage {

  private def remaining(count: Int): String = s"\\.\\.\\.and $count more"

  /** Within the limit the message is untouched. Otherwise the list (the lines after the first blank line) is cut,
    * ending with a "...and N more" line, to stay within [[TelegramLimits.maxTextMessageLength]].
    */
  def limit(message: Markdown, maxLength: Int = TelegramLimits.maxTextMessageLength): Markdown =
    if message.value.length <= maxLength then message
    else {
      val (head, items) = message.value.split("\n", -1).toList.span(_.nonEmpty)
      val list          = items.drop(1) // the blank line separating the header from the list
      // each kept line costs its length plus a newline; the trailer is sized for the worst case (all lines dropped)
      val budget = maxLength - head.mkString("\n").length - 2 - (remaining(list.size).length + 1)
      val kept   = list
        .scanLeft(0)((used, line) => used + line.length + 1)
        .tail
        .zip(list)
        .takeWhile { case (used, _) => used <= budget }
        .map(_._2)
      message.copy(value = ((head :+ "") ++ kept :+ remaining(list.size - kept.size)).mkString("\n"))
    }

  def render(botName: String, shows: List[Show]): Option[Markdown] =
    CalendarMessage.render(botName, shows).map(limit(_))
}
