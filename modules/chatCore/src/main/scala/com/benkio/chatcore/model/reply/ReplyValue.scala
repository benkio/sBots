package com.benkio.chatcore.model.reply

import cats.Show
import com.benkio.chatcore.model.media.Media
import com.benkio.chatcore.model.MimeType
import com.benkio.chatcore.model.SBotInfo
import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*

import scala.concurrent.duration.FiniteDuration

trait ReplyValue

object ReplyValue {
  given Decoder[ReplyValue] = new Decoder[ReplyValue] {
    def apply(c: HCursor): Decoder.Result[ReplyValue] =
      c.downField("Text")
        .as[Text]
        .map(identity)
        .orElse(
          c.downField("Mp3File").as[Mp3File].map(mediaFile => mediaFile)
        )
        .orElse(
          c.downField("GifFile").as[GifFile].map(mediaFile => mediaFile)
        )
        .orElse(
          c.downField("PhotoFile").as[PhotoFile].map(mediaFile => mediaFile)
        )
        .orElse(
          c.downField("VideoFile").as[VideoFile].map(mediaFile => mediaFile)
        )
        .orElse(c.downField("Document").as[Document].map(mediaFile => mediaFile))
        .orElse(c.downField("Sticker").as[Sticker].map(mediaFile => mediaFile))
  }

  given Encoder[ReplyValue] = new Encoder[ReplyValue] {
    def apply(value: ReplyValue): Json = value match {
      case text: Text           => Json.obj("Text" -> text.asJson)
      case mp3File: Mp3File     => Json.obj("Mp3File" -> mp3File.asJson)
      case gifFile: GifFile     => Json.obj("GifFile" -> gifFile.asJson)
      case photoFile: PhotoFile => Json.obj("PhotoFile" -> photoFile.asJson)
      case videoFile: VideoFile => Json.obj("VideoFile" -> videoFile.asJson)
      case document: Document   => Json.obj("Document" -> document.asJson)
      case sticker: Sticker     => Json.obj("Sticker" -> sticker.asJson)
    }
  }

  given Show[ReplyValue] with {
    def show(value: ReplyValue): String = value match {
      case text: Text           => text.value
      case mediaFile: MediaFile => mediaFile.filename
    }
  }
}

final case class Text(
    value: String,
    textType: Text.TextType = TextType.Plain,
    timeToLive: Option[FiniteDuration] = None
) extends ReplyValue

object Text {
  enum TextType {
    case Plain    extends TextType
    case Html     extends TextType
    case Markdown extends TextType
  }

  given Show[Text]    = Show.show(_.value)
  given Decoder[Text] = Decoder[String].map(Text(_))
  given Encoder[Text] = Encoder[String].contramap(_.value)
}

enum EffectfulKey(val sBotInfo: SBotInfo) {
  case Random(override val sBotInfo: SBotInfo)     extends EffectfulKey(sBotInfo)
  case SearchShow(override val sBotInfo: SBotInfo) extends EffectfulKey(sBotInfo)
  case TriggerSearch(
      override val sBotInfo: SBotInfo,
      replyBundleMessage: List[ReplyBundleMessage],
      ignoreMessagePrefix: Option[String]
  ) extends EffectfulKey(sBotInfo)
  case Instructions(
      override val sBotInfo: SBotInfo,
      ignoreMessagePrefix: Option[String],
      commands: List[ReplyBundleCommand]
  )                                                              extends EffectfulKey(sBotInfo)
  case Subscribe(override val sBotInfo: SBotInfo)                extends EffectfulKey(sBotInfo)
  case Unsubscribe(override val sBotInfo: SBotInfo)              extends EffectfulKey(sBotInfo)
  case Subscriptions(override val sBotInfo: SBotInfo)            extends EffectfulKey(sBotInfo)
  case TopTwenty(override val sBotInfo: SBotInfo)                extends EffectfulKey(sBotInfo)
  case Timeout(override val sBotInfo: SBotInfo)                  extends EffectfulKey(sBotInfo)
  case MediaByKind(key: String, override val sBotInfo: SBotInfo) extends EffectfulKey(sBotInfo)
  case Callback(key: String, override val sBotInfo: SBotInfo)    extends EffectfulKey(sBotInfo)
}

