package com.benkio.chatcore.model

import com.benkio.chatcore.model.reply.Document
import com.benkio.chatcore.model.reply.GifFile
import com.benkio.chatcore.model.reply.MarkdownFile
import com.benkio.chatcore.model.reply.MediaFile
import com.benkio.chatcore.model.reply.Mp3File
import com.benkio.chatcore.model.reply.PhotoFile
import com.benkio.chatcore.model.reply.Sticker
import com.benkio.chatcore.model.reply.VideoFile

/** The kinds of media a platform can send, one per [[MediaFile]] subtype. */
enum MediaKind {
  case Audio, Gif, Photo, Video, Document, Sticker, MarkdownFile
}

object MediaKind {
  def of(mediaFile: MediaFile): MediaKind = mediaFile match {
    case _: Mp3File      => MediaKind.Audio
    case _: GifFile      => MediaKind.Gif
    case _: PhotoFile    => MediaKind.Photo
    case _: VideoFile    => MediaKind.Video
    case _: Document     => MediaKind.Document
    case _: Sticker      => MediaKind.Sticker
    case _: MarkdownFile => MediaKind.MarkdownFile
  }
}

/** How a platform renders the [[com.benkio.chatcore.model.reply.Markdown]] replies. */
enum MarkdownSupport {

  /** The adapter renders the markdown, converting it to the platform dialect when needed. */
  case Native

  /** No markdown at all (e.g. IRC): the markdown is turned into plain text. */
  case Plain
}

/** What a platform can send and do: the [[com.benkio.chatcore.model.reply.ReplyTransformer]] adapts the replies to it.
  *
  * @param supportedMedia
  *   the media kinds the platform can send; any other media is sent as a link
  * @param maxTextLength
  *   the maximum number of characters of a text message; longer texts are split
  * @param acceptsFileIds
  *   whether the media sources that are not an url (`Left`, Telegram file ids) can be used
  * @param commandSyntax
  *   how the commands and the file requests are written, e.g. `/command@BotName` or `!command`
  */
final case class PlatformCapabilities(
    platform: Platform,
    supportedMedia: Set[MediaKind],
    maxTextLength: Int,
    markdown: MarkdownSupport,
    supportsDelete: Boolean,
    supportsEdit: Boolean,
    supportsButtons: Boolean,
    acceptsFileIds: Boolean,
    commandSyntax: CommandSyntax
) {
  require(maxTextLength > 0, s"maxTextLength must be positive: $maxTextLength")

  /** What a command message starts with, e.g. `/` or `!`. */
  def commandPrefix: String = commandSyntax.prefix

  def supports(mediaFile: MediaFile): Boolean = supportedMedia.contains(MediaKind.of(mediaFile))
}
