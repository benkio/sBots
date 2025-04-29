package com.benkio.telegrambotinfrastructure.model

import cats.Show
enum MimeType(val value: String):
  case GIF     extends MimeType("image/gif")
  case JPEG    extends MimeType("image/jpeg")
  case PNG     extends MimeType("image/png")
  case STICKER extends MimeType("image/sticker")
  case MPEG    extends MimeType("audio/mpeg")
  case MP4     extends MimeType("video/mp4")
  case DOC     extends MimeType("application/octet-stream")

object MimeTypeOps:

  given Show[MimeType] = Show.show[MimeType](_.value)

  private def fromString(mimeType: String): Option[MimeType] = mimeType match {
    case "image/gif"                => Some(MimeType.GIF)
    case "image/jpeg"               => Some(MimeType.JPEG)
    case "image/png"                => Some(MimeType.PNG)
    case "image/sticker"            => Some(MimeType.STICKER)
    case "audio/mpeg"               => Some(MimeType.MPEG)
    case "video/mp4"                => Some(MimeType.MP4)
    case "application/octet-stream" => Some(MimeType.DOC)
    case _                          => None
  }

  private def fromMediaName(media_name: String): Option[MimeType] =
    media_name.toLowerCase.takeRight(3) match {
      case "gif"     => Some(MimeType.GIF)
      case "jpg"     => Some(MimeType.JPEG)
      case "png"     => Some(MimeType.PNG)
      case "sticker" => Some(MimeType.STICKER)
      case "mp3"     => Some(MimeType.MPEG)
      case "mp4"     => Some(MimeType.MP4)
      case _         => None
    }

  def mimeTypeOrDefault(media_name: String, mime_type: Option[String]): MimeType =
    mime_type.flatMap(fromString).orElse(fromMediaName(media_name)).getOrElse(MimeType.DOC)