object EffectfulKey {
  given replyDecoder: Decoder[EffectfulKey] = deriveDecoder[EffectfulKey]
  given replyEncoder: Encoder[EffectfulKey] = deriveEncoder[EffectfulKey]
}

sealed trait MediaFile extends ReplyValue {
  def filepath: String
  def filename: String  = filepath.split('/').last
  def extension: String = filename.takeRight(4)
}

final case class Mp3File(filepath: String, replyToMessage: Boolean = false) extends MediaFile {
  require(filepath.endsWith(".mp3"))
}

object Mp3File {
  given Decoder[Mp3File] = deriveDecoder[Mp3File]
  given Encoder[Mp3File] = deriveEncoder[Mp3File]
}

final case class GifFile(filepath: String, replyToMessage: Boolean = false) extends MediaFile {
  require(filepath.endsWith(".mp4"))
}

object GifFile {
  given Decoder[GifFile] = deriveDecoder[GifFile]
  given Encoder[GifFile] = deriveEncoder[GifFile]
}

final case class PhotoFile(filepath: String, replyToMessage: Boolean = false) extends MediaFile {
  require(List(".jpg", ".png").exists(filepath.endsWith(_)))
}

object PhotoFile {
  given Decoder[PhotoFile] = deriveDecoder[PhotoFile]
  given Encoder[PhotoFile] = deriveEncoder[PhotoFile]
}

final case class VideoFile(filepath: String, replyToMessage: Boolean = false) extends MediaFile {
  require(List(".mp4").exists(filepath.endsWith(_)))
}

object VideoFile {
  given Decoder[VideoFile] = deriveDecoder[VideoFile]
  given Encoder[VideoFile] = deriveEncoder[VideoFile]
}

final case class Document(filepath: String, replyToMessage: Boolean = false) extends MediaFile {}

object Document {
  given Decoder[Document] = deriveDecoder[Document]
  given Encoder[Document] = deriveEncoder[Document]
}

final case class Sticker(filepath: String, replyToMessage: Boolean = false)  extends MediaFile {}

object Sticker {
  given Decoder[Sticker] = deriveDecoder[Sticker]
  given Encoder[Sticker] = deriveEncoder[Sticker]
}

object MediaFile {

  given Decoder[MediaFile] = deriveDecoder[MediaFile]
  given Encoder[MediaFile] = deriveEncoder[MediaFile]

  given showInstance: Show[MediaFile] = Show.show(_.filename)

  def fromString(filename: String): MediaFile = filename.takeRight(4) match {
    case ".mp3"                                       => Mp3File(filename)
    case ".jpg" | ".png"                              => PhotoFile(filename)
    case ".mp4" if filename.takeRight(7) == "Gif.mp4" => GifFile(filename)
    case ".mp4"                                       => VideoFile(filename)
    case _ if filename.takeRight(8) == ".sticker"     => Sticker(filename)
    case _                                            => Document(filename)
  }

  def fromMimeType(media: Media, replyToMessage: Boolean = false): MediaFile = media.mimeType match {
    case MimeType.GIF     => GifFile(media.mediaName, replyToMessage)
    case MimeType.JPEG    => PhotoFile(media.mediaName, replyToMessage)
    case MimeType.PNG     => PhotoFile(media.mediaName, replyToMessage)
    case MimeType.MPEG    => Mp3File(media.mediaName, replyToMessage)
    case MimeType.STICKER => Sticker(media.mediaName, replyToMessage)
    case MimeType.MP4     => VideoFile(media.mediaName, replyToMessage)
    case MimeType.DOC     => Document(media.mediaName, replyToMessage)
  }
}

extension (sc: StringContext) {
  def mp3(args: Any*): Mp3File     = Mp3File(sc.s(args*))
  def gif(args: Any*): GifFile     = GifFile(sc.s(args*))
  def vid(args: Any*): VideoFile   = VideoFile(sc.s(args*))
  def pho(args: Any*): PhotoFile   = PhotoFile(sc.s(args*))
  def txt(args: Any*): Text        = Text(sc.s(args*))
  def doc(args: Any*): Document    = Document(sc.s(args*))
  def sticker(args: Any*): Sticker = Sticker(sc.s(args*))
}

extension (values: List[String]) def toText: List[Text] = values.map(Text(_))
