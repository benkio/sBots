package com.benkio.chatdiscordadapter.model

import com.benkio.chatcore.model.CommandSyntax
import com.benkio.chatcore.model.MarkdownSupport
import com.benkio.chatcore.model.MediaKind
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.PlatformCapabilities

/** What Discord can do. The files are uploaded as attachments, so every media but the Telegram stickers and the
  * markdown files (sent on Telegram as rich text) goes through; those become a link. The commands are plain messages
  * with a `!` prefix, no mention separator: a Discord mention is `<@id>`. No buttons for now, and no file references
  * (`acceptsFileIds = false`): the sources that are not an url are Telegram's, they come back with the milestone M4.
  */
object DiscordCapabilities {
  val value: PlatformCapabilities = PlatformCapabilities(
    platform = Platform.Discord,
    supportedMedia = Set(MediaKind.Audio, MediaKind.Gif, MediaKind.Photo, MediaKind.Video, MediaKind.Document),
    maxTextLength = DiscordLimits.maxTextMessageLength,
    markdown = MarkdownSupport.Native,
    supportsDelete = true,
    supportsEdit = true,
    supportsButtons = false,
    acceptsFileIds = false,
    commandSyntax = CommandSyntax(prefix = "!")
  )
}
