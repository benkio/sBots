package com.benkio.chatdiscordadapter.model

import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.MarkdownSupport
import com.benkio.chatcore.model.MediaKind
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.PlatformCapabilities

/** What Discord can do. The files are uploaded as attachments, and a markdown file is sent as a formatted text message
  * (the Discord markdown is close to ours), as Telegram does with its rich messages. The Telegram stickers are not
  * supported: they become a link. The commands are slash commands, `/command`, with no mention separator: a Discord
  * mention is `<@id>`. No buttons for now, and no file references (`acceptsFileIds = false`): the sources that are not
  * an url are Telegram's, they come back with the milestone M4.
  */
object DiscordCapabilities {
  val value: PlatformCapabilities = PlatformCapabilities(
    platform = Platform.Discord,
    supportedMedia =
      Set(MediaKind.Audio, MediaKind.Gif, MediaKind.Photo, MediaKind.Video, MediaKind.Document, MediaKind.MarkdownFile),
    maxTextLength = DiscordLimits.maxTextMessageLength,
    markdown = MarkdownSupport.Native,
    supportsDelete = true,
    supportsEdit = true,
    supportsButtons = false,
    acceptsFileIds = false,
    commandSyntax = CommandSyntax(prefix = "/")
  )
}
