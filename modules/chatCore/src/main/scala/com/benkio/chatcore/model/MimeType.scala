package com.benkio.chatcore.model

import cats.Show
enum MimeType(val value: String) {
  case GIF     extends MimeType("image/gif")
  case JPEG    extends MimeType("image/jpeg")
  case PNG     extends MimeType("image/png")
  case STICKER extends MimeType("image/sticker")
  case MPEG    extends MimeType("audio/mpeg")
  case MP4     extends MimeType("video/mp4")
  case MD      extends MimeType("text/markdown")
  case DOC     extends MimeType("application/octet-stream")
}

object MimeTypeOps {

  given Show[MimeType] = Show.show[MimeType](_.value)

  private def fromString(mimeType: String): Option[MimeType] = mimeType match {
    case "image/gif"                => Some(MimeType.GIF)
    case "image/jpeg"               => Some(MimeType.JPEG)
    case "image/png"                => Some(MimeType.PNG)
    case "image/sticker"            => Some(MimeType.STICKER)
    case "audio/mpeg"               => Some(MimeType.MPEG)
    case "video/mp4"                => Some(MimeType.MP4)
    case "text/markdown"            => Some(MimeType.MD)
    case "application/octet-stream" => Some(MimeType.DOC)
    case _                          => None
  }

  private def fromMediaName(media_name: String): Option[MimeType] = {
    val lowerName = media_name.toLowerCase
    val mapping   = List(
      "gif.mp4"  -> MimeType.GIF,
      ".mp4"     -> MimeType.MP4,
      ".md"      -> MimeType.MD,
      ".mp3"     -> MimeType.MPEG,
      ".sticker" -> MimeType.STICKER,
      ".png"     -> MimeType.PNG,
      ".jpg"     -> MimeType.JPEG,
      ".gif"     -> MimeType.GIF
    )
    mapping.collectFirst { case (suffix, mimeType) if lowerName.endsWith(suffix) => mimeType }
  }

  def mimeTypeOrDefault(media_name: String, mime_type: Option[String]): MimeType =
    mime_type.flatMap(fromString).orElse(fromMediaName(media_name)).getOrElse(MimeType.DOC)
}
