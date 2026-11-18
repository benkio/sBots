package com.benkio.chatcore.model.media

import com.benkio.chatcore.model.Platform
import io.circe.Decoder
import io.circe.Encoder
import org.http4s.Uri

/** A place a media can be read from: an url to fetch, or a reference to a file already uploaded to a platform.
  *
  * Stored as a string: an url as it is, a reference as `<platform.id>:<id>`. A reference is only useful to the platform
  * (and the bot) it was produced for, so an untagged reference cannot be represented.
  */
enum MediaSource {
  case Url(uri: Uri)
  case FileRef(platform: Platform, id: String)
}

object MediaSource {

  private val separator = ':'

  /** The stored form: the url, or `<platform.id>:<id>`. */
  def render(source: MediaSource): String = source match {
    case Url(uri)              => uri.toString
    case FileRef(platform, id) => s"${platform.id}$separator$id"
  }

  /** An http(s) url is an url, `<platform.id>:<id>` is a reference, anything else (untagged, unknown platform, empty
    * id) is not a source.
    */
  def parse(value: String): Option[MediaSource] = {
    val asUrl = Uri
      .fromString(value)
      .toOption
      .filter(uri => uri.scheme.exists(s => s.value == "http" || s.value == "https") && uri.host.isDefined)
      .map(Url(_))
    asUrl.orElse {
      val index = value.indexOf(separator)
      if index <= 0 then None
      else {
        val id = value.substring(index + 1).trim
        Platform.fromId(value.substring(0, index)).toOption.filter(_ => id.nonEmpty).map(FileRef(_, id))
      }
    }
  }

  given Decoder[MediaSource] =
    Decoder.decodeString.emap(value => parse(value).toRight(s"'$value' is neither an url nor a platform:id reference"))

  given Encoder[MediaSource] = Encoder.encodeString.contramap(render)

  extension (source: MediaSource) {
    def isUrl: Boolean = source match {
      case Url(_) => true
      case _      => false
    }
  }
}
