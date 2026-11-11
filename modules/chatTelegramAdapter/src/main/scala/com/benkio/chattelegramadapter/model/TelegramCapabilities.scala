package com.benkio.chattelegramadapter.model

import com.benkio.chatcore.model.MarkdownSupport
import com.benkio.chatcore.model.MediaKind
import com.benkio.chatcore.model.Platform
import com.benkio.chatcore.model.PlatformCapabilities

/** What Telegram can do: it sends every kind of media, so the [[com.benkio.chatcore.model.reply.ReplyTransformer]]
  * leaves the replies as they are.
  */
object TelegramCapabilities {
  val value: PlatformCapabilities = PlatformCapabilities(
    platform = Platform.Telegram,
    supportedMedia = MediaKind.values.toSet,
    maxTextLength = TelegramLimits.maxTextMessageLength,
    markdown = MarkdownSupport.Native,
    supportsDelete = true,
    supportsEdit = true,
    supportsButtons = true,
    acceptsFileIds = true,
    commandPrefix = "/"
  )
}
