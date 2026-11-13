package com.benkio.chatircadapter.model

import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.MarkdownSupport
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.PlatformCapabilities

/** What IRC can do: plain text only. The replies are adapted to it by the
  * [[com.benkio.chatcore.model.reply.ReplyTransformer]]: the markdown becomes plain text, the texts are split to the
  * line limit, a media becomes its link, a time to live is dropped (a message cannot be deleted) and a paged list
  * becomes a numbered text. The commands are written with a `!` prefix, with no mention separator. No file references
  * (`acceptsFileIds = false`): the sources that are not an url are Telegram's.
  */
object IrcCapabilities {
  val value: PlatformCapabilities = PlatformCapabilities(
    platform = Platform.Irc,
    supportedMedia = Set.empty,
    maxTextLength = IrcLimits.maxTextMessageLength,
    markdown = MarkdownSupport.Plain,
    supportsDelete = false,
    supportsEdit = false,
    supportsButtons = false,
    acceptsFileIds = false,
    commandSyntax = CommandSyntax(prefix = "!")
  )
}
