package com.benkio.chatcore.model.show

import com.benkio.chatcore.model.reply.Markdown

/** The daily calendar message: one markdown (rich message) text listing the shows of the day, linking to YouTube. */
object CalendarMessage {

  // Inside the `(...)` of a link only `)` and `\` have to be escaped.
  private def escapeUrl(url: String): String =
    url.flatMap(c => if c == ')' || c == '\\' then s"\\$c" else c.toString)

  private def header(botName: String): String = s"📅 ${Markdown.escape(botName)}: oggi nel calendario"

  private def item(show: Show): String =
    s"• [${Markdown.escape(show.title)}](${escapeUrl(Show.youtubeUrl(show))})"

  /** `None` when there are no shows. Shows are ordered by upload date and then id. Blocks (the header and every show)
    * are separated by blank lines: a single newline is only a soft break in markdown. The whole message is returned:
    * the adapter of the platform it is sent to applies its own limits.
    */
  def render(botName: String, shows: List[Show]): Option[Markdown] =
    Option.when(shows.nonEmpty) {
      val items = shows.sortBy(show => (show.uploadDate.toEpochDay, show.id)).map(item)
      Markdown(value = (header(botName) :: items).mkString("\n\n"))
    }
}
