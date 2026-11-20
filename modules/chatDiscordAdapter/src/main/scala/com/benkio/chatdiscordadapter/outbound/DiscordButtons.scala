package com.benkio.chatdiscordadapter.outbound

/** A button of a message: the id is what comes back when it is pressed (see `PagedListAction`). */
final case class DiscordButton(id: String, label: String, disabled: Boolean = false)

/** A message with rows of buttons: Discord allows 5 rows of 5 buttons. */
final case class DiscordButtonMessage(text: String, rows: List[List[DiscordButton]])
