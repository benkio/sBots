package com.benkio.telegrambotinfrastructure.model

import cats.Show
enum MimeType(val value: String):
  case GIF extends MimeType("image/gif")
  case JPEG extends MimeType("image/jpeg")
  case PNG extends MimeType("image/png")
  case STICKER extends MimeType("image/sticker")
  case MPEG extends MimeType("audio/mpeg")
  case MP4 extends MimeType("video/mp4")
  case DOC extends MimeType("application/octet-stream")

object MimeTypeOps:

  given Show[MimeType] = Show.show[MimeType](_.value)
  
  def mimeTypeOrDefault(media_name: String, mime_type: Option[String]): MimeType =
      val mimeType = mime_type.getOrElse(media_name.toLowerCase.takeRight(3)) match {
      case "gif"     => MimeType.GIF
      case "jpg"     => MimeType.JPEG
      case "png"     => MimeType.PNG
      case "sticker" => MimeType.STICKER
      case "mp3"     => MimeType.MPEG
      case "mp4"     => MimeType.MP4
      case _         => MimeType.DOC
      }
      mimeType
